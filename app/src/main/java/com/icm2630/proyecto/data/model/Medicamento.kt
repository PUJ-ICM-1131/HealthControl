    package com.icm2630.proyecto.data.model


    data class Medicamento(
        val id: String,

        /**
         * Si es null, el medicamento pertenece al propio usuario.
         * Si tiene valor, corresponde al id de una Persona asociada
         */
        val personaId: String? = null,
        val nombre: String,
        val forma: FormaMedicamento,
        val dosis: String,
        //mg, mL, gotas
        val unidad: String,
        val cantidadPorToma: String = "",
        //["8:00 AM", "8:00 PM"]
        val horarios: List<String>,
        val fechaInicioMillis: Long,
        //Será null cuando el tratamiento sea permanente.
        val fechaFinMillis: Long? = null,
        val tratamientoPermanente: Boolean,
        val indicaciones: String = "",
        /**
         * Por ahora se guarda únicamente la referencia al archivo
         * seleccionado. En una implementación futura podría apuntar
         * a un archivo almacenado en backend.
         */
        val ordenMedicaUri: String? = null
    )

    enum class FormaMedicamento(
        val titulo: String
    ) {
        PASTILLA("Pastilla"),
        JARABE("Jarabe"),
        GOTAS("Gotas"),
        INYECCION("Inyección")
    }