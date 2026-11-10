package com.icm2630.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import com.icm2630.proyecto.data.model.FormaMedicamento
import com.icm2630.proyecto.data.model.Medicamento
import com.icm2630.proyecto.data.repository.MedicamentoRepository
import com.icm2630.proyecto.data.repository.VinculacionRepository
import com.icm2630.proyecto.data.repository.SesionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID


class MedicationRegisterViewModel : ViewModel() {

    // ESTADO
    private val _uiState =
        MutableStateFlow(
            MedicationRegisterUiState()
        )

    val uiState: StateFlow<MedicationRegisterUiState> =
        _uiState.asStateFlow()

    init {
        cargarPerfilDesdeSesion()
    }

    // PERFIL DEL USUARIO
    /** Carga el perfil de la sesión al crear el ViewModel de esta entrada. */
    private fun cargarPerfilDesdeSesion() {

        val perfilUsuario = SesionRepository.perfil ?: return


        // Los vínculos autorizan consulta. Cada cuenta registra sus propios datos.
        val personas = emptyList<com.icm2630.proyecto.data.model.PersonaVinculada>()

        _uiState.value = _uiState.value.copy(
                perfilUsuario = perfilUsuario,
                personasAsociadas = personas,
                personaSeleccionadaId = null
            )
    }

    // PERSONA
    fun seleccionarPersona(personaId: String) {

        val existe = _uiState.value.personasAsociadas
                .any { persona ->
                    persona.id == personaId
                }

        if (!existe) {
            return
        }


        _uiState.value = _uiState.value.copy(
                personaSeleccionadaId = personaId
            )
    }

    // MODO EDICIÓN (aplicado también a medicamentos)

    /**
     * Precarga el formulario con un medicamento ya registrado
     * para que la persona pueda modificarlo en vez de crear
     * uno desde cero.
     */
    fun cargarMedicamentoParaEditar(medicamentoId: String) {

        val medicamento =
            MedicamentoRepository.obtenerPorId(
                medicamentoId
            ) ?: return


        _uiState.value =
            _uiState.value.copy(
                medicamentoId = medicamento.id,
                personaSeleccionadaId = medicamento.personaId,
                nombre = medicamento.nombre,
                forma = medicamento.forma,
                dosis = medicamento.dosis,
                unidad = medicamento.unidad,
                cantidadPorToma = medicamento.cantidadPorToma,
                horarios = medicamento.horarios,
                fechaInicioMillis = medicamento.fechaInicioMillis,
                fechaFinMillis = medicamento.fechaFinMillis,
                tratamientoPermanente = medicamento.tratamientoPermanente,
                indicaciones = medicamento.indicaciones,
                ordenMedicaUri = medicamento.ordenMedicaUri,
                nombreOrdenMedica = medicamento.nombreOrdenMedica
            )
    }

    /**
     * El ViewModel de esta pantalla se reutiliza tanto para crear
     * como para editar. Si se entra a "registrar" después de haber
     * editado algo, hay que limpiar el formulario para no arrastrar
     * esos datos.
     */
    fun iniciarNuevoMedicamento() {

        val estadoActual = _uiState.value

        _uiState.value = MedicationRegisterUiState(
                perfilUsuario = estadoActual.perfilUsuario,
                personasAsociadas = estadoActual.personasAsociadas
            )
    }

