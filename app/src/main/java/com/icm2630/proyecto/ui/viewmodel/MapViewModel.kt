package com.icm2630.proyecto.ui.viewmodel

import android.app.Application
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
import com.google.firebase.firestore.ListenerRegistration
import com.icm2630.proyecto.data.model.PersonaVinculada
import com.icm2630.proyecto.data.repository.FamiliarSimulator
import com.icm2630.proyecto.data.repository.SesionRepository

/** Solo administra el mapa: no modifica perfiles, invitaciones ni familiares. */
class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val rutas = RutaRepository()
    private val direcciones = GeocodingRepository(application)
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    private var tareaRuta: Job? = null
    private var tareaBusqueda: Job? = null
    private var revisionRuta = 0
    private var revisionBusqueda = 0

    private var escuchaFamiliar: ListenerRegistration? = null

    /** Empieza a escuchar la ubicación simulada del familiar que se está consultando. */
    fun observarFamiliar(persona: PersonaVinculada) {
        if (escuchaFamiliar != null) return
        val uid = runCatching { SesionRepository.uidActual }.getOrNull() ?: return
        escuchaFamiliar = FamiliarSimulator.escucharUbicacion(
            uid = uid,
            persona = persona,
            onUbicacion = { punto, momento ->
                _uiState.update { it.copy(ubicacionFamiliar = punto, momentoFamiliarMillis = momento) }
            },
            onError = {
                _uiState.update { it.copy(mensajeError = "No se pudo cargar la ubicación de ${persona.nombre}.") }
            }
        )
    }

    override fun onCleared() {
        escuchaFamiliar?.remove()
        super.onCleared()
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
    }

    fun seleccionarObjetivo(seleccion: SeleccionMapa) {
        cancelarBusqueda()
        _uiState.update { it.copy(seleccion = seleccion, resultadosBusqueda = emptyList()) }
    }

    fun seleccionarPunto(punto: PuntoMapa) {
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

    fun limpiarRuta() {
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