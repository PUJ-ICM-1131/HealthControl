package com.icm2630.proyecto.data.model

enum class EstadoInvitacion { PENDIENTE, ACEPTADA, RECHAZADA, CANCELADA, REVOCADA }

/** Una solicitud autoriza lectura mutua solo cuando el destinatario la acepta. */
data class Invitacion(
    val id: String,
    val emisorUid: String,
    val destinatarioUid: String,
    val nombreEmisor: String,
    val nombreDestinatario: String,
    val estado: EstadoInvitacion,
    val creadaMillis: Long? = null
) {
    fun otroUid(miUid: String) = if (emisorUid == miUid) destinatarioUid else emisorUid
    fun nombreDelOtro(miUid: String) = if (emisorUid == miUid) nombreDestinatario else nombreEmisor
}