    // NOMBRE DEL MEDICAMENTO
    fun onNombreChange(
        nombre: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                nombre = nombre
            )
    }

    // PRESENTACIÓN
    fun onFormaChange(forma: FormaMedicamento) {
        val unidadInicial =
            when (forma) {
                FormaMedicamento.PASTILLA -> "mg"
                FormaMedicamento.JARABE -> "mL"
                FormaMedicamento.GOTAS -> "gotas"
                FormaMedicamento.INYECCION -> "mL"
            }

        _uiState.value = _uiState.value.copy(
                forma = forma,
                unidad = unidadInicial,
                // Esta información solo tiene sentido
                // principalmente para pastillas.
                cantidadPorToma =
                    if (forma == FormaMedicamento.PASTILLA) {
                        "1"
                    } else {
                        ""
                    }
            )
    }

    // DOSIS
    fun onDosisChange(dosis: String) {
        _uiState.value = _uiState.value.copy(
                dosis = dosis
            )
    }

    fun onUnidadChange(unidad: String) {
        _uiState.value = _uiState.value.copy(
                unidad = unidad
            )
    }


    fun onCantidadPorTomaChange(cantidad: String) {
        _uiState.value = _uiState.value.copy(
                cantidadPorToma = cantidad
            )
    }

    // HORARIOS
    fun agregarHorario(horario: String) {

        if (horario.isBlank()) {
            return
        }

        val horariosActuales = _uiState.value.horarios

        // No agregamos horarios repetidos.
        if (horario in horariosActuales) {
            return
        }

        _uiState.value = _uiState.value.copy(
                horarios =
                    horariosActuales + horario
            )
    }

    fun eliminarHorario(horario: String) {

        _uiState.value = _uiState.value.copy(
                horarios =
                    _uiState.value
                        .horarios
                        .filterNot {
                            it == horario
                        }
            )
    }

    // FECHAS
    fun onFechaInicioChange(fechaMillis: Long) {

        val estadoActual = _uiState.value

        /*
         * Si ya había una fecha final anterior
         * a la nueva fecha inicial, la limpiamos.
         */
        val nuevaFechaFin =
            estadoActual
                .fechaFinMillis
                ?.takeIf { fechaFin ->

                    fechaFin >= fechaMillis
                }

        _uiState.value = estadoActual.copy(
                fechaInicioMillis = fechaMillis,
                fechaFinMillis = nuevaFechaFin
            )
    }

    fun onFechaFinChange(fechaMillis: Long) {
        _uiState.value = _uiState.value.copy(
                fechaFinMillis = fechaMillis
            )
    }


    fun onTratamientoPermanenteChange(permanente: Boolean) {
        _uiState.value = _uiState.value.copy(
                tratamientoPermanente =
                    permanente,
                fechaFinMillis =
                    if (permanente) {
                        null
                    } else {
                        _uiState.value.fechaFinMillis
                    }
            )
    }

    // INDICACIONES
    fun onIndicacionesChange(indicaciones: String) {
        _uiState.value = _uiState.value.copy(
                indicaciones = indicaciones
            )
    }

    // ORDEN MÉDICA
    fun onOrdenMedicaSeleccionada(uri: String, nombreArchivo: String) {
        _uiState.value = _uiState.value.copy(
                ordenMedicaUri = uri,
                nombreOrdenMedica = nombreArchivo
            )
    }


    fun eliminarOrdenMedica() {
        _uiState.value = _uiState.value.copy(
                ordenMedicaUri = null,
                nombreOrdenMedica = null
            )
    }

    // GUARDAR MEDICAMENTO
    fun guardarMedicamento() {

        val estado = _uiState.value

        if (estado.guardando) return
        if (SesionRepository.familiarActivo != null || !estado.formularioValido) {
            _uiState.value =
                estado.copy(
                    mensajeError =
                        "Completa la información requerida antes de guardar."
                )

            return
        }

        _uiState.value = estado.copy(
                guardando = true,
                mensajeError = null
            )

        val medicamento = Medicamento(

                id = estado.medicamentoId ?: UUID
                            .randomUUID()
                            .toString(),

                /*
                 * null = propio usuario
                 *
                 * id de Persona = persona asociada
                 */
                personaId = estado.personaSeleccionadaId,

                nombre = estado.nombre.trim(),
                forma = estado.forma,
                dosis = estado.dosis.trim(),
                unidad = estado.unidad,
                cantidadPorToma = estado.cantidadPorToma,
                horarios = estado.horarios,
                fechaInicioMillis = estado.fechaInicioMillis!!,
                fechaFinMillis =
                    if (estado.tratamientoPermanente) {
                        null
                    } else {
                        estado.fechaFinMillis
                    },
                tratamientoPermanente = estado.tratamientoPermanente,
                indicaciones = estado.indicaciones.trim(),
                ordenMedicaUri = estado.ordenMedicaUri,
                nombreOrdenMedica = estado.nombreOrdenMedica
            )


        // Se conserva el ID al reintentar para no crear registros duplicados.
        _uiState.value = _uiState.value.copy(medicamentoId = medicamento.id)
        viewModelScope.launch {
            try {
                MedicamentoRepository.registrar(medicamento)
                _uiState.value = _uiState.value.copy(guardando = false, guardadoExitoso = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(guardando = false,
                    mensajeError = "No se pudo guardar. Revisa la conexión y los datos del formulario.")
            }
        }
    }

    // EVENTOS DE UI
    fun consumirGuardadoExitoso() {
        _uiState.value = _uiState.value.copy(
                guardadoExitoso = false
            )
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(
                mensajeError = null
            )
    }
}