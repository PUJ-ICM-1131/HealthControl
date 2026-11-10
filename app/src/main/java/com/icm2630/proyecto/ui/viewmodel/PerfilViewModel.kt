package com.icm2630.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icm2630.proyecto.data.model.*
import com.icm2630.proyecto.data.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class PerfilUiState(
    val recibidas: List<Invitacion> = emptyList(),
    val enviadas: List<Invitacion> = emptyList(),
    val vinculos: List<Invitacion> = emptyList(),
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val solicitudEnviada: Boolean = false
)

/** Sesión 11.2: snapshots como Flow, colectados y cancelados en el ámbito del ViewModel. */
class PerfilViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState = _uiState.asStateFlow()
    private var uid: String? = null
    private var escucha: Job? = null
    private var operacion: Job? = null
    private var escuchaRegistros: Job? = null
    private var cuentasObservadas: Set<String> = emptySet()

    fun iniciar(usuarioUid: String) {
        if (uid == usuarioUid && escucha?.isActive == true) return
        detener()
        uid = usuarioUid
        observarRegistros(usuarioUid, setOf(usuarioUid))
        escucha = viewModelScope.launch {
            supervisorScope {
                launch {
                    UsuarioRepository.observarPerfil(usuarioUid).catch {
                        _uiState.update { it.copy(error = "No se pudo actualizar tu perfil.") }
                    }.collect { perfil ->
                        if (perfil != null && sesionVigente(usuarioUid)) {
                            SesionRepository.perfil = perfil.copy(acompanantes = SesionRepository.perfil?.acompanantes.orEmpty())
                            SesionRepository.nombreCuenta = perfil.nombreCompleto
                        }
                    }
                }
                launch {
                    VinculacionRepository.observar(usuarioUid).catch {
                        limpiarVinculos()
                        _uiState.update { it.copy(error = "No se pudieron cargar las solicitudes. Reintenta desde tu perfil.") }
                    }.collectLatest { solicitudes ->
                        if (!sesionVigente(usuarioUid)) return@collectLatest
                        val aceptadas = solicitudes.filter { it.estado == EstadoInvitacion.ACEPTADA }
                        _uiState.update { it.copy(
                            recibidas = solicitudes.filter { s -> s.destinatarioUid == usuarioUid && s.estado == EstadoInvitacion.PENDIENTE },
                            enviadas = solicitudes.filter { s -> s.emisorUid == usuarioUid && s.estado == EstadoInvitacion.PENDIENTE },
                            vinculos = aceptadas
                        ) }
                        val autorizados = aceptadas.map { it.otroUid(usuarioUid) }.toSet()
                        observarRegistros(usuarioUid, autorizados + usuarioUid)
                        SesionRepository.perfilesVinculados = SesionRepository.perfilesVinculados.filterKeys { it in autorizados }
                        if (SesionRepository.familiarActivo?.uid?.let { it !in autorizados } == true) {
                            SesionRepository.volverAMiPerfil()
                        }
                        actualizarPersonas(aceptadas, usuarioUid)
                        coroutineScope {
                            aceptadas.distinctBy { it.otroUid(usuarioUid) }.forEach { solicitud ->
                                val otroUid = solicitud.otroUid(usuarioUid)
                                launch {
                                    UsuarioRepository.observarPerfilCompartido(otroUid).catch {
                                        SesionRepository.perfilesVinculados = SesionRepository.perfilesVinculados - otroUid
                                        if (SesionRepository.familiarActivo?.uid == otroUid) SesionRepository.volverAMiPerfil()
                                        _uiState.update { it.copy(error = "No se pudo consultar el perfil compartido de esta persona.") }
                                    }.collect { perfil ->
                                        if (!sesionVigente(usuarioUid)) return@collect
                                        SesionRepository.perfilesVinculados = if (perfil == null) {
                                            SesionRepository.perfilesVinculados - otroUid
                                        } else SesionRepository.perfilesVinculados + (otroUid to perfil)
                                        actualizarPersonas(aceptadas, usuarioUid)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observarRegistros(miUid: String, cuentas: Set<String>) {
        if (cuentas == cuentasObservadas && escuchaRegistros?.isActive == true) return
        escuchaRegistros?.cancel()
        cuentasObservadas = cuentas
        CitaRepository.conservarCuentas(cuentas)
        MedicamentoRepository.conservarCuentas(cuentas)
        escuchaRegistros = viewModelScope.launch {
            supervisorScope {
                cuentas.forEach { cuenta ->
                    launch {
                        CitaRepository.observar(cuenta).catch {
                            CitaRepository.actualizarCache(cuenta, emptyList())
                            _uiState.update { it.copy(error = "No se pudieron consultar las citas. Revisa la conexión y el vínculo.") }
                        }.collect {
                            if (sesionVigente(miUid) && cuenta in cuentasObservadas) CitaRepository.actualizarCache(cuenta, it)
                        }
                    }
                    launch {
                        MedicamentoRepository.observar(cuenta).catch {
                            MedicamentoRepository.actualizarCache(cuenta, emptyList())
                            _uiState.update { it.copy(error = "No se pudieron consultar los medicamentos. Revisa la conexión y el vínculo.") }
                        }.collect {
                            if (sesionVigente(miUid) && cuenta in cuentasObservadas) MedicamentoRepository.actualizarCache(cuenta, it)
                        }
                    }
                }
            }
        }
    }

    private fun actualizarPersonas(vinculos: List<Invitacion>, miUid: String) {
        val personas = vinculos.distinctBy { it.otroUid(miUid) }.map { invitacion ->
            val otro = invitacion.otroUid(miUid)
            val perfil = SesionRepository.perfilesVinculados[otro]
            PersonaVinculada(
                nombre = perfil?.nombreCompleto ?: invitacion.nombreDelOtro(miUid),
                relacion = "Persona vinculada",
                codigo = perfil?.codigoVinculacion.orEmpty(),
                uid = otro
            )
        }
        SesionRepository.perfil = SesionRepository.perfil?.copy(acompanantes = personas)
        val seleccionado = SesionRepository.familiarActivo?.uid
        personas.find { it.uid == seleccionado }?.let { SesionRepository.verFamiliar(it) }
    }

    fun guardarPerfil(perfil: PerfilUsuario) = ejecutar {
        UsuarioRepository.guardarPerfil(checkNotNull(uid), perfil)
        _uiState.update { it.copy(mensaje = "Perfil guardado.") }
    }

    fun enviar(codigo: String) = ejecutar {
        VinculacionRepository.enviar(checkNotNull(uid), codigo)
        _uiState.update { it.copy(mensaje = "Solicitud enviada. La otra persona debe aceptarla.", solicitudEnviada = true) }
    }

    fun responder(invitacion: Invitacion, aceptar: Boolean) = ejecutar {
        VinculacionRepository.responder(checkNotNull(uid), invitacion, aceptar)
        _uiState.update { it.copy(mensaje = if (aceptar) "Vínculo aceptado." else "Solicitud rechazada.") }
    }

    fun cancelar(invitacion: Invitacion) = ejecutar {
        VinculacionRepository.cancelar(checkNotNull(uid), invitacion)
        _uiState.update { it.copy(mensaje = "Solicitud cancelada.") }
    }

    fun desvincular(invitacion: Invitacion) = ejecutar {
        VinculacionRepository.desvincular(checkNotNull(uid), invitacion)
        _uiState.update { it.copy(mensaje = "Vínculo eliminado. Ya no se compartirán los datos.") }
    }

    fun consumirEnvio() = _uiState.update { it.copy(solicitudEnviada = false) }

    fun consumirMensaje() = _uiState.update { it.copy(error = null, mensaje = null, solicitudEnviada = false) }

    private fun ejecutar(bloque: suspend () -> Unit) {
        val actual = uid ?: return
        if (_uiState.value.guardando || !sesionVigente(actual)) return
        _uiState.update { it.copy(guardando = true, error = null, mensaje = null, solicitudEnviada = false) }
        operacion = viewModelScope.launch {
            try {
                bloque()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = if (e is IllegalArgumentException || e is IllegalStateException) e.message
                    else "No se pudo completar la operación. Revisa la conexión e inténtalo de nuevo.") }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    private fun sesionVigente(actual: String) =
        uid == actual && SesionRepository.haySesionGuardada && SesionRepository.uidActual == actual

    private fun limpiarVinculos() {
        uid?.let { observarRegistros(it, setOf(it)) }
        SesionRepository.perfilesVinculados = emptyMap()
        SesionRepository.perfil = SesionRepository.perfil?.copy(acompanantes = emptyList())
        SesionRepository.volverAMiPerfil()
    }

    fun detener() {
        escucha?.cancel()
        operacion?.cancel()
        escuchaRegistros?.cancel()
        cuentasObservadas = emptySet()
        CitaRepository.limpiar()
        MedicamentoRepository.limpiar()
        uid = null
        limpiarVinculos()
        _uiState.value = PerfilUiState()
    }
}
