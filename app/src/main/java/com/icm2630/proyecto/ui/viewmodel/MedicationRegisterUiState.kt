package com.icm2630.proyecto.ui.viewmodel

import com.icm2630.proyecto.data.model.FormaMedicamento
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.Persona
import com.icm2630.proyecto.data.model.TipoPerfil


data class MedicationRegisterUiState(

    // =========================================================
    // PERFIL Y PERSONA
    // =========================================================

    /**
     * Perfil del usuario que actualmente está utilizando la app.
     */
    val perfilUsuario: PerfilUsuario? = null,

    /**
     * Personas que puede supervisar cuando el usuario
     * tiene un perfil ASOCIADO.
     */
    val personasAsociadas: List<Persona> = emptyList(),

    /**
     * null = medicamento para el propio usuario.
     * id   = medicamento para una persona asociada.
     */
    val personaSeleccionadaId: String? = null,


    // =========================================================
    // MEDICAMENTO
    // =========================================================

    val nombre: String = "",

    val forma: FormaMedicamento =
        FormaMedicamento.PASTILLA,


    // =========================================================
    // DOSIS
    // =========================================================

    val dosis: String = "",

    val unidad: String = "mg",

    val cantidadPorToma: String = "1",


    // =========================================================
    // HORARIOS
    // =========================================================

    val horarios: List<String> = emptyList(),


    // =========================================================
    // DURACIÓN
    // =========================================================

    val fechaInicioMillis: Long? = null,

    val fechaFinMillis: Long? = null,

    val tratamientoPermanente: Boolean = false,


    // =========================================================
    // INFORMACIÓN ADICIONAL
    // =========================================================

    val indicaciones: String = "",

    val ordenMedicaUri: String? = null,

    val nombreOrdenMedica: String? = null,


    // =========================================================
    // ESTADO DE LA OPERACIÓN
    // =========================================================

    val guardando: Boolean = false,

    val guardadoExitoso: Boolean = false,

    val mensajeError: String? = null
) {

    // =========================================================
    // PERSONA SELECCIONADA
    // =========================================================

    val personaSeleccionada: Persona?
        get() = personasAsociadas.find { persona ->
            persona.id == personaSeleccionadaId
        }


    // =========================================================
    // VALIDACIÓN DE FECHA FINAL
    // =========================================================

    val fechaFinInvalida: Boolean
        get() {

            val inicio = fechaInicioMillis
            val fin = fechaFinMillis

            return !tratamientoPermanente &&
                    inicio != null &&
                    fin != null &&
                    fin < inicio
        }


    // =========================================================
    // VALIDACIÓN GENERAL
    // =========================================================

    val formularioValido: Boolean
        get() {

            val personaValida =
                when (perfilUsuario?.tipoPerfil) {

                    TipoPerfil.TITULAR ->
                        true

                    TipoPerfil.ACOMPANANTE ->
                        personaSeleccionadaId != null

                    null ->
                        false
                }


            val duracionValida =
                if (tratamientoPermanente) {

                    fechaInicioMillis != null

                } else {

                    fechaInicioMillis != null &&
                            fechaFinMillis != null &&
                            !fechaFinInvalida
                }


            return personaValida &&
                    nombre.isNotBlank() &&
                    dosis.isNotBlank() &&
                    horarios.isNotEmpty() &&
                    duracionValida
        }
}