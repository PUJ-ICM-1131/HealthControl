package com.icm2630.proyecto.data.model


data class Cita(
    val id: String = "",

    // Compatibilidad con registros anteriores. El titular lo determina usuarios/{uid}/citas.
    // Los nuevos registros conservan personaId = null.
    val personaId: String? = null,

    val tipo: TipoCita = TipoCita.MEDICINA_GENERAL,
    val especialidad: String = "",
    val motivo: String = "",
    val fechaMillis: Long = 0,
    val hora: Int = 0,
    val minuto: Int = 0,
    val modalidad: ModalidadCita = ModalidadCita.PRESENCIAL,

    //Datos principalmente para citas presenciales
    val institucion: String = "",
    val direccion: String = "",

     //Dato principalmente para citas virtuales
    val enlaceVirtual: String = "",

    val nombreMedico: String = "",
    val notas: String = "",

    //URI de una orden, autorización o soporte
    @get:com.google.firebase.firestore.Exclude
    val soporteUri: String? = null,
    val nombreSoporte: String? = null,
    val auditoria: AuditoriaRegistro? = null,
    val ubicacionConfirmada: com.google.firebase.firestore.GeoPoint? = null,
    val nombreLugarConfirmado: String? = null
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
