package com.icm2630.proyecto.data.model


data class Medicamento(
    val id: String = "",

    // Compatibilidad: el titular lo determina usuarios/{uid}/medicamentos, no personaId.
    val personaId: String? = null,
    val nombre: String = "",
    val forma: FormaMedicamento = FormaMedicamento.PASTILLA,
    val dosis: String = "",
    //mg, mL, gotas
    val unidad: String = "",
    val cantidadPorToma: String = "",
    //["8:00 AM", "8:00 PM"]
    val horarios: List<String> = emptyList(),
    val fechaInicioMillis: Long = 0,
    //Será null cuando el tratamiento sea permanente.
    val fechaFinMillis: Long? = null,
    val tratamientoPermanente: Boolean = false,
    val indicaciones: String = "",
    /**
     * Referencia al archivo seleccionado (imagen o PDF).
     */
    @get:com.google.firebase.firestore.Exclude
    val ordenMedicaUri: String? = null,
    val nombreOrdenMedica: String? = null,
    val auditoria: AuditoriaRegistro? = null
)

enum class FormaMedicamento(
    val titulo: String
) {
    PASTILLA("Pastilla"),
    JARABE("Jarabe"),
    GOTAS("Gotas"),
    INYECCION("Inyección")
}
