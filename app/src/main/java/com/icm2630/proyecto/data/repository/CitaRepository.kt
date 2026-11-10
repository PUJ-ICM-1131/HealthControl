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
                            soporteUri = if (SesionRepository.haySesionGuardada && SesionRepository.uidActual == uid)
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

    suspend fun registrar(cita: Cita) = guardar(cita)
    suspend fun actualizar(cita: Cita) = guardar(cita)
    private suspend fun guardar(cita: Cita) {
        check(SesionRepository.familiarActivo == null) { "El perfil compartido es de solo lectura." }
        val uid = SesionRepository.uidActual
        require(cita.personaId == null) { "Registra la cita desde la cuenta de su titular." }
        AdjuntosLocales.guardar(uid, "cita", cita.id, cita.soporteUri)
        coleccion(uid).document(cita.id).set(cita).await()
    }

    fun obtenerTodas(): List<Cita> = porCuenta[uidConsultado()].orEmpty()
    fun obtenerPropias(): List<Cita> = obtenerTodas()
    fun obtenerPorPersona(personaId: String): List<Cita> = porCuenta[personaId].orEmpty()
    fun obtenerPorId(citaId: String): Cita? = obtenerTodas().find { it.id == citaId }

    suspend fun eliminar(citaId: String) {
        check(SesionRepository.familiarActivo == null) { "El perfil compartido es de solo lectura." }
        val uid = SesionRepository.uidActual
        coleccion(uid).document(citaId).delete().await()
        AdjuntosLocales.eliminar(uid, "cita", citaId)
    }
}
