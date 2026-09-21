package com.icm2630.proyecto.ui.viewmodel

import com.icm2630.proyecto.data.model.ModalidadCita
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.Persona
import com.icm2630.proyecto.data.model.TipoCita
import com.icm2630.proyecto.data.model.TipoPerfil


data class AppointmentRegisterUiState(

    // =========================================================
    // MODO EDICIÓN
    // =========================================================

    val citaId: String? = null,


    // =========================================================
    // PERFIL Y PERSONA
    // =========================================================

    val perfilUsuario: PerfilUsuario? = null,

    val personasAsociadas: List<Persona> = emptyList(),

    /**
     * true  = la cita es para el propio usuario ("Yo").
     * false = la cita es para una persona asociada.
     */
    val citaParaMi: Boolean = true,

    /**
     * null = cita del propio usuario.
     * id   = cita de una persona asociada.
     */
    val personaSeleccionadaId: String? = null,


    // =========================================================
    // INFORMACIÓN DE LA CITA
    // =========================================================

    val tipoCita: TipoCita =
        TipoCita.MEDICINA_GENERAL,

    val especialidad: String = "",

    val motivo: String = "",


    // =========================================================
    // FECHA Y HORA
    // =========================================================

    val fechaMillis: Long? = null,

    val hora: Int? = null,

    val minuto: Int? = null,


    // =========================================================
    // LUGAR Y MODALIDAD
    // =========================================================

    val modalidad: ModalidadCita =
        ModalidadCita.PRESENCIAL,

    val institucion: String = "",

    val direccion: String = "",

    val enlaceVirtual: String = "",


    // =========================================================
    // INFORMACIÓN DEL MÉDICO
    // =========================================================

    /**
     * Nombre del médico, especialista u odontólogo.
     */
    val nombreMedico: String = "",


    // =========================================================
    // INFORMACIÓN ADICIONAL
    // =========================================================

    val notas: String = "",

    val soporteUri: String? = null,

    val nombreSoporte: String? = null,


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
        get() {

            if (citaParaMi) {
                return null
            }

            return personasAsociadas.find { persona ->
                persona.id == personaSeleccionadaId
            }
        }


    // =========================================================
    // VALIDACIÓN DE PERSONA
    // =========================================================

    val personaValida: Boolean
        get() {

            return when (
                perfilUsuario?.tipoPerfil
            ) {

                // El titular siempre puede registrar
                // una cita para sí mismo.
                TipoPerfil.TITULAR -> {
                    true
                }


                // El acompañante puede elegir:
                //
                // 1. "Yo"
                // 2. Una persona asociada
                TipoPerfil.ACOMPANANTE -> {

                    if (citaParaMi) {

                        true

                    } else {

                        personaSeleccionadaId != null
                    }
                }


                null -> {
                    false
                }
            }
        }


    // =========================================================
    // VALIDACIÓN DE ESPECIALIDAD
    // =========================================================

    val especialidadValida: Boolean
        get() {

            return if (
                tipoCita ==
                TipoCita.ESPECIALISTA
            ) {

                especialidad.isNotBlank()

            } else {

                true
            }
        }


    // =========================================================
    // VALIDACIÓN DE FECHA Y HORA
    // =========================================================

    val fechaHoraValida: Boolean
        get() =
            fechaMillis != null &&
                    hora != null &&
                    minuto != null


    // =========================================================
    // VALIDACIÓN DEL LUGAR
    // =========================================================

    val ubicacionValida: Boolean
        get() {

            return when (modalidad) {

                ModalidadCita.PRESENCIAL -> {

                    institucion.isNotBlank() &&
                            direccion.isNotBlank()
                }


                ModalidadCita.VIRTUAL -> {

                    /*
                     * El enlace puede agregarse posteriormente
                     * cuando la institución lo envíe.
                     */
                    true
                }
            }
        }


    // =========================================================
    // VALIDACIÓN GENERAL
    // =========================================================

    val formularioValido: Boolean
        get() {

            return personaValida &&
                    motivo.isNotBlank() &&
                    especialidadValida &&
                    fechaHoraValida &&
                    ubicacionValida
        }
}