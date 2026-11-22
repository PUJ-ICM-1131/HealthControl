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
                    trySend(result?.documents.orEmpty().mapNotNull { doc ->
                        doc.toObject(Cita::class.java)?.copy(id = doc.id,
                            soporteUri = if (SesionRepository.haySesionGuardada && SesionRepository.puedeGestionar(uid))
                                AdjuntosLocales.obtener(uid, "cita", doc.id) else null)
                    })
                } catch (e: Exception) { close(e) }
            }
        }
        awaitClose { listener.remove() }
    }

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
                tx.set(ref, cita)
                tx.update(ref, "auditoria", auditoria)
            }.await()
        } catch (e: Exception) {
            AdjuntosLocales.guardar(uid, "cita", cita.id, anterior)
            throw e
        }
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
