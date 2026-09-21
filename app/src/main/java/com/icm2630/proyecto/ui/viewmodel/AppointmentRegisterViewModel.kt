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

                // Por defecto la cita será para el propio usuario.
                citaParaMi = true,

                personaSeleccionadaId = null
            )
    }


    // =========================================================
    // CITA PARA "YO"
    // =========================================================

    fun seleccionarYo() {

        _uiState.value =
            _uiState.value.copy(

                citaParaMi = true,

                // null representa al propio usuario.
                personaSeleccionadaId = null
            )
    }


    // =========================================================
    // CITA PARA PERSONA ASOCIADA
    // =========================================================

    fun seleccionarPersona(
        personaId: String
    ) {

        val existe =
            _uiState.value
                .personasAsociadas
                .any { persona ->

                    persona.id ==
                            personaId
                }


        if (!existe) {
            return
        }


        _uiState.value =
            _uiState.value.copy(

                citaParaMi = false,

                personaSeleccionadaId =
                    personaId
            )
    }


    // =========================================================
    // MODO EDICIÓN
    // =========================================================

    /**
     * Precarga el formulario con una cita ya registrada
     * para permitir modificarla.
     */
    fun cargarCitaParaEditar(
        citaId: String
    ) {

        val cita =
            CitaRepository.obtenerPorId(
                citaId
            ) ?: return


        _uiState.value =
            _uiState.value.copy(

                citaId =
                    cita.id,


                // Si personaId es null,
                // la cita pertenece al propio usuario.
                citaParaMi =
                    cita.personaId == null,


                personaSeleccionadaId =
                    cita.personaId,


                tipoCita =
                    cita.tipo,

                especialidad =
                    cita.especialidad,

                motivo =
                    cita.motivo,

                fechaMillis =
                    cita.fechaMillis,

                hora =
                    cita.hora,

                minuto =
                    cita.minuto,

                modalidad =
                    cita.modalidad,

                institucion =
                    cita.institucion,

                direccion =
                    cita.direccion,

                enlaceVirtual =
                    cita.enlaceVirtual,

                nombreMedico =
                    cita.nombreMedico,

                notas =
                    cita.notas,

                soporteUri =
                    cita.soporteUri
            )
    }


    // =========================================================
    // NUEVA CITA
    // =========================================================

    fun iniciarNuevaCita() {

        val estadoActual =
            _uiState.value


        _uiState.value =
            AppointmentRegisterUiState(

                perfilUsuario =
                    estadoActual.perfilUsuario,

                personasAsociadas =
                    estadoActual.personasAsociadas,

                citaParaMi = true,

                personaSeleccionadaId = null
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
                 * limpiamos la especialidad.
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

                        // El enlace virtual deja de aplicar.
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
    // MÉDICO / ESPECIALISTA
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
                    estado.citaId
                        ?: UUID
                            .randomUUID()
                            .toString(),


                /*
                 * "Yo"
                 * → personaId = null
                 *
                 * Persona asociada
                 * → personaId = id de la persona
                 */
                personaId =
                    if (estado.citaParaMi) {

                        null

                    } else {

                        estado.personaSeleccionadaId
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


        // =====================================================
        // ACTUALIZAR O REGISTRAR
        // =====================================================

        if (estado.citaId != null) {

            CitaRepository.actualizar(
                cita
            )

        } else {

            CitaRepository.registrar(
                cita
            )
        }


        _uiState.value =
            _uiState.value.copy(

                citaId =
                    cita.id,

                guardando =
                    false,

                guardadoExitoso =
                    true
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