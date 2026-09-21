package com.icm2630.proyecto.data.model


data class Cita(
    val id: String,

     //null = cita del propio usuario.
     //Si tiene valor, corresponde al id de una Persona asociada
    val personaId: String? = null,

    val tipo: TipoCita,
    val especialidad: String = "",
    val motivo: String,
    val fechaMillis: Long,
    val hora: Int,
    val minuto: Int,
    val modalidad: ModalidadCita,

    //Datos principalmente para citas presenciales
    val institucion: String = "",
    val direccion: String = "",

     //Dato principalmente para citas virtuales
    val enlaceVirtual: String = "",

    val nombreMedico: String = "",
    val notas: String = "",

    //URI de una orden, autorización o soporte
    val soporteUri: String? = null
)

enum class TipoCita(
    val titulo: String
) {
    MEDICINA_GENERAL("Medicina general"),
    ESPECIALISTA("Especialista"),
    ODONTOLOGIA("Odontología"),
    EXAMENES("Exámenes")
}

enum class ModalidadCita(
    val titulo: String
) {
    PRESENCIAL("Presencial"),
    VIRTUAL("Virtual")
}