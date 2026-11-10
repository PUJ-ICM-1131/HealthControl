package com.icm2630.proyecto.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.icm2630.proyecto.data.model.*
import com.icm2630.proyecto.data.remote.FirebaseDb
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

object VinculacionRepository {
    private val db get() = FirebaseDb.firestore
    private val invitaciones get() = db.collection("invitaciones")

    fun obtenerPersonasAsociadas(): List<PersonaVinculada> =
        SesionRepository.perfil?.acompanantes.orEmpty()
    fun obtenerPorId(personaId: String): PersonaVinculada? =
        obtenerPersonasAsociadas().find { it.uid == personaId }

    fun observar(uid: String): Flow<List<Invitacion>> = combine(
        invitaciones.whereEqualTo("emisorUid", uid).observar(),
        invitaciones.whereEqualTo("destinatarioUid", uid).observar()
    ) { enviadas, recibidas ->
        (enviadas + recibidas).distinctBy { it.id }
            .sortedByDescending { it.creadaMillis ?: Long.MAX_VALUE }
    }

    suspend fun enviar(uid: String, codigoIngresado: String) {
        val codigo = CodigoInvitacion.normalizar(codigoIngresado)
        require(CodigoInvitacion.esValido(codigo)) { "El código debe tener seis letras o números válidos." }
        val indice = db.collection("codigos").document(codigo).get(Source.SERVER).await()
        require(indice.exists()) { "No existe una cuenta con ese código." }
        val destino = checkNotNull(indice.getString("uid")) { "El código no está disponible." }
        require(destino != uid) { "No puedes enviarte una solicitud a ti mismo." }
        val ref = invitaciones.document("${uid}_${destino}")
        val inversa = invitaciones.document("${destino}_${uid}")
        db.runTransaction { tx ->
            val anterior = tx.get(ref)
            val otra = tx.get(inversa)
            val emisor = tx.get(db.collection("usuarios").document(uid))
            val codigoDoc = tx.get(db.collection("codigos").document(codigo))
            require(codigoDoc.getString("uid") == destino) { "El código cambió. Vuelve a solicitarlo." }
            val estados = listOf(anterior.getString("estado"), otra.getString("estado"))
            require("ACEPTADA" !in estados) { "Ya tienes un vínculo con esta persona." }
            require("PENDIENTE" !in estados) { "Ya hay una solicitud pendiente entre ustedes. Revisa las solicitudes recibidas." }
            tx.set(ref, mapOf(
                "emisorUid" to uid,
                "destinatarioUid" to destino,
                "nombreEmisor" to emisor.getString("nombreCompleto").orEmpty(),
                "nombreDestinatario" to codigoDoc.getString("nombre").orEmpty(),
                "codigoDestino" to codigo,
                "estado" to EstadoInvitacion.PENDIENTE.name,
                "creadaEn" to FieldValue.serverTimestamp(),
                "actualizadaEn" to FieldValue.serverTimestamp()
            ))
        }.await()
    }

    suspend fun responder(uid: String, invitacion: Invitacion, aceptar: Boolean) {
        require(invitacion.destinatarioUid == uid) { "Solo el destinatario puede responder." }
        cambiarEstado(invitacion.id, uid, EstadoInvitacion.PENDIENTE,
            if (aceptar) EstadoInvitacion.ACEPTADA else EstadoInvitacion.RECHAZADA)
    }

    suspend fun cancelar(uid: String, invitacion: Invitacion) {
        require(invitacion.emisorUid == uid) { "Solo el emisor puede cancelar." }
        cambiarEstado(invitacion.id, uid, EstadoInvitacion.PENDIENTE, EstadoInvitacion.CANCELADA)
    }

    suspend fun desvincular(uid: String, invitacion: Invitacion) =
        cambiarEstado(invitacion.id, uid, EstadoInvitacion.ACEPTADA, EstadoInvitacion.REVOCADA)

    private suspend fun cambiarEstado(id: String, uid: String, esperado: EstadoInvitacion, nuevo: EstadoInvitacion) {
        val ref = invitaciones.document(id)
        db.runTransaction { tx ->
            val doc = tx.get(ref)
            require(uid == doc.getString("emisorUid") || uid == doc.getString("destinatarioUid")) { "La solicitud no pertenece a esta cuenta." }
            require(doc.getString("estado") == esperado.name) { "La solicitud ya cambió. Actualiza la pantalla." }
            tx.update(ref, mapOf("estado" to nuevo.name, "actualizadaEn" to FieldValue.serverTimestamp()))
        }.await()
    }

    private fun Query.observar(): Flow<List<Invitacion>> = callbackFlow {
        val listener = addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { result, error ->
            if (error != null) close(error)
            else if (result?.metadata?.hasPendingWrites() != true) trySend(result?.documents.orEmpty().mapNotNull { it.aInvitacion() })
        }
        awaitClose { listener.remove() }
    }

    private fun DocumentSnapshot.aInvitacion(): Invitacion? {
        val emisor = getString("emisorUid") ?: return null
        val destinatario = getString("destinatarioUid") ?: return null
        val estado = EstadoInvitacion.entries.find { it.name == getString("estado") } ?: return null
        return Invitacion(id, emisor, destinatario, getString("nombreEmisor").orEmpty(),
            getString("nombreDestinatario").orEmpty(), estado, getTimestamp("creadaEn")?.toDate()?.time)
    }
}
