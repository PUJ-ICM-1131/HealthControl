package com.icm2630.proyecto.data.repository

import com.icm2630.proyecto.data.model.ModoRuta
import com.icm2630.proyecto.data.model.PuntoMapa
import com.icm2630.proyecto.data.model.RutaMapa
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class ErrorRuta(message: String) : IOException(message)

/**
 * Consulta HTTPS a Mapbox Directions v5. No usa Google Maps ni un SDK de navegación.
 * El trabajo de red se realiza fuera del hilo de la interfaz.
 */
class RutaRepository {

    suspend fun calcular(
        token: String,
        origen: PuntoMapa,
        destino: PuntoMapa,
        modo: ModoRuta
    ): RutaMapa = withContext(Dispatchers.IO) {
        if (!token.startsWith("pk.")) throw ErrorRuta("Configura tu token público de Mapbox.")
        if (origen.distanciaMetros(destino) < 5.0) {
            throw ErrorRuta("Elige un destino separado del origen por al menos 5 metros.")
        }

        val conexion = crearUrl(token, origen, destino, modo).openConnection() as HttpURLConnection
        try {
            currentCoroutineContext().ensureActive()
            conexion.requestMethod = "GET"
            conexion.connectTimeout = 10_000
            conexion.readTimeout = 15_000
            conexion.setRequestProperty("Accept", "application/json")

            val estado = conexion.responseCode
            if (estado !in 200..299) {
                throw ErrorRuta(
                    when (estado) {
                        401 -> "Mapbox rechazó el token. Revisa que sea un token público válido."
                        403 -> "Mapbox no autorizó la consulta. Revisa los permisos y restricciones del token."
                        429 -> "Se alcanzó el límite de consultas de Mapbox. Inténtalo más tarde."
                        in 500..599 -> "El servicio de rutas no está disponible. Inténtalo de nuevo."
                        else -> "No se pudo consultar la ruta (HTTP $estado)."
                    }
                )
            }

            val json = conexion.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            currentCoroutineContext().ensureActive()
            interpretarRespuesta(json)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ErrorRuta) {
            throw e
        } catch (e: SocketTimeoutException) {
            throw ErrorRuta("La consulta de la ruta tardó demasiado. Inténtalo de nuevo.")
        } catch (e: IOException) {
            throw ErrorRuta("No se pudo conectar con Mapbox. Revisa tu conexión a internet.")
        } finally {
            conexion.disconnect()
        }
    }

    internal fun crearUrl(
        token: String,
        origen: PuntoMapa,
        destino: PuntoMapa,
        modo: ModoRuta
    ): URL {
        val coordenadas = "${origen.longitud},${origen.latitud};${destino.longitud},${destino.latitud}"
        val tokenSeguro = URLEncoder.encode(token, "UTF-8")
        return URL(
            "https://api.mapbox.com/directions/v5/mapbox/${modo.perfilApi}/$coordenadas" +
                    "?geometries=geojson&overview=full&alternatives=false&steps=false&access_token=$tokenSeguro"
        )
    }

    /** Separado de la red para probar respuestas reales y errores sin un token. */
    internal fun interpretarRespuesta(json: String): RutaMapa {
        try {
            val respuesta = JSONObject(json)
            when (respuesta.optString("code")) {
                "Ok" -> Unit
                "NoRoute" -> throw ErrorRuta("No hay una ruta disponible entre esos puntos para el modo elegido.")
                "NoSegment" -> throw ErrorRuta("Uno de los puntos está lejos de una vía. Elige un lugar cercano a una calle o camino.")
                else -> throw ErrorRuta("Mapbox no pudo calcular esa ruta. Revisa los puntos e inténtalo de nuevo.")
            }

            val rutas = respuesta.getJSONArray("routes")
            if (rutas.length() == 0) throw ErrorRuta("No hay una ruta disponible entre esos puntos.")
            val ruta = rutas.getJSONObject(0)
            val geometria = ruta.getJSONObject("geometry")
            require(geometria.getString("type") == "LineString")
            val coordenadas = geometria.getJSONArray("coordinates")
            require(coordenadas.length() >= 2)
            val puntos = List(coordenadas.length()) { indice ->
                val par = coordenadas.getJSONArray(indice)
                PuntoMapa(latitud = par.getDouble(1), longitud = par.getDouble(0))
            }
            val distancia = ruta.getDouble("distance")
            val duracion = ruta.getDouble("duration")
            require(distancia.isFinite() && distancia >= 0.0)
            require(duracion.isFinite() && duracion >= 0.0)
            return RutaMapa(puntos, distancia, duracion)
        } catch (e: JSONException) {
            throw ErrorRuta("Mapbox devolvió una respuesta de ruta incompleta.")
        } catch (e: IllegalArgumentException) {
            throw ErrorRuta("Mapbox devolvió coordenadas o datos de ruta inválidos.")
        }
    }
}