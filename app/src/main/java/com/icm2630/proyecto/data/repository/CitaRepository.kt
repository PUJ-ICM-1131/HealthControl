package com.icm2630.proyecto.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.icm2630.proyecto.data.model.Cita
import com.icm2630.proyecto.data.remote.FirebaseDb
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object CitaRepository {
    private var porCuenta by mutableStateOf<Map<String, List<Cita>>>(emptyMap())
    private fun coleccion(uid: String) = FirebaseDb.firestore.collection("usuarios").document(uid).collection("citas")
    private fun uidConsultado(): String? = if (!SesionRepository.haySesionGuardada) null
        else SesionRepository.familiarActivo?.uid ?: SesionRepository.uidActual

    fun observar(uid: String) = callbackFlow<List<Cita>> {
        val listener = coleccion(uid).addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { result, error ->
            if (error != null) close(error)
            else {
                if (result?.metadata?.hasPendingWrites() == true) return@addSnapshotListener
                try {
                    trySend(result?.documents.orEmpty().mapNotNull { doc -> doc.aCita(uid) })
                } catch (e: Exception) { close(e) }
            }
        }
        awaitClose { listener.remove() }
    }

    fun observarPorId(uid: String, id: String) = callbackFlow<Cita?> {
        val listener = coleccion(uid).document(id).addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { doc, error ->
            if (error != null) close(error)
            else if (doc?.metadata?.hasPendingWrites() != true) {
                try { trySend(doc?.aCita(uid)) } catch (e: Exception) { close(e) }
            }
        }
        awaitClose { listener.remove() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.aCita(uid: String): Cita? =
        toObject(Cita::class.java)?.copy(id = id,
            soporteUri = if (SesionRepository.haySesionGuardada && SesionRepository.puedeGestionar(uid))
                AdjuntosLocales.obtener(uid, "cita", id) else null)

    internal fun actualizarCache(uid: String, citas: List<Cita>) { porCuenta = porCuenta + (uid to citas) }
    internal fun conservarCuentas(uids: Set<String>) { porCuenta = porCuenta.filterKeys { it in uids } }
    fun limpiar() { porCuenta = emptyMap() }

    suspend fun registrar(cita: Cita, uid: String = SesionRepository.uidConsultado) = guardar(cita, uid)
    suspend fun actualizar(cita: Cita, uid: String = SesionRepository.uidConsultado) = guardar(cita, uid)
    private suspend fun guardar(cita: Cita, uid: String) {
        check(SesionRepository.puedeGestionar(uid)) { "No tienes permiso para gestionar los registros de esta persona." }
        require(cita.personaId == null) { "El titular se identifica por su cuenta, no por personaId." }
        val anterior = AdjuntosLocales.obtener(uid, "cita", cita.id)
        val adjunto = AdjuntosLocales.conservar(cita.soporteUri, cita.nombreSoporte)
        AdjuntosLocales.guardar(uid, "cita", cita.id, adjunto)
        try {
            val ref = coleccion(uid).document(cita.id)
            FirebaseDb.firestore.runTransaction { tx ->
                val anterior = tx.get(ref)
                val auditoria = AuditoriaRegistros.paraGuardar(anterior)
                val paraGuardar = com.icm2630.proyecto.data.model.UbicacionCitas.conservar(
                    cita, anterior.toObject(Cita::class.java))
                tx.set(ref, paraGuardar)
                tx.update(ref, "auditoria", auditoria)
            }.await()
        } catch (e: Exception) {
            AdjuntosLocales.guardar(uid, "cita", cita.id, anterior)
            throw e
        }
    }

    suspend fun confirmarUbicacion(uid: String, esperada: Cita, punto: com.icm2630.proyecto.data.model.PuntoMapa) {
        check(SesionRepository.puedeGestionar(uid)) { "Solo el titular o un cuidador autorizado puede guardar el lugar." }
        val ref = coleccion(uid).document(esperada.id)
        FirebaseDb.firestore.runTransaction { tx ->
            val doc = tx.get(ref)
            val actual = checkNotNull(doc.toObject(Cita::class.java)) { "La cita ya no está disponible." }
            check(com.icm2630.proyecto.data.model.UbicacionCitas.mismoLugar(actual, esperada)) {
                "La dirección de la cita cambió. Revisa el lugar y confírmalo de nuevo."
            }
            tx.update(ref, mapOf(
                "ubicacionConfirmada" to com.google.firebase.firestore.GeoPoint(punto.latitud, punto.longitud),
                "nombreLugarConfirmado" to punto.nombre.trim().ifBlank { "Lugar confirmado" }.take(500),
                "auditoria" to AuditoriaRegistros.paraGuardar(doc),
                // Las versiones antiguas serializaban esta propiedad calculada, ajena al esquema actual.
                "cuentaCodigo" to com.google.firebase.firestore.FieldValue.delete()
            ))
        }.await()
    }

    fun obtenerTodas(): List<Cita> = porCuenta[uidConsultado()].orEmpty()
    fun obtenerPropias(): List<Cita> = obtenerTodas()
    fun obtenerPorPersona(personaId: String): List<Cita> = porCuenta[personaId].orEmpty()
    fun obtenerPorId(citaId: String, uid: String? = uidConsultado()): Cita? =
        porCuenta[uid].orEmpty().find { it.id == citaId }

    suspend fun eliminar(citaId: String, uid: String = SesionRepository.uidConsultado) {
        check(SesionRepository.haySesionGuardada && uid == SesionRepository.uidActual) {
            "Solo el titular puede eliminar sus registros."
        }
        coleccion(uid).document(citaId).delete().await()
        AdjuntosLocales.eliminar(uid, "cita", citaId)
    }
}
