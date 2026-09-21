package com.icm2630.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.icm2630.proyecto.data.model.Genero
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.PersonaVinculada
import com.icm2630.proyecto.data.model.TipoPerfil
import com.icm2630.proyecto.data.repository.VinculacionSimulator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private val LONGITUD_CODIGO = VinculacionSimulator.LONGITUD_CODIGO

data class PerfilSetupUiState(
    val nombre: String = "",
    val fechaNacimientoMillis: Long? = null,
    val genero: Genero? = null,
    val contacto: String = "",
    val contactoEmergencia: String = "",
    val tipoPerfil: TipoPerfil = TipoPerfil.TITULAR,
    val condicionRelevante: String = "",
    val permisoUbicacion: Boolean = false,
    val notificacionesActivas: Boolean = true,
    /** Código que este titular genera para compartir con su acompañante. */
    val codigoGenerado: String? = null,
    /** Relación declarada por el acompañante con el titular (ej. "Hija", "Cuidador"). */
    val relacionConTitular: String = "",
    /** Lo que el acompañante va escribiendo en el campo de código. */
    val codigoIngresado: String = "",
    /** Titular con el que el acompañante quedó vinculado tras validar el código. */
    val personaVinculada: PersonaVinculada? = null,
    val errorCodigo: String? = null,
    val guardando: Boolean = false
) {
    /** Mínimo indispensable para continuar. Lo demás se completa después en Ajustes. */
    val puedeContinuar: Boolean
        get() {
            val datosBasicos = nombre.isNotBlank() &&
                    fechaNacimientoMillis != null &&
                    contacto.isNotBlank()
            val vinculacionResuelta = when (tipoPerfil) {
                // El titular no depende de vincularse para poder continuar:
                // puede generar y compartir su código más adelante también.
                TipoPerfil.TITULAR -> true
                // El acompañante sí necesita haber validado un código real.
                TipoPerfil.ACOMPANANTE -> personaVinculada != null &&
                        relacionConTitular.isNotBlank()
            }
            return datosBasicos && vinculacionResuelta
        }
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

    fun onTipoPerfilChange(valor: TipoPerfil) = _uiState.update {
        // Cambiar de rol invalida cualquier vinculación a medio hacer del rol anterior.
        it.copy(
            tipoPerfil = valor,
            codigoGenerado = null,
            codigoIngresado = "",
            relacionConTitular = "",
            personaVinculada = null,
            errorCodigo = null
        )
    }

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
        _uiState.update { it.copy(codigoGenerado = VinculacionSimulator.generarCodigo()) }
    }

    fun onRelacionChange(valor: String) = _uiState.update {
        it.copy(relacionConTitular = valor, errorCodigo = null)
    }

    fun onCodigoIngresadoChange(valor: String) = _uiState.update {
        it.copy(
            codigoIngresado = valor.uppercase().filter { c -> c.isLetterOrDigit() }.take(LONGITUD_CODIGO),
            errorCodigo = null,
            personaVinculada = null
        )
    }

    /**
     * HU-03: el acompañante ingresa el código que le compartió su titular.
     * Todavía no hay backend real, así que se valida el formato del código
     * y se simula la respuesta que normalmente vendría del servidor
     * (los datos básicos del titular dueño de ese código).
     */
    fun vincularConCodigo() {
        val estado = _uiState.value
        val codigo = estado.codigoIngresado

        if (codigo.length != LONGITUD_CODIGO) {
            _uiState.update { it.copy(errorCodigo = "El código debe tener $LONGITUD_CODIGO caracteres") }
            return
        }
        if (estado.relacionConTitular.isBlank()) {
            _uiState.update { it.copy(errorCodigo = "Indica tu relación con esa persona") }
            return
        }

        val nombreTitular = VinculacionSimulator.nombreParaCodigo(codigo)
        _uiState.update {
            it.copy(
                personaVinculada = PersonaVinculada(
                    nombre = nombreTitular,
                    relacion = it.relacionConTitular.trim(),
                    codigo = codigo
                ),
                errorCodigo = null
            )
        }
    }

    fun quitarVinculacion() = _uiState.update {
        it.copy(personaVinculada = null, codigoIngresado = "", errorCodigo = null)
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
            codigoVinculacion = codigoGenerado,
            personaVinculada = personaVinculada,
            configurado = true
        )
    }
}
