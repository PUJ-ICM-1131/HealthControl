package com.icm2630.proyecto.ui.viewmodel

import com.icm2630.proyecto.data.model.ModoRuta
import com.icm2630.proyecto.data.model.PuntoMapa
import com.icm2630.proyecto.data.model.RutaMapa

data class PersonaEnMapa(
    val persona: com.icm2630.proyecto.data.model.PersonaVinculada,
    val punto: PuntoMapa? = null,
    val momentoMillis: Long? = null,
    val cargando: Boolean = true,
    val noDisponible: Boolean = false
)

enum class SeleccionMapa { ORIGEN, DESTINO }

data class MapUiState(
    val ubicacionActual: PuntoMapa? = null,
    val precisionMetros: Float? = null,
    val momentoUbicacionMillis: Long? = null,
    val origen: PuntoMapa? = null,
    val origenDesdeUbicacion: Boolean = true,
    val destino: PuntoMapa? = null,
    val seleccion: SeleccionMapa = SeleccionMapa.DESTINO,
    val modoRuta: ModoRuta = ModoRuta.CAMINANDO,
    val ruta: RutaMapa? = null,
    val calculandoRuta: Boolean = false,
    val consulta: String = "",
    val buscandoDireccion: Boolean = false,
    val resultadosBusqueda: List<PuntoMapa> = emptyList(),
    val personasEnMapa: List<PersonaEnMapa> = emptyList(),
    val personaSeleccionadaUid: String? = null,
    val ubicacionFamiliar: PuntoMapa? = null,
    val momentoFamiliarMillis: Long? = null,
    val mensajeError: String? = null,
    val titularCitaUid: String? = null,
    val cita: com.icm2630.proyecto.data.model.Cita? = null,
    val cargandoCita: Boolean = false,
    val guardandoLugar: Boolean = false,
    val mensajeLugar: String? = null
) {
    val destinoCitaConfirmado: Boolean
        get() {
            val guardado = cita?.let(com.icm2630.proyecto.data.model.UbicacionCitas::punto) ?: return false
            val elegido = destino ?: return false
            return guardado.latitud == elegido.latitud && guardado.longitud == elegido.longitud
        }
}