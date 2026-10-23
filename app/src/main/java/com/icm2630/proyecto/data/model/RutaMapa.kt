package com.icm2630.proyecto.data.model

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Coordenada propia de la aplicación. Mapbox recibe longitud primero. */
data class PuntoMapa(
    val latitud: Double,
    val longitud: Double,
    val nombre: String = "Punto seleccionado"
) {
    init {
        require(latitud.isFinite() && latitud in -90.0..90.0) { "Latitud inválida" }
        require(longitud.isFinite() && longitud in -180.0..180.0) { "Longitud inválida" }
    }

    fun distanciaMetros(otro: PuntoMapa): Double {
        val lat1 = Math.toRadians(latitud)
        val lat2 = Math.toRadians(otro.latitud)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(otro.longitud - longitud)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        return 6_371_000.0 * 2 * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }
}

enum class ModoRuta(val titulo: String, val perfilApi: String) {
    CAMINANDO("A pie", "walking"),
    AUTOMOVIL("En carro", "driving")
}

/** Resultado de Directions: los puntos ya siguen las calles/caminos de la ruta. */
data class RutaMapa(
    val puntos: List<PuntoMapa>,
    val distanciaMetros: Double,
    val duracionSegundos: Double
)
