package com.icm2630.proyecto.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.icm2630.proyecto.data.model.PuntoMapa
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/** Convierte una dirección escrita en posibles puntos del mapa. */
class GeocodingRepository(context: Context) {
    private val geocoder = Geocoder(context.applicationContext, Locale.getDefault())

    suspend fun buscar(consulta: String): List<PuntoMapa> = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) {
            throw IOException("Este dispositivo no tiene búsqueda de direcciones. Elige el punto en el mapa.")
        }
        try {
            val direcciones: List<Address> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine<List<Address>> { continuation ->
                    geocoder.getFromLocationName(consulta, 5, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) continuation.resume(addresses.toList())
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(
                                    IOException("No se pudo buscar esa dirección. Inténtalo de nuevo o elige el punto en el mapa.")
                                )
                            }
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(consulta, 5).orEmpty()
            }
            direcciones.filter { it.hasLatitude() && it.hasLongitude() }
                .map { direccion ->
                    PuntoMapa(
                        latitud = direccion.latitude,
                        longitud = direccion.longitude,
                        nombre = direccion.getAddressLine(0)
                            ?: direccion.featureName
                            ?: consulta
                    )
                }
                .distinctBy { it.latitud to it.longitud }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            throw IOException("No se pudo buscar la dirección. Revisa internet o elige el punto en el mapa.")
        } catch (e: IllegalArgumentException) {
            throw IOException("Escribe una dirección válida e incluye la ciudad.")
        }
    }
}