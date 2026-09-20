package com.icm2630.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.icm2630.proyecto.data.model.TipoPerfil
import com.icm2630.proyecto.data.repository.PersonaRepository
import com.icm2630.proyecto.data.repository.SesionRepository
import com.icm2630.proyecto.data.repository.UbicacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class MapViewModel : ViewModel() {


    // =========================================================
    // ESTADO
    // =========================================================

    private val _uiState =
        MutableStateFlow(
            MapUiState()
        )

    val uiState: StateFlow<MapUiState> =
        _uiState.asStateFlow()


    // =========================================================
    // INICIALIZACIÓN
    // =========================================================

    init {
        cargarDatos()
    }


    // =========================================================
    // CARGAR INFORMACIÓN INICIAL
    // =========================================================

    private fun cargarDatos() {

        val perfil =
            SesionRepository.perfil


        if (perfil == null) {

            _uiState.value =
                _uiState.value.copy(
                    mensajeError =
                        "No se encontró información del perfil."
                )

            return
        }


        // -----------------------------------------------------
        // PERFIL INDIVIDUAL
        // -----------------------------------------------------

        if (
            perfil.tipoPerfil ==
            TipoPerfil.TITULAR
        ) {

            val ubicacion =
                UbicacionRepository
                    .obtenerUbicacionPropia()


            _uiState.value =
                MapUiState(
                    perfilUsuario = perfil,
                    personasAsociadas = emptyList(),
                    personaSeleccionadaId = null,
                    ubicacion = ubicacion,
                    cargando = false,
                    mensajeError =
                        if (ubicacion == null) {
                            "No hay una ubicación disponible."
                        } else {
                            null
                        }
                )

            return
        }


        // -----------------------------------------------------
        // PERFIL ASOCIADO
        // -----------------------------------------------------

        val personas =
            PersonaRepository
                .obtenerPersonasAsociadas()


        val primeraPersona =
            personas.firstOrNull()


        val ubicacion =
            primeraPersona?.let { persona ->

                UbicacionRepository
                    .obtenerPorPersona(
                        persona.id
                    )
            }


        _uiState.value =
            MapUiState(
                perfilUsuario = perfil,
                personasAsociadas = personas,
                personaSeleccionadaId =
                    primeraPersona?.id,
                ubicacion = ubicacion,
                cargando = false,
                mensajeError =
                    if (personas.isEmpty()) {
                        "No tienes personas asociadas disponibles."
                    } else if (ubicacion == null) {
                        "No hay una ubicación disponible para esta persona."
                    } else {
                        null
                    }
            )
    }


    // =========================================================
    // CAMBIAR PERSONA
    // =========================================================

    fun seleccionarPersona(
        personaId: String
    ) {

        val personaExiste =
            _uiState.value
                .personasAsociadas
                .any { persona ->

                    persona.id ==
                            personaId
                }


        if (!personaExiste) {
            return
        }


        val nuevaUbicacion =
            UbicacionRepository
                .obtenerPorPersona(
                    personaId
                )


        _uiState.value =
            _uiState.value.copy(
                personaSeleccionadaId =
                    personaId,

                ubicacion =
                    nuevaUbicacion,

                mensajeError =
                    if (nuevaUbicacion == null) {
                        "No hay una ubicación disponible para esta persona."
                    } else {
                        null
                    }
            )
    }


    // =========================================================
    // ACTUALIZAR UBICACIÓN
    // =========================================================

    fun actualizarUbicacion() {

        val estadoActual =
            _uiState.value


        _uiState.value =
            estadoActual.copy(
                cargando = true,
                mensajeError = null
            )


        val nuevaUbicacion =
            when (
                estadoActual
                    .perfilUsuario
                    ?.tipoPerfil
            ) {

                TipoPerfil.TITULAR -> {

                    UbicacionRepository
                        .obtenerUbicacionPropia()
                }


                TipoPerfil.ACOMPANANTE -> {

                    estadoActual
                        .personaSeleccionadaId
                        ?.let { personaId ->

                            UbicacionRepository
                                .obtenerPorPersona(
                                    personaId
                                )
                        }
                }


                null -> {
                    null
                }
            }


        _uiState.value =
            _uiState.value.copy(
                cargando = false,
                ubicacion = nuevaUbicacion,

                mensajeError =
                    if (nuevaUbicacion == null) {
                        "No fue posible obtener la ubicación."
                    } else {
                        null
                    }
            )
    }


    // =========================================================
    // LIMPIAR ERROR
    // =========================================================

    fun limpiarError() {

        _uiState.value =
            _uiState.value.copy(
                mensajeError = null
            )
    }
}