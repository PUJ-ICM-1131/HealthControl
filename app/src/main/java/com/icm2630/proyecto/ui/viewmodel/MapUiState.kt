package com.icm2630.proyecto.ui.viewmodel

import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.Persona
import com.icm2630.proyecto.data.model.TipoPerfil
import com.icm2630.proyecto.data.model.UbicacionPersona


data class MapUiState(

    // =========================================================
    // PERFIL DEL USUARIO
    // =========================================================

    val perfilUsuario: PerfilUsuario? = null,


    // =========================================================
    // PERSONAS ASOCIADAS
    // =========================================================

    val personasAsociadas: List<Persona> = emptyList(),

    /**
     * null:
     * - perfil individual → ubicación propia
     * - perfil asociado sin selección todavía
     *
     * con id:
     * - ubicación de una Persona asociada
     */
    val personaSeleccionadaId: String? = null,


    // =========================================================
    // UBICACIÓN MOSTRADA
    // =========================================================

    val ubicacion: UbicacionPersona? = null,


    // =========================================================
    // ESTADO DE UI
    // =========================================================

    val cargando: Boolean = false,

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
    // NOMBRE QUE MOSTRAREMOS
    // =========================================================

    val nombrePersona: String
        get() {

            return when (
                perfilUsuario?.tipoPerfil
            ) {

                TipoPerfil.INDIVIDUAL -> {

                    perfilUsuario
                        ?.nombreCompleto
                        ?.ifBlank {
                            "Mi ubicación"
                        }
                        ?: "Mi ubicación"
                }


                TipoPerfil.ASOCIADO -> {

                    personaSeleccionada
                        ?.nombreCompleto
                        ?: "Selecciona una persona"
                }


                null -> {

                    "Ubicación"
                }
            }
        }


    // =========================================================
    // TIPO DE PERSONA
    // =========================================================

    val tipoPersona: String
        get() {

            return when (
                perfilUsuario?.tipoPerfil
            ) {

                TipoPerfil.ASOCIADO ->
                    "Persona asociada"

                TipoPerfil.INDIVIDUAL ->
                    "Perfil personal"

                null ->
                    ""
            }
        }


    // =========================================================
    // PERMITIR CAMBIAR PERSONA
    // =========================================================

    val puedeCambiarPersona: Boolean
        get() =
            perfilUsuario?.tipoPerfil ==
                    TipoPerfil.ASOCIADO &&
                    personasAsociadas.isNotEmpty()


    // =========================================================
    // UBICACIÓN DISPONIBLE
    // =========================================================

    val ubicacionDisponible: Boolean
        get() =
            ubicacion != null
}