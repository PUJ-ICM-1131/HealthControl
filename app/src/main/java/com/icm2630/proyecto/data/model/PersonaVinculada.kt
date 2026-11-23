package com.icm2630.proyecto.data.model

/** Permisos derivados de una invitación aceptada; el servidor vuelve a verificarlos al escribir. */
data class PersonaVinculada(
    val nombre: String,
    val relacion: String,
    val codigo: String,
    val uid: String = "",
    val telefono: String = "",
    val puedeGestionar: Boolean = false,
    val gestionaMiSalud: Boolean = false
) {
    val id: String get() = uid
    val nombreCompleto: String get() = nombre
}
