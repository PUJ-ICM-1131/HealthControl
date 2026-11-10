package com.icm2630.proyecto.data.model

/** Cuenta vinculada por invitación. El UID identifica a la persona; el código solo permite invitar. */
data class PersonaVinculada(
    val nombre: String,
    val relacion: String,
    val codigo: String,
    val uid: String,
    val telefono: String = ""
) {
    val id: String get() = uid
    val nombreCompleto: String get() = nombre
}
