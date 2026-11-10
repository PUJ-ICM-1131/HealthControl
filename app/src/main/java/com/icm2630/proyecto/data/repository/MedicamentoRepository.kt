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
                            ordenMedicaUri = if (SesionRepository.haySesionGuardada && SesionRepository.uidActual == uid)
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

    suspend fun registrar(medicamento: Medicamento) = guardar(medicamento)
    suspend fun actualizar(medicamento: Medicamento) = guardar(medicamento)
    private suspend fun guardar(medicamento: Medicamento) {
        check(SesionRepository.familiarActivo == null) { "El perfil compartido es de solo lectura." }
        val uid = SesionRepository.uidActual
        require(medicamento.personaId == null) { "Registra el medicamento desde la cuenta de su titular." }
        AdjuntosLocales.guardar(uid, "medicamento", medicamento.id, medicamento.ordenMedicaUri)
        coleccion(uid).document(medicamento.id).set(medicamento).await()
    }

    fun obtenerTodos(): List<Medicamento> = porCuenta[uidConsultado()].orEmpty()
    fun obtenerPropios(): List<Medicamento> = obtenerTodos()
    fun obtenerPorPersona(personaId: String): List<Medicamento> = porCuenta[personaId].orEmpty()
    fun obtenerPorId(medicamentoId: String): Medicamento? = obtenerTodos().find { it.id == medicamentoId }

    suspend fun eliminar(medicamentoId: String) {
        check(SesionRepository.familiarActivo == null) { "El perfil compartido es de solo lectura." }
        val uid = SesionRepository.uidActual
        coleccion(uid).document(medicamentoId).delete().await()
        AdjuntosLocales.eliminar(uid, "medicamento", medicamentoId)
    }
}
