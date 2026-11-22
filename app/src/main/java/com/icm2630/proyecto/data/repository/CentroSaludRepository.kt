package com.icm2630.proyecto.data.repository

import com.icm2630.proyecto.data.model.PuntoMapa
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class ErrorServicioExterno(message: String) : IOException(message)


class CentrosSaludRepository {

    suspend fun buscarCercanos(token: String, cerca: PuntoMapa): List<PuntoMapa> =
        withContext(Dispatchers.IO) {
            if (!token.startsWith("pk.")) throw ErrorServicioExterno("Configura tu token público de Mapbox.")
            // Primero hospitales y clínicas; si no hay, cualquier servicio de salud.
            for (categoria in CATEGORIAS) {
                val resultados = consultar(token, cerca, categoria)
                if (resultados.isNotEmpty()) return@withContext resultados
            }
            emptyList()
        }

    private suspend fun consultar(token: String, cerca: PuntoMapa, categoria: String): List<PuntoMapa> {
        val conexion = crearUrl(token, cerca, categoria).openConnection() as HttpURLConnection
        return try {
            currentCoroutineContext().ensureActive()
            conexion.requestMethod = "GET"
            conexion.connectTimeout = 10_000
            conexion.readTimeout = 15_000
            conexion.setRequestProperty("Accept", "application/json")

            when (val estado = conexion.responseCode) {
                in 200..299 -> Unit
                // Categoría no reconocida: se prueba con la siguiente.
                400, 404 -> return emptyList()
                401 -> throw ErrorServicioExterno("Mapbox rechazó el token. Revisa que sea un token público válido.")
                403 -> throw ErrorServicioExterno("El token de Mapbox no tiene permiso para la búsqueda de lugares.")
                429 -> throw ErrorServicioExterno("Se alcanzó el límite de consultas de Mapbox. Inténtalo más tarde.")
                else -> throw ErrorServicioExterno("No se pudieron consultar los centros de salud (HTTP $estado).")
            }

            val json = conexion.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            currentCoroutineContext().ensureActive()
            interpretarRespuesta(json, cerca)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ErrorServicioExterno) {
            throw e
        } catch (e: SocketTimeoutException) {
            throw ErrorServicioExterno("La búsqueda de centros de salud tardó demasiado. Inténtalo de nuevo.")
        } catch (e: IOException) {
            throw ErrorServicioExterno("No se pudo conectar con Mapbox. Revisa tu conexión a internet.")
        } finally {
            conexion.disconnect()
        }
    }

    private fun crearUrl(token: String, cerca: PuntoMapa, categoria: String): URL {
        val tokenSeguro = URLEncoder.encode(token, "UTF-8")
        return URL(
            "https://api.mapbox.com/search/searchbox/v1/category/$categoria" +
                    "?proximity=${cerca.longitud},${cerca.latitud}" +
                    "&language=es&limit=$LIMITE&access_token=$tokenSeguro"
        )
    }

    /** Respuesta GeoJSON: features[].properties { name, full_address, coordinates }. */
    internal fun interpretarRespuesta(json: String, cerca: PuntoMapa): List<PuntoMapa> {
        try {
            val features = JSONObject(json).optJSONArray("features") ?: return emptyList()
            val lugares = (0 until features.length()).mapNotNull { indice ->
                val feature = features.getJSONObject(indice)
                val propiedades = feature.optJSONObject("properties") ?: return@mapNotNull null
                val nombre = propiedades.optString("name").ifBlank { return@mapNotNull null }
                // Coordenadas en properties.coordinates o, si faltan, en geometry.coordinates [lng, lat].
                val coordenadas = propiedades.optJSONObject("coordinates")
                val geometria = feature.optJSONObject("geometry")?.optJSONArray("coordinates")
                val latitud = coordenadas?.optDouble("latitude")?.takeIf { !it.isNaN() }
                    ?: geometria?.optDouble(1)?.takeIf { !it.isNaN() }
                    ?: return@mapNotNull null
                val longitud = coordenadas?.optDouble("longitude")?.takeIf { !it.isNaN() }
                    ?: geometria?.optDouble(0)?.takeIf { !it.isNaN() }
                    ?: return@mapNotNull null
                val direccion = propiedades.optString("full_address")
                    .ifBlank { propiedades.optString("place_formatted") }
                runCatching { PuntoMapa(latitud, longitud, nombre) }.getOrNull()?.let { punto ->
                    val distancia = cerca.distanciaMetros(punto)
                    distancia to punto.copy(
                        nombre = buildString {
                            append(nombre).append(" · ").append(formatoDistancia(distancia))
                            if (direccion.isNotBlank()) append(" — ").append(direccion)
                        }
                    )
                }
            }
            return lugares
                .filter { (distancia, _) -> distancia <= RADIO_MAXIMO_METROS }
                .sortedBy { (distancia, _) -> distancia }
                .map { (_, punto) -> punto }
        } catch (e: JSONException) {
            throw ErrorServicioExterno("Mapbox devolvió una respuesta incompleta.")
        }
    }

    private fun formatoDistancia(metros: Double): String =
        if (metros < 1_000) "${metros.toInt()} m"
        else String.format(Locale.getDefault(), "%.1f km", metros / 1_000)

    private companion object {
        val CATEGORIAS = listOf("hospital", "health_services")
        const val LIMITE = 10
        const val RADIO_MAXIMO_METROS = 30_000.0
    }
}