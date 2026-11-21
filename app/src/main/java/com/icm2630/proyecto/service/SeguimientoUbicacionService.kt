package com.icm2630.proyecto.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.icm2630.proyecto.MainActivity
import com.icm2630.proyecto.data.model.PuntoMapa
import com.icm2630.proyecto.data.remote.FirebaseDb
import com.icm2630.proyecto.data.repository.UbicacionRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class EstadoSeguimiento(val uid: String? = null, val error: String? = null)

/** El estado del servicio es local; el consentimiento y las coordenadas están en Firestore. */
object SeguimientoUbicacion {
    internal val estadoMutable = MutableStateFlow(EstadoSeguimiento())
    val estado = estadoMutable.asStateFlow()

    fun iniciar(context: Context): Boolean {
        val location = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(location)) {
            estadoMutable.value = EstadoSeguimiento(error = "Enciende la ubicación del dispositivo antes de iniciar.")
            return false
        }
        return try {
            ContextCompat.startForegroundService(context, Intent(context, SeguimientoUbicacionService::class.java))
            true
        } catch (_: RuntimeException) {
            estadoMutable.value = EstadoSeguimiento(error = "No se pudo iniciar. Abre la app y revisa el permiso de ubicación.")
            false
        }
    }

    fun detener(context: Context) {
        context.stopService(Intent(context, SeguimientoUbicacionService::class.java))
    }
}

