package com.icm2630.proyecto.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icm2630.proyecto.data.model.TomaProgramada
import com.icm2630.proyecto.data.repository.SesionRepository
import com.icm2630.proyecto.data.repository.TomaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TomasUiState(val guardando: Set<String> = emptySet(), val error: String? = null)

class TomasViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TomasUiState())
    val uiState = _uiState.asStateFlow()

    fun cambiar(toma: TomaProgramada, tomada: Boolean) {
        val uid = TomaRepository.cuentaConsultada() ?: return
        val operacion = "${uid}/${toma.clave}"
        if (operacion in _uiState.value.guardando || SesionRepository.familiarActivo != null) return
        _uiState.update { it.copy(guardando = it.guardando + operacion, error = null) }
        viewModelScope.launch {
            try {
                TomaRepository.cambiar(uid, toma, tomada)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                Log.e("TomasViewModel", "No se pudo actualizar la toma", e)
                _uiState.update { it.copy(error = "No se pudo guardar la toma. Revisa la conexión e inténtalo de nuevo.") }
            } finally {
                _uiState.update { it.copy(guardando = it.guardando - operacion) }
            }
        }
    }
    fun limpiarError() = _uiState.update { it.copy(error = null) }
}
