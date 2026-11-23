package com.icm2630.proyecto.ui.viewmodel

import android.app.Application
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.icm2630.proyecto.data.model.ModoRuta
import com.icm2630.proyecto.data.model.PuntoMapa
import com.icm2630.proyecto.data.repository.GeocodingRepository
import com.icm2630.proyecto.data.repository.RutaRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.delay
import com.icm2630.proyecto.data.model.PersonaVinculada
import com.icm2630.proyecto.data.repository.UbicacionRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import com.icm2630.proyecto.data.repository.SesionRepository
import com.icm2630.proyecto.data.repository.CentrosSaludRepository
/** Solo administra el mapa: no modifica perfiles, invitaciones ni familiares. */
class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val rutas = RutaRepository()
    private val direcciones = GeocodingRepository(application)
    private val centrosSalud = CentrosSaludRepository()
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    private var tareaRuta: Job? = null
    private var tareaBusqueda: Job? = null
    private var revisionRuta = 0
    private var revisionBusqueda = 0

    private var escuchaFamiliar: Job? = null
    private var escuchaCita: Job? = null
    private var tokenRutaCitaPendiente: String? = null

    fun observarCita(uid: String, id: String) {
        escuchaCita?.cancel()
        _uiState.update { it.copy(titularCitaUid = uid, cita = null, cargandoCita = true) }
        escuchaCita = viewModelScope.launch {
            com.icm2630.proyecto.data.repository.CitaRepository.observarPorId(uid, id).catch {
                limpiarRuta()
                _uiState.update { actual -> actual.copy(cita = null, cargandoCita = false,
                    mensajeError = "No se pudo consultar la cita. Revisa la conexión y el permiso de acceso.") }
            }.collect { cita ->
                val previa = _uiState.value.cita
                val lugar = cita?.let(com.icm2630.proyecto.data.model.UbicacionCitas::punto)
                val cambioLugar = previa == null || cita == null ||
                    !com.icm2630.proyecto.data.model.UbicacionCitas.mismoLugar(cita, previa) ||
                    cita.ubicacionConfirmada != previa.ubicacionConfirmada ||
                    cita.nombreLugarConfirmado != previa.nombreLugarConfirmado
                if (cambioLugar) {
                    tokenRutaCitaPendiente = null
                    invalidarRuta()
                    cancelarBusqueda()
                }
                _uiState.update { actual -> actual.copy(
                    cita = cita, cargandoCita = false,
                    destino = if (cambioLugar) lugar else actual.destino,
                    seleccion = if (cambioLugar) SeleccionMapa.DESTINO else actual.seleccion,
                    consulta = if (cambioLugar) cita?.let(com.icm2630.proyecto.data.model.UbicacionCitas::consulta).orEmpty()
                        else actual.consulta,
                    resultadosBusqueda = if (cambioLugar) emptyList() else actual.resultadosBusqueda,
                    mensajeLugar = if (cambioLugar) null else actual.mensajeLugar,
                    mensajeError = if (cita == null) "La cita ya no está disponible." else actual.mensajeError
                ) }
            }
        }
    }

    fun confirmarLugarCita() {
        val estado = _uiState.value
        val uid = estado.titularCitaUid ?: return
        val cita = estado.cita ?: return
        val punto = estado.destino ?: return
        if (estado.guardandoLugar) return
        _uiState.update { it.copy(guardandoLugar = true, mensajeLugar = null, mensajeError = null) }
        viewModelScope.launch {
            try {
                com.icm2630.proyecto.data.repository.CitaRepository.confirmarUbicacion(uid, cita, punto)
                _uiState.update { it.copy(guardandoLugar = false, mensajeLugar = "Lugar guardado en la cita.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MapViewModel", "No se pudo confirmar el lugar de la cita", e)
                val mensaje = when {
                    e is IllegalStateException -> e.message ?: "No se pudo guardar el lugar."
                    e is FirebaseFirestoreException -> when (e.code) {
                        FirebaseFirestoreException.Code.UNAVAILABLE,
                        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                            "No se pudo conectar para guardar el lugar. Revisa tu conexión e inténtalo de nuevo."
                        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                            "No se autorizó el guardado. Si es una cita de otra persona, revisa que te haya permitido gestionar su salud."
                        FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                            "Tu sesión ya no es válida. Vuelve a iniciar sesión para guardar el lugar."
                        else -> "No se pudo guardar el lugar. Inténtalo de nuevo."
                    }
                    else -> "No se pudo guardar el lugar. Inténtalo de nuevo."
                }
                _uiState.update { it.copy(guardandoLugar = false, mensajeError = mensaje) }
            }
        }
    }

    fun rutaHaciaCita(token: String) {
        if (_uiState.value.cita == null || _uiState.value.destino == null) return
        val reciente = (_uiState.value.momentoUbicacionMillis ?: 0L) >= System.currentTimeMillis() - 90_000L
        if (!reciente || _uiState.value.ubicacionActual == null) {
            tokenRutaCitaPendiente = token
            _uiState.update { it.copy(mensajeError = "Esperando tu ubicación para calcular el recorrido.") }
            return
        }
        tokenRutaCitaPendiente = null
        usarMiUbicacionComoOrigen()
        calcularRuta(token)
    }

    fun cancelarRutaCitaPendiente() { tokenRutaCitaPendiente = null }


    /** Cada vínculo autorizado aporta su propio documento de ubicación. */
    fun observarPersonas(personas: List<PersonaVinculada>) {
        escuchaFamiliar?.cancel()
        val unicas = personas.distinctBy { it.uid }
        val autorizados = unicas.map { it.uid }.toSet()
        val seleccionado = _uiState.value.personaSeleccionadaUid?.takeIf { it in autorizados }
        if (_uiState.value.personaSeleccionadaUid != seleccionado) limpiarRuta()
        _uiState.update { it.copy(
            personasEnMapa = unicas.map { persona -> PersonaEnMapa(persona) },
            personaSeleccionadaUid = seleccionado,
            ubicacionFamiliar = null, momentoFamiliarMillis = null
        ) }
        if (unicas.isEmpty()) return
        escuchaFamiliar = viewModelScope.launch {
            val flujos = unicas.map { persona ->
                UbicacionRepository.observar(persona.uid, persona.nombre)
                    .map { dato -> PersonaEnMapa(persona, dato?.punto, dato?.momentoMillis, cargando = false) }
                    .retryWhen { _, _ ->
                        emit(PersonaEnMapa(persona, cargando = false, noDisponible = true))
                        delay(15_000L)
                        true
                    }
                    .onStart { emit(PersonaEnMapa(persona)) }
            }
            combine(flujos) { it.toList() }.collect { posiciones ->
                _uiState.update { actual ->
                    val elegida = posiciones.find { it.persona.uid == actual.personaSeleccionadaUid }
                    actual.copy(
                        personasEnMapa = posiciones,
                        ubicacionFamiliar = elegida?.punto,
                        momentoFamiliarMillis = elegida?.momentoMillis
                    )
                }
                // Si se retiró el permiso, tampoco conservamos una ruta hacia ese dato.
                if (_uiState.value.personaSeleccionadaUid != null &&
                    _uiState.value.ubicacionFamiliar == null) limpiarRuta()
            }
        }
    }

    fun seleccionarPersona(uid: String) {
        val persona = _uiState.value.personasEnMapa.find { it.persona.uid == uid } ?: return
        if (_uiState.value.personaSeleccionadaUid != uid) limpiarRuta()
        _uiState.update { it.copy(
            personaSeleccionadaUid = uid,
            ubicacionFamiliar = persona.punto,
            momentoFamiliarMillis = persona.momentoMillis
        ) }
    }

    /** Pone al familiar como destino y calcula la ruta desde el origen actual (o mi ubicación). */
    fun rutaHaciaFamiliar(token: String) {
        val familiar = _uiState.value.ubicacionFamiliar ?: return
        invalidarRuta()
        cancelarBusqueda()
        _uiState.update {
            it.copy(
                origen = it.origen ?: it.ubicacionActual?.copy(nombre = "Mi ubicación"),
                origenDesdeUbicacion = if (it.origen == null) true else it.origenDesdeUbicacion,
                destino = familiar,
                seleccion = SeleccionMapa.DESTINO,
                resultadosBusqueda = emptyList()
            )
        }
        if (_uiState.value.origen == null) {
            _uiState.update {
                it.copy(mensajeError = "Activa tu ubicación o elige un origen para trazar la ruta hacia ${familiar.nombre}.")
            }
            return
        }
        calcularRuta(token)
    }

    fun actualizarUbicacion(punto: PuntoMapa, precision: Float?, momentoMillis: Long) {
        if (momentoMillis < (_uiState.value.momentoUbicacionMillis ?: 0L)) return
        _uiState.update { actual ->
            if (momentoMillis < (actual.momentoUbicacionMillis ?: 0L)) return@update actual
            actual.copy(
                ubicacionActual = punto,
                precisionMetros = precision,
                momentoUbicacionMillis = momentoMillis,
                // Una ruta ya calculada conserva el origen con el que se consultó.
                // El puck puede moverse sin cambiar silenciosamente ese recorrido.
                origen = if (actual.origenDesdeUbicacion && actual.ruta == null && !actual.calculandoRuta) {
                    punto.copy(nombre = "Mi ubicación")
                } else actual.origen
            )
        }
        tokenRutaCitaPendiente?.let { rutaHaciaCita(it) }
    }

    fun restaurarDestinoCita() {
        limpiarRuta()
        _uiState.update { it.copy(destino = it.cita?.let(com.icm2630.proyecto.data.model.UbicacionCitas::punto),
            seleccion = SeleccionMapa.DESTINO, origenDesdeUbicacion = true) }
    }

    fun seleccionarObjetivo(seleccion: SeleccionMapa) {
        tokenRutaCitaPendiente = null
        cancelarBusqueda()
        _uiState.update { it.copy(seleccion = seleccion, resultadosBusqueda = emptyList()) }
    }

    fun seleccionarPunto(punto: PuntoMapa) {
        tokenRutaCitaPendiente = null
        invalidarRuta()
        cancelarBusqueda()
        _uiState.update {
            when (it.seleccion) {
                SeleccionMapa.ORIGEN -> it.copy(
                    origen = punto,
                    origenDesdeUbicacion = false,
                    seleccion = SeleccionMapa.DESTINO,
                    resultadosBusqueda = emptyList()
                )
                SeleccionMapa.DESTINO -> it.copy(destino = punto, resultadosBusqueda = emptyList())
            }
        }
    }

    fun usarMiUbicacionComoOrigen() {
        invalidarRuta()
        _uiState.update {
            val reciente = (it.momentoUbicacionMillis ?: 0L) >= System.currentTimeMillis() - 90_000L
            it.copy(
                origen = it.ubicacionActual?.takeIf { reciente }?.copy(nombre = "Mi ubicación"),
                origenDesdeUbicacion = true,
                seleccion = SeleccionMapa.DESTINO
            )
        }
    }

    fun cambiarModo(modo: ModoRuta) {
        if (_uiState.value.modoRuta == modo) return
        invalidarRuta()
        _uiState.update { it.copy(modoRuta = modo) }
    }

    fun calcularRuta(token: String) {
        val estado = _uiState.value
        val origen = estado.origen
        val destino = estado.destino
        if (origen == null || destino == null) {
            _uiState.update { it.copy(mensajeError = "Elige un origen y un destino para calcular la ruta.") }
            return
        }
        invalidarRuta()
        val revision = revisionRuta
        _uiState.update { it.copy(calculandoRuta = true) }
        tareaRuta = viewModelScope.launch {
            try {
                val resultado = rutas.calcular(token, origen, destino, estado.modoRuta)
                if (revision == revisionRuta) {
                    _uiState.update { it.copy(ruta = resultado, calculandoRuta = false) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                if (revision == revisionRuta) {
                    _uiState.update { it.copy(calculandoRuta = false, mensajeError = e.message) }
                }
            }
        }
    }

    fun cambiarConsulta(consulta: String) {
        cancelarBusqueda()
        _uiState.update { it.copy(consulta = consulta, resultadosBusqueda = emptyList()) }
    }

    fun buscarDireccion() {
        val consulta = _uiState.value.consulta.trim()
        if (consulta.length < 3) {
            _uiState.update { it.copy(mensajeError = "Escribe una dirección o lugar e incluye la ciudad.") }
            return
        }
        cancelarBusqueda()
        val revision = revisionBusqueda
        _uiState.update { it.copy(buscandoDireccion = true, resultadosBusqueda = emptyList(), mensajeError = null) }
        tareaBusqueda = viewModelScope.launch {
            try {
                val resultados = withTimeout(15_000L) { direcciones.buscar(consulta) }
                if (revision == revisionBusqueda) {
                    _uiState.update {
                        it.copy(
                            buscandoDireccion = false,
                            resultadosBusqueda = resultados,
                            mensajeError = if (resultados.isEmpty()) {
                                "No encontramos ese lugar. Añade la ciudad o selecciónalo en el mapa."
                            } else null
                        )
                    }
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                if (revision == revisionBusqueda) {
                    _uiState.update {
                        it.copy(buscandoDireccion = false, mensajeError = "La búsqueda tardó demasiado. Elige el punto en el mapa o inténtalo de nuevo.")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                if (revision == revisionBusqueda) {
                    _uiState.update { it.copy(buscandoDireccion = false, mensajeError = e.message) }
                }
            }
        }
    }
    /** Servicio REST (Mapbox Search Box): centros de salud cercanos a mi ubicación. */
    fun buscarCentrosSalud(token: String) {
        val cerca = _uiState.value.ubicacionActual
        if (cerca == null) {
            _uiState.update { it.copy(mensajeError = "Activa tu ubicación para buscar centros de salud cercanos.") }
            return
        }
        cancelarBusqueda()
        val revision = revisionBusqueda
        _uiState.update {
            it.copy(
                buscandoDireccion = true,
                seleccion = SeleccionMapa.DESTINO,
                consulta = "",
                resultadosBusqueda = emptyList(),
                mensajeError = null
            )
        }
        tareaBusqueda = viewModelScope.launch {
            try {
                val resultados = withTimeout(20_000L) { centrosSalud.buscarCercanos(token, cerca) }
                if (revision == revisionBusqueda) {
                    _uiState.update {
                        it.copy(
                            buscandoDireccion = false,
                            resultadosBusqueda = resultados,
                            mensajeError = if (resultados.isEmpty()) {
                                "No encontramos centros de salud cerca de tu ubicación."
                            } else null
                        )
                    }
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                if (revision == revisionBusqueda) {
                    _uiState.update {
                        it.copy(buscandoDireccion = false, mensajeError = "La búsqueda tardó demasiado. Inténtalo de nuevo.")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                if (revision == revisionBusqueda) {
                    _uiState.update { it.copy(buscandoDireccion = false, mensajeError = e.message) }
                }
            }
        }
    }
    fun limpiarRuta() {
        tokenRutaCitaPendiente = null
        invalidarRuta()
        cancelarBusqueda()
        _uiState.update {
            it.copy(
                origen = null,
                origenDesdeUbicacion = false,
                destino = null,
                seleccion = SeleccionMapa.ORIGEN,
                consulta = "",
                resultadosBusqueda = emptyList()
            )
        }
    }

    fun limpiarError() = _uiState.update { it.copy(mensajeError = null) }

    fun cancelarOperaciones() {
        escuchaCita?.cancel()
        escuchaCita = null
        tokenRutaCitaPendiente = null
        escuchaFamiliar?.cancel()
        escuchaFamiliar = null
        _uiState.update { it.copy(personasEnMapa = emptyList(), personaSeleccionadaUid = null,
            ubicacionFamiliar = null, momentoFamiliarMillis = null) }
        invalidarRuta(borrarResultado = false)
        cancelarBusqueda()
    }

    private fun invalidarRuta(borrarResultado: Boolean = true) {
        revisionRuta++
        tareaRuta?.cancel()
        _uiState.update {
            it.copy(
                ruta = if (borrarResultado) null else it.ruta,
                calculandoRuta = false,
                mensajeError = null
            )
        }
    }

    private fun cancelarBusqueda() {
        revisionBusqueda++
        tareaBusqueda?.cancel()
        _uiState.update { it.copy(buscandoDireccion = false) }
    }
}