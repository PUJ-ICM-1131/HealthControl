package com.icm2630.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import com.icm2630.proyecto.data.model.Genero
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.TipoSangre
import com.icm2630.proyecto.data.repository.SesionRepository
import com.icm2630.proyecto.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Solo datos de salud: el nombre y el correo ya se capturaron en el
 * registro / login, así que no se vuelven a pedir aquí.
 */
data class PerfilSetupUiState(
    val fechaNacimientoMillis: Long? = null,
    val genero: Genero? = null,
    val tipoSangre: TipoSangre? = null,
    val alergias: String = "",
    val condiciones: List<String> = emptyList(),
    val contactoEmergencia: String = "",
    val telefonoEmergencia: String = "",
    val condicionRelevante: String = "",
    val permisoUbicacion: Boolean = false,
    val notificacionesActivas: Boolean = false,
    val fotoUri: String? = null,
    val guardando: Boolean = false,
    val perfilGuardado: PerfilUsuario? = null,
    val mensajeError: String? = null
) {
    /** Mínimo indispensable para continuar. Lo demás se completa después en Ajustes. */
    val puedeContinuar: Boolean
        get() = fechaNacimientoMillis != null
}

class PerfilSetupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilSetupUiState())
    val uiState: StateFlow<PerfilSetupUiState> = _uiState.asStateFlow()

    fun onFechaChange(millis: Long?) = _uiState.update { it.copy(fechaNacimientoMillis = millis) }
    fun onGeneroChange(valor: Genero) = _uiState.update { it.copy(genero = valor) }
    fun onContactoEmergenciaChange(valor: String) =
        _uiState.update { it.copy(contactoEmergencia = valor) }

    fun onTelefonoEmergenciaChange(valor: String) =
        _uiState.update { it.copy(telefonoEmergencia = valor.filter { c -> c.isDigit() || c == '+' }) }

    /** Tocar el tipo ya seleccionado lo deja vacío otra vez. */
    fun onTipoSangreChange(valor: TipoSangre) =
        _uiState.update { it.copy(tipoSangre = if (it.tipoSangre == valor) null else valor) }

    fun onAlergiasChange(valor: String) = _uiState.update { it.copy(alergias = valor) }

    fun onCondicionToggle(condicion: String) = _uiState.update {
        it.copy(
            condiciones = if (condicion in it.condiciones) it.condiciones - condicion
            else it.condiciones + condicion
        )
    }

    fun onCondicionChange(valor: String) = _uiState.update { it.copy(condicionRelevante = valor) }

    /**
     * El switch NO debe encenderse solo: quien lo llama primero lanza la
     * solicitud real de permiso y pasa el resultado aquí. Si el usuario
     * niega, llega `false` y el switch se apaga de nuevo.
     */
    fun onPermisoUbicacionResultado(concedido: Boolean) =
        _uiState.update { it.copy(permisoUbicacion = concedido) }

    fun onPermisoNotificacionesResultado(concedido: Boolean) =
        _uiState.update { it.copy(notificacionesActivas = concedido) }

    fun onFotoUriChange(uri: String?) =
        _uiState.update { it.copy(fotoUri = uri) }

    fun guardarPerfil(nombre: String, contacto: String) {
        if (!_uiState.value.puedeContinuar || _uiState.value.guardando) return
        val perfil = construirPerfil(nombre, contacto)
        _uiState.update { it.copy(guardando = true, mensajeError = null) }
        val uid = SesionRepository.uidActual
        viewModelScope.launch {
            try {
                UsuarioRepository.guardarPerfil(uid, perfil)
                if (SesionRepository.haySesionGuardada && SesionRepository.uidActual == uid) {
                    SesionRepository.perfil = perfil
                    _uiState.update { it.copy(guardando = false, perfilGuardado = perfil) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, mensajeError = "No se pudo guardar el perfil. Revisa tu conexión e inténtalo de nuevo.") }
            }
        }
    }

    fun consumirGuardado() = _uiState.update { it.copy(perfilGuardado = null) }

    /** El nombre y el correo vienen del registro; el código ya fue reservado para esta cuenta. */
    fun construirPerfil(nombre: String, contacto: String): PerfilUsuario = with(_uiState.value) {
        PerfilUsuario(
            nombreCompleto = nombre.trim(),
            fechaNacimientoMillis = fechaNacimientoMillis,
            genero = genero,
            contacto = contacto.trim(),
            contactoEmergencia = contactoEmergencia.trim(),
            telefonoEmergencia = telefonoEmergencia.trim(),
            tipoSangre = tipoSangre,
            alergias = alergias.trim(),
            condiciones = condiciones,
            condicionRelevante = condicionRelevante.trim(),
            permisoUbicacion = permisoUbicacion,
            notificacionesActivas = notificacionesActivas,
            fotoUri = fotoUri,
            codigoVinculacion = SesionRepository.perfil?.codigoVinculacion,
            configurado = true
        )
    }
}
