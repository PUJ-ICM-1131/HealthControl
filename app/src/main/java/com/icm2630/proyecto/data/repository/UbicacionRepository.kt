package com.icm2630.proyecto.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.icm2630.proyecto.data.model.PuntoMapa
import com.icm2630.proyecto.data.remote.FirebaseDb
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class UbicacionCompartida(val punto: PuntoMapa, val momentoMillis: Long?)

object UbicacionRepository {
    suspend fun guardar(uid: String, punto: PuntoMapa) {
        FirebaseDb.firestore.collection("ubicaciones").document(uid).set(mapOf(
            "punto" to GeoPoint(punto.latitud, punto.longitud),
            "actualizadoEn" to FieldValue.serverTimestamp()
        )).await()
    }

    fun observar(uid: String, nombre: String) = callbackFlow<UbicacionCompartida?> {
        val listener = FirebaseDb.firestore.collection("ubicaciones").document(uid)
            .addSnapshotListener { doc, error ->
                if (error != null) close(error)
                else {
                    val punto = doc?.getGeoPoint("punto")
                    trySend(punto?.let { UbicacionCompartida(
                        PuntoMapa(it.latitude, it.longitude, nombre),
                        doc.getTimestamp("actualizadoEn")?.toDate()?.time
                    ) })
                }
            }
        awaitClose { listener.remove() }
    }
}
