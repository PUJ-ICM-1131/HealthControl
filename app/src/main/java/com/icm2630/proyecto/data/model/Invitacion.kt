package com.icm2630.proyecto.data.model

enum class EstadoInvitacion { PENDIENTE, ACEPTADA, RECHAZADA, CANCELADA, REVOCADA }

/** Los vínculos antiguos conservan consulta; la gestión exige consentimiento del titular. */
data class Invitacion(
    val id: String,
    val emisorUid: String,
    val destinatarioUid: String,
    val nombreEmisor: String,
    val nombreDestinatario: String,
    val estado: EstadoInvitacion,
    val creadaMillis: Long? = null,
    val solicitaGestion: Boolean = false,
    val titularGestionUid: String = ""
) {
    fun otroUid(miUid: String) = if (emisorUid == miUid) destinatarioUid else emisorUid
    fun nombreDelOtro(miUid: String) = if (emisorUid == miUid) nombreDestinatario else nombreEmisor
    fun permiteGestionar(cuidadorUid: String, titularUid: String): Boolean =
        estado == EstadoInvitacion.ACEPTADA && titularGestionUid == titularUid &&
            titularUid in listOf(emisorUid, destinatarioUid) && otroUid(titularUid) == cuidadorUid
}
