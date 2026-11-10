package com.icm2630.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icm2630.proyecto.data.repository.CitaRepository
import com.icm2630.proyecto.data.repository.MedicamentoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalleRegistroUiState(val eliminando: Boolean = false, val eliminado: Boolean = false, val error: String? = null)

class DetalleRegistroViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DetalleRegistroUiState())
    val uiState = _uiState.asStateFlow()

    fun eliminarCita(id: String) = eliminar { CitaRepository.eliminar(id) }
    fun eliminarMedicamento(id: String) = eliminar { MedicamentoRepository.eliminar(id) }
    fun consumirEliminado() { _uiState.value = DetalleRegistroUiState() }

    private fun eliminar(operacion: suspend () -> Unit) {
        if (_uiState.value.eliminando) return
        _uiState.value = DetalleRegistroUiState(eliminando = true)
        viewModelScope.launch {
            try {
                operacion()
                _uiState.value = DetalleRegistroUiState(eliminado = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = DetalleRegistroUiState(error = "No se pudo eliminar. Revisa la conexión e inténtalo de nuevo.")
            }
        }
    }
}
