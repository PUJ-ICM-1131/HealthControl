package com.icm2630.proyecto.ui.viewmodel

import com.icm2630.proyecto.data.model.ModoRuta
import com.icm2630.proyecto.data.model.PuntoMapa
import com.icm2630.proyecto.data.model.RutaMapa

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
    val ubicacionFamiliar: PuntoMapa? = null,
    val momentoFamiliarMillis: Long? = null,
    val mensajeError: String? = null
)