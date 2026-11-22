package com.icm2630.proyecto.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.icm2630.proyecto.data.model.Medicamento
import com.icm2630.proyecto.data.remote.FirebaseDb
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object MedicamentoRepository {
    private var porCuenta by mutableStateOf<Map<String, List<Medicamento>>>(emptyMap())
    private fun coleccion(uid: String) = FirebaseDb.firestore.collection("usuarios").document(uid).collection("medicamentos")
    private fun uidConsultado(): String? = if (!SesionRepository.haySesionGuardada) null
        else SesionRepository.familiarActivo?.uid ?: SesionRepository.uidActual

    fun observar(uid: String) = callbackFlow<List<Medicamento>> {
        val listener = coleccion(uid).addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { result, error ->
            if (error != null) close(error)
            else {
                if (result?.metadata?.hasPendingWrites() == true) return@addSnapshotListener
                try {
                    trySend(result?.documents.orEmpty().mapNotNull { doc ->
                        doc.toObject(Medicamento::class.java)?.copy(id = doc.id,
                            ordenMedicaUri = if (SesionRepository.haySesionGuardada && SesionRepository.puedeGestionar(uid))
                                AdjuntosLocales.obtener(uid, "medicamento", doc.id) else null)
                    })
                } catch (e: Exception) { close(e) }
            }
        }
        awaitClose { listener.remove() }
    }

    internal fun actualizarCache(uid: String, medicamentos: List<Medicamento>) { porCuenta = porCuenta + (uid to medicamentos) }
    internal fun conservarCuentas(uids: Set<String>) { porCuenta = porCuenta.filterKeys { it in uids } }
    fun limpiar() { porCuenta = emptyMap() }

    suspend fun registrar(medicamento: Medicamento, uid: String = SesionRepository.uidConsultado) = guardar(medicamento, uid)
    suspend fun actualizar(medicamento: Medicamento, uid: String = SesionRepository.uidConsultado) = guardar(medicamento, uid)
    private suspend fun guardar(medicamento: Medicamento, uid: String) {
        check(SesionRepository.puedeGestionar(uid)) { "No tienes permiso para gestionar los registros de esta persona." }
        require(medicamento.personaId == null) { "El titular se identifica por su cuenta, no por personaId." }
        val anterior = AdjuntosLocales.obtener(uid, "medicamento", medicamento.id)
        val adjunto = AdjuntosLocales.conservar(medicamento.ordenMedicaUri, medicamento.nombreOrdenMedica)
        AdjuntosLocales.guardar(uid, "medicamento", medicamento.id, adjunto)
        try {
            val ref = coleccion(uid).document(medicamento.id)
            FirebaseDb.firestore.runTransaction { tx ->
                val anterior = tx.get(ref)
                val auditoria = AuditoriaRegistros.paraGuardar(anterior)
                tx.set(ref, medicamento)
                tx.update(ref, "auditoria", auditoria)
            }.await()
        } catch (e: Exception) {
            AdjuntosLocales.guardar(uid, "medicamento", medicamento.id, anterior)
            throw e
        }
    }

    fun obtenerTodos(): List<Medicamento> = porCuenta[uidConsultado()].orEmpty()
    fun obtenerPropios(): List<Medicamento> = obtenerTodos()
    fun obtenerPorPersona(personaId: String): List<Medicamento> = porCuenta[personaId].orEmpty()
    fun obtenerPorId(medicamentoId: String, uid: String? = uidConsultado()): Medicamento? =
        porCuenta[uid].orEmpty().find { it.id == medicamentoId }

    suspend fun eliminar(medicamentoId: String, uid: String = SesionRepository.uidConsultado) {
        check(SesionRepository.haySesionGuardada && uid == SesionRepository.uidActual) {
            "Solo el titular puede eliminar sus registros."
        }
        coleccion(uid).document(medicamentoId).delete().await()
        AdjuntosLocales.eliminar(uid, "medicamento", medicamentoId)
    }
}
