package com.icm2630.proyecto.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.MetadataChanges
import com.icm2630.proyecto.data.model.*
import com.icm2630.proyecto.data.remote.FirebaseDb
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object TomaRepository {
    private var porCuenta by mutableStateOf<Map<String, List<TomaMedicamento>>>(emptyMap())
    private var errores by mutableStateOf<Set<String>>(emptySet())
    private fun coleccion(uid: String) = FirebaseDb.firestore.collection("usuarios").document(uid).collection("tomas")
    fun cuentaConsultada(): String? = if (!SesionRepository.haySesionGuardada) null
        else SesionRepository.familiarActivo?.uid ?: SesionRepository.uidActual

    fun observar(uid: String) = callbackFlow<List<TomaMedicamento>> {
        val listener = coleccion(uid).addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) close(error)
            else if (snapshot != null && !snapshot.metadata.hasPendingWrites()) {
                try {
                    trySend(snapshot.documents.mapNotNull { doc ->
                        val registrada = doc.getTimestamp("registradaEn") ?: return@mapNotNull null
                        TomaMedicamento(
                            medicamentoId = checkNotNull(doc.getString("medicamentoId")),
                            fechaMillis = checkNotNull(doc.getLong("fechaMillis")),
                            horario = checkNotNull(doc.getString("horario")),
                            registradaEnMillis = registrada.toDate().time
                        )
                    })
                } catch (e: Exception) { close(e) }
            }
        }
        awaitClose { listener.remove() }
    }

    internal fun actualizarCache(uid: String, tomas: List<TomaMedicamento>) {
        porCuenta = porCuenta + (uid to tomas)
        errores = errores - uid
    }
    internal fun fallo(uid: String) { porCuenta = porCuenta - uid; errores = errores + uid }
    internal fun conservarCuentas(uids: Set<String>) {
        porCuenta = porCuenta.filterKeys { it in uids }
        errores = errores.intersect(uids)
    }
    fun limpiar() { porCuenta = emptyMap(); errores = emptySet() }
    fun cargado(uid: String?) = uid != null && uid in porCuenta
    fun falloCarga(uid: String?) = uid in errores
    fun obtener(uid: String? = cuentaConsultada()) = porCuenta[uid].orEmpty()
    fun claves(uid: String? = cuentaConsultada()) = obtener(uid).map { it.clave }.toSet()

    suspend fun cambiar(uid: String, toma: TomaProgramada, tomada: Boolean) {
        check(SesionRepository.haySesionGuardada && SesionRepository.uidActual == uid &&
            SesionRepository.familiarActivo == null) { "Solo el titular puede confirmar sus tomas." }
        require(TomasProgramadas.esDeHoy(toma.medicamento, toma.fechaMillis, toma.horario, FechasPendientes.hoy())) {
            "Solo puedes registrar o deshacer las tomas de hoy."
        }
        val ref = coleccion(uid).document(toma.clave)
        FirebaseDb.firestore.runTransaction { tx ->
            val existente = tx.get(ref)
            if (tomada && !existente.exists()) {
                tx.set(ref, mapOf(
                    "medicamentoId" to toma.medicamento.id,
                    "fechaMillis" to toma.fechaMillis,
                    "horario" to toma.horario,
                    "registradaEn" to FieldValue.serverTimestamp()
                ))
            } else if (!tomada && existente.exists()) tx.delete(ref)
            Unit
        }.await()
    }
}
