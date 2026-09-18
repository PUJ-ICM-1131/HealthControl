package com.icm2630.proyecto.data.model


data class Persona(
    val id: String,
    val nombre: String,
    val apellido: String
) {

    val nombreCompleto: String
        get() = "$nombre $apellido"
}