/** Iniciado por el usuario con la app visible; continúa al minimizar o bloquear la pantalla. */
class SeguimientoUbicacionService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val posiciones = Channel<Location>(Channel.CONFLATED)
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private var uid: String? = null
    private var escuchando = false
    private var consentimiento: com.google.firebase.firestore.ListenerRegistration? = null
    private var ultimaMedicionNanos = 0L
    private var ultimoEnvioNanos = 0L
    private val authListener = FirebaseAuth.AuthStateListener {
        if (uid != null && it.currentUser?.uid != uid) stopSelf()
    }
    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (!escuchando || auth.currentUser?.uid != uid) return
            val punto = result.lastLocation ?: return
            val ahora = SystemClock.elapsedRealtimeNanos()
            val edad = ahora - punto.elapsedRealtimeNanos
            if (edad !in 0..120_000_000_000L || punto.elapsedRealtimeNanos <= ultimaMedicionNanos) return
            if (!punto.latitude.isFinite() || !punto.longitude.isFinite()) return
            if (ultimoEnvioNanos != 0L && ahora - ultimoEnvioNanos < 10_000_000_000L) return
            ultimaMedicionNanos = punto.elapsedRealtimeNanos
            ultimoEnvioNanos = ahora
            posiciones.trySend(punto)
        }

        override fun onLocationAvailability(value: LocationAvailability) {
            if (!value.isLocationAvailable && escuchando) {
                notificar("Esperando señal de ubicación. Revisa el GPS del dispositivo.")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == DETENER) {
            detenerConConsentimiento()
            return START_NOT_STICKY
        }
        if (uid != null) return START_NOT_STICKY
        val actual = auth.currentUser?.uid
        if (actual == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        uid = actual
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                getSystemService(NotificationManager::class.java).createNotificationChannel(
                    NotificationChannel(CANAL, "Ubicación compartida", NotificationManager.IMPORTANCE_LOW)
                )
            }
            ServiceCompat.startForeground(this, NOTIFICACION,
                notificacion("Preparando la ubicación compartida…"),
                if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0)
        } catch (_: RuntimeException) {
            fallar("No se pudo iniciar el seguimiento. Revisa los permisos y el GPS.")
            return START_NOT_STICKY
        }
        auth.addAuthStateListener(authListener)
        consentimiento = FirebaseDb.firestore.collection("usuarios").document(actual)
            .addSnapshotListener { doc, error ->
                if (error != null || doc?.getBoolean("compartirUbicacion") != true) stopSelf()
            }
        scope.launch {
            try {
                // La autorización se comprueba en el servidor antes de comenzar a publicar.
                val cuenta = FirebaseDb.firestore.collection("usuarios").document(actual)
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                if (cuenta.getBoolean("compartirUbicacion") != true || auth.currentUser?.uid != actual) {
                    stopSelf()
                    return@launch
                }
                iniciarGps(actual)
                for (location in posiciones) {
                    if (!escuchando || auth.currentUser?.uid != actual) break
                    try {
                        // Una escritura en curso y solo la última muestra pendiente.
                        UbicacionRepository.guardar(actual, PuntoMapa(location.latitude, location.longitude))
                        if (escuchando) notificar("Compartiendo con tus personas vinculadas, incluso con la app minimizada.")
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: FirebaseFirestoreException) {
                        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            fallar("El seguimiento se detuvo porque cambió la autorización.")
                            break
                        }
                        notificar("No se pudo enviar la ubicación. Revisa la conexión.")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                fallar("No se pudo iniciar el seguimiento. Revisa la conexión, el GPS y los permisos.")
            }
        }
        return START_NOT_STICKY
    }

    private fun iniciarGps(actual: String) {
        val preciso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val aproximado = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        check(preciso || aproximado)
        val request = LocationRequest.Builder(
            if (preciso) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            10_000L
        ).setMinUpdateIntervalMillis(5_000L).setMaxUpdateDelayMillis(10_000L).build()
        try {
            escuchando = true
            fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
                .addOnFailureListener { fallar("Se perdió el permiso de ubicación. Abre la app para revisarlo.") }
            SeguimientoUbicacion.estadoMutable.value = EstadoSeguimiento(uid = actual)
            notificar("Esperando una posición del dispositivo…")
        } catch (_: SecurityException) {
            fallar("Concede el permiso de ubicación para compartir tu posición.")
        }
    }

    private fun detenerConConsentimiento() {
        if (uid == null) { stopSelf(); return }
        escuchando = false
        fused.removeLocationUpdates(callback)
        notificar("Deteniendo la ubicación compartida…")
        val actual = uid!!
        scope.launch {
            try {
                if (auth.currentUser?.uid == actual) {
                    withTimeoutOrNull(10_000L) {
                        val batch = FirebaseDb.firestore.batch()
                        batch.update(FirebaseDb.firestore.collection("usuarios").document(actual), "compartirUbicacion", false)
                        batch.delete(FirebaseDb.firestore.collection("ubicaciones").document(actual))
                        batch.commit().await()
                    }
                }
            } finally { stopSelf() }
        }
    }

    private fun fallar(mensaje: String) {
        SeguimientoUbicacion.estadoMutable.value = EstadoSeguimiento(error = mensaje)
        stopSelf()
    }

    private fun notificar(texto: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICACION, notificacion(texto))
    }

    private fun notificacion(texto: String): Notification {
        val abrir = PendingIntent.getActivity(this, 11, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val detener = PendingIntent.getService(this, 12,
            Intent(this, SeguimientoUbicacionService::class.java).setAction(DETENER),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CANAL)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("HealthControl · Ubicación compartida")
            .setContentText(texto).setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(abrir).setOngoing(true).setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(android.R.drawable.ic_media_pause, "Dejar de compartir", detener)
            .build()
    }

    override fun onDestroy() {
        escuchando = false
        fused.removeLocationUpdates(callback)
        auth.removeAuthStateListener(authListener)
        consentimiento?.remove()
        posiciones.close()
        scope.cancel()
        val estado = SeguimientoUbicacion.estadoMutable.value
        SeguimientoUbicacion.estadoMutable.value = estado.copy(uid = null)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        private const val CANAL = "ubicacion_compartida"
        private const val NOTIFICACION = 701
        private const val DETENER = "com.icm2630.proyecto.DETENER_UBICACION"
    }
}
