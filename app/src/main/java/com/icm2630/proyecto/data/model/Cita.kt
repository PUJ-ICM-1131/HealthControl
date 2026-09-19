package com.icm2630.proyecto.data.model


data class Cita(
    val id: String,

    /**
     * null = cita del propio usuario.
     * Si tiene valor, corresponde al id de una Persona asociada.
     */
    val personaId: String? = null,

    val tipo: TipoCita,

    /**
     * Ejemplo:
     * Cardiología, Dermatología, Pediatría...
     *
     * Puede quedar vacío para medicina general,
     * odontología o exámenes si no se necesita.
     */
    val especialidad: String = "",

    val motivo: String,

    /**
     * Día seleccionado en el calendario.
     */
    val fechaMillis: Long,

    /**
     * Guardamos hora y minuto por separado para no depender
     * de un String como "10:30 AM".
     */
    val hora: Int,
    val minuto: Int,

    val modalidad: ModalidadCita,

    //Datos principalmente para citas presenciales.
    val institucion: String = "",
    val direccion: String = "",

     //Dato principalmente para citas virtuales
    val enlaceVirtual: String = "",

    val nombreMedico: String = "",

    val notas: String = "",

    /**
     * URI de una orden, autorización o soporte.
     */
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