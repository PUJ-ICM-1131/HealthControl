package com.icm2630.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.icm2630.proyecto.data.model.Genero
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.TipoPerfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PerfilSetupUiState(
    val nombre: String = "",
    val fechaNacimientoMillis: Long? = null,
    val genero: Genero? = null,
    val contacto: String = "",
    val contactoEmergencia: String = "",
    val tipoPerfil: TipoPerfil = TipoPerfil.INDIVIDUAL,
    val condicionRelevante: String = "",
    val permisoUbicacion: Boolean = false,
    val notificacionesActivas: Boolean = true,
    val codigoInvitacion: String? = null,
    val guardando: Boolean = false
) {
    /** Mínimo indispensable para continuar. Lo demás se completa después en Ajustes. */
    val puedeContinuar: Boolean
        get() = nombre.isNotBlank() &&
                fechaNacimientoMillis != null &&
                contacto.isNotBlank()
}

class PerfilSetupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilSetupUiState())
    val uiState: StateFlow<PerfilSetupUiState> = _uiState.asStateFlow()

    fun onNombreChange(valor: String) = _uiState.update { it.copy(nombre = valor) }
    fun onFechaChange(millis: Long?) = _uiState.update { it.copy(fechaNacimientoMillis = millis) }
    fun onGeneroChange(valor: Genero) = _uiState.update { it.copy(genero = valor) }
    fun onContactoChange(valor: String) = _uiState.update { it.copy(contacto = valor) }
    fun onContactoEmergenciaChange(valor: String) =
        _uiState.update { it.copy(contactoEmergencia = valor) }

    fun onCondicionChange(valor: String) = _uiState.update { it.copy(condicionRelevante = valor) }
    fun onTipoPerfilChange(valor: TipoPerfil) = _uiState.update { it.copy(tipoPerfil = valor) }

    /**
     * El switch NO debe encenderse solo: quien lo llama primero lanza la
     * solicitud real de permiso y pasa el resultado aquí. Si el usuario
     * niega, llega `false` y el switch se apaga de nuevo.
     */
    fun onPermisoUbicacionResultado(concedido: Boolean) =
        _uiState.update { it.copy(permisoUbicacion = concedido) }

    fun onPermisoNotificacionesResultado(concedido: Boolean) =
        _uiState.update { it.copy(notificacionesActivas = concedido) }

    /** HU-03: el titular genera el código que su acompañante usará para vincularse. */
    fun generarCodigoInvitacion() {
        val codigo = (1..6)
            .map { "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".random() }
            .joinToString("")
        _uiState.update { it.copy(codigoInvitacion = codigo) }
    }

    fun construirPerfil(): PerfilUsuario = with(_uiState.value) {
        PerfilUsuario(
            nombreCompleto = nombre.trim(),
            fechaNacimientoMillis = fechaNacimientoMillis,
            genero = genero,
            contacto = contacto.trim(),
            contactoEmergencia = contactoEmergencia.trim(),
            tipoPerfil = tipoPerfil,
            condicionRelevante = condicionRelevante.trim(),
            permisoUbicacion = permisoUbicacion,
            notificacionesActivas = notificacionesActivas,
            configurado = true
        )
    }
}