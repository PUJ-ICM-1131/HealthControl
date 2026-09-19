package com.icm2630.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.icm2630.proyecto.data.model.Cita
import com.icm2630.proyecto.data.model.ModalidadCita
import com.icm2630.proyecto.data.model.TipoCita
import com.icm2630.proyecto.data.model.TipoPerfil
import com.icm2630.proyecto.data.repository.CitaRepository
import com.icm2630.proyecto.data.repository.PersonaRepository
import com.icm2630.proyecto.data.repository.SesionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID


class AppointmentRegisterViewModel : ViewModel() {

    // =========================================================
    // ESTADO
    // =========================================================

    private val _uiState =
        MutableStateFlow(
            AppointmentRegisterUiState()
        )

    val uiState: StateFlow<AppointmentRegisterUiState> =
        _uiState.asStateFlow()


    // =========================================================
    // INICIALIZACIÓN
    // =========================================================

    init {
        cargarPerfilDesdeSesion()
    }


    // =========================================================
    // PERFIL Y PERSONAS ASOCIADAS
    // =========================================================

    private fun cargarPerfilDesdeSesion() {

        val perfilUsuario =
            SesionRepository.perfil
                ?: return


        val personas =
            if (
                perfilUsuario.tipoPerfil ==
                TipoPerfil.ACOMPANANTE
            ) {

                PersonaRepository
                    .obtenerPersonasAsociadas()

            } else {

                emptyList()
            }


        _uiState.value =
            _uiState.value.copy(
                perfilUsuario = perfilUsuario,
                personasAsociadas = personas,
                personaSeleccionadaId = null
            )
    }


    fun seleccionarPersona(
        personaId: String
    ) {

        val existe =
            _uiState.value
                .personasAsociadas
                .any { persona ->
                    persona.id == personaId
                }


        if (!existe) {
            return
        }


        _uiState.value =
            _uiState.value.copy(
                personaSeleccionadaId = personaId
            )
    }


    // =========================================================
    // TIPO DE CITA
    // =========================================================

    fun onTipoCitaChange(
        tipoCita: TipoCita
    ) {

        _uiState.value =
            _uiState.value.copy(
                tipoCita = tipoCita,

                /*
                 * Si deja de ser cita con especialista,
                 * limpiamos una especialidad que ya no aplica.
                 */
                especialidad =
                    if (
                        tipoCita ==
                        TipoCita.ESPECIALISTA
                    ) {

                        _uiState.value.especialidad

                    } else {

                        ""
                    }
            )
    }


    // =========================================================
    // ESPECIALIDAD
    // =========================================================

    fun onEspecialidadChange(
        especialidad: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                especialidad = especialidad
            )
    }


    // =========================================================
    // MOTIVO
    // =========================================================

    fun onMotivoChange(
        motivo: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                motivo = motivo
            )
    }


    // =========================================================
    // FECHA
    // =========================================================

    fun onFechaChange(
        fechaMillis: Long
    ) {

        _uiState.value =
            _uiState.value.copy(
                fechaMillis = fechaMillis
            )
    }


    // =========================================================
    // HORA
    // =========================================================

    fun onHoraChange(
        hora: Int,
        minuto: Int
    ) {

        _uiState.value =
            _uiState.value.copy(
                hora = hora,
                minuto = minuto
            )
    }


    // =========================================================
    // MODALIDAD
    // =========================================================

    fun onModalidadChange(
        modalidad: ModalidadCita
    ) {

        val estadoActual =
            _uiState.value


        _uiState.value =
            when (modalidad) {

                ModalidadCita.PRESENCIAL -> {

                    estadoActual.copy(
                        modalidad =
                            ModalidadCita.PRESENCIAL,

                        /*
                         * Un enlace virtual deja de aplicar.
                         */
                        enlaceVirtual = ""
                    )
                }


                ModalidadCita.VIRTUAL -> {

                    estadoActual.copy(
                        modalidad =
                            ModalidadCita.VIRTUAL,

                        /*
                         * Para cita virtual no necesitamos
                         * institución física ni dirección.
                         */
                        institucion = "",
                        direccion = ""
                    )
                }
            }
    }


    // =========================================================
    // INSTITUCIÓN
    // =========================================================

    fun onInstitucionChange(
        institucion: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                institucion = institucion
            )
    }


    // =========================================================
    // DIRECCIÓN
    // =========================================================

    fun onDireccionChange(
        direccion: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                direccion = direccion
            )
    }


    // =========================================================
    // ENLACE VIRTUAL
    // =========================================================

    fun onEnlaceVirtualChange(
        enlace: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                enlaceVirtual = enlace
            )
    }


    // =========================================================
    // MÉDICO
    // =========================================================

    fun onNombreMedicoChange(
        nombreMedico: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                nombreMedico = nombreMedico
            )
    }


    // =========================================================
    // NOTAS
    // =========================================================

    fun onNotasChange(
        notas: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                notas = notas
            )
    }


    // =========================================================
    // SOPORTE
    // =========================================================

    fun onSoporteSeleccionado(
        uri: String,
        nombreArchivo: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                soporteUri = uri,
                nombreSoporte = nombreArchivo
            )
    }


    fun eliminarSoporte() {

        _uiState.value =
            _uiState.value.copy(
                soporteUri = null,
                nombreSoporte = null
            )
    }


    // =========================================================
    // GUARDAR CITA
    // =========================================================

    fun guardarCita() {

        val estado =
            _uiState.value


        if (!estado.formularioValido) {

            _uiState.value =
                estado.copy(
                    mensajeError =
                        "Completa la información requerida antes de guardar."
                )

            return
        }


        _uiState.value =
            estado.copy(
                guardando = true,
                mensajeError = null
            )


        val cita =
            Cita(

                id =
                    UUID
                        .randomUUID()
                        .toString(),

                /*
                 * null = cita del propio usuario.
                 * id   = persona asociada.
                 */
                personaId =
                    if (
                        estado.perfilUsuario?.tipoPerfil ==
                        TipoPerfil.ACOMPANANTE
                    ) {

                        estado.personaSeleccionadaId

                    } else {

                        null
                    },

                tipo =
                    estado.tipoCita,

                especialidad =
                    estado.especialidad.trim(),

                motivo =
                    estado.motivo.trim(),

                fechaMillis =
                    estado.fechaMillis!!,

                hora =
                    estado.hora!!,

                minuto =
                    estado.minuto!!,

                modalidad =
                    estado.modalidad,

                institucion =
                    estado.institucion.trim(),

                direccion =
                    estado.direccion.trim(),

                enlaceVirtual =
                    estado.enlaceVirtual.trim(),

                nombreMedico =
                    estado.nombreMedico.trim(),

                notas =
                    estado.notas.trim(),

                soporteUri =
                    estado.soporteUri
            )


        CitaRepository.registrar(
            cita
        )


        _uiState.value =
            _uiState.value.copy(
                guardando = false,
                guardadoExitoso = true
            )
    }


    // =========================================================
    // EVENTOS DE UI
    // =========================================================

    fun consumirGuardadoExitoso() {

        _uiState.value =
            _uiState.value.copy(
                guardadoExitoso = false
            )
    }


    fun limpiarError() {

        _uiState.value =
            _uiState.value.copy(
                mensajeError = null
            )
    }
}