package com.icm2630.proyecto.ui.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * Patrón de la sesión 9: FusedLocationProviderClient + LocationRequest + callback.
 * Además de onDispose, pausa las actualizaciones cuando la pantalla pierde el foco.
 */
@SuppressLint("MissingPermission") // Se verifica justo antes de cada solicitud.
@Composable
fun LocationUpdatesEffect(
    habilitado: Boolean,
    permisoPreciso: Boolean,
    reintento: Int,
    onUbicacion: (Location) -> Unit,
    onEstadoServicio: (Boolean) -> Unit,
    onError: (String?) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val ubicacionActualizada by rememberUpdatedState(onUbicacion)
    val servicioActualizado by rememberUpdatedState(onEstadoServicio)
    val errorActualizado by rememberUpdatedState(onError)

    DisposableEffect(context, lifecycleOwner, habilitado, permisoPreciso, reintento) {
        val cliente = LocationServices.getFusedLocationProviderClient(context)
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var suscrito = false
        var cerrado = false

        fun entregar(location: Location) {
            if (cerrado || !suscrito) return
            if (!location.latitude.isFinite() || location.latitude !in -90.0..90.0 ||
                !location.longitude.isFinite() || location.longitude !in -180.0..180.0
            ) return
            if (System.currentTimeMillis() - location.time > 90_000L) return
            errorActualizado(null)
            ubicacionActualizada(location)
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(resultado: LocationResult) {
                resultado.lastLocation?.let(::entregar)
            }
        }

        fun detener() {
            suscrito = false
            cliente.removeLocationUpdates(callback)
        }

        fun actualizarSuscripcion() {
            if (cerrado) return
            val servicioActivo = LocationManagerCompat.isLocationEnabled(locationManager)
            servicioActualizado(servicioActivo)
            val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
            val enPantalla = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if (!habilitado || !permiso || !servicioActivo || !enPantalla) {
                detener()
                return
            }
            if (suscrito) return

            val solicitud = LocationRequest.Builder(
                if (permisoPreciso) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                5_000L
            )
                .setMinUpdateIntervalMillis(2_000L)
                .setWaitForAccurateLocation(false)
                .build()
            suscrito = true
            try {
                errorActualizado(null)
                cliente.lastLocation.addOnSuccessListener { it?.let(::entregar) }
                cliente.requestLocationUpdates(solicitud, callback, Looper.getMainLooper())
                    .addOnSuccessListener {
                        if (cerrado || !suscrito) cliente.removeLocationUpdates(callback)
                    }
                    .addOnFailureListener {
                        if (!cerrado && suscrito) {
                            suscrito = false
                            errorActualizado("No se pudo obtener la ubicación. Reintenta o elige el origen en el mapa.")
                        }
                    }
            } catch (e: SecurityException) {
                detener()
                errorActualizado("El permiso de ubicación cambió. Vuelve a permitirlo desde los ajustes.")
            }
        }

        val observador = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> actualizarSuscripcion()
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> detener()
                else -> Unit
            }
        }
        val receptor = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) = actualizarSuscripcion()
        }
        val filtro = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(LocationManager.MODE_CHANGED_ACTION)
        }
        ContextCompat.registerReceiver(context, receptor, filtro, ContextCompat.RECEIVER_NOT_EXPORTED)
        lifecycleOwner.lifecycle.addObserver(observador)
        actualizarSuscripcion()

        onDispose {
            cerrado = true
            detener()
            lifecycleOwner.lifecycle.removeObserver(observador)
            context.unregisterReceiver(receptor)
        }
    }
}