package com.icm2630.proyecto.data.model

/** Estados derivados de fechas: no certifican asistencia ni consumo de medicamentos. */
object FechasHistorial {
    enum class EstadoCita { PASADA, HOY, PROGRAMADA }
    enum class EstadoTratamiento { PROGRAMADO, VIGENTE, FINALIZADO }

    fun estadoCita(fecha: Long, hoy: Long): EstadoCita = when {
        FechasPendientes.dia(fecha) < hoy -> EstadoCita.PASADA
        FechasPendientes.dia(fecha) == hoy -> EstadoCita.HOY
        else -> EstadoCita.PROGRAMADA
    }

    fun estadoTratamiento(medicamento: Medicamento, hoy: Long): EstadoTratamiento = when {
        FechasPendientes.dia(medicamento.fechaInicioMillis) > hoy -> EstadoTratamiento.PROGRAMADO
        !medicamento.tratamientoPermanente && medicamento.fechaFinMillis?.let {
            FechasPendientes.dia(it) < hoy
        } == true -> EstadoTratamiento.FINALIZADO
        else -> EstadoTratamiento.VIGENTE
    }

    fun rangoValido(desde: Long?, hasta: Long?): Boolean =
        desde == null || hasta == null || FechasPendientes.dia(desde) <= FechasPendientes.dia(hasta)

    fun enRango(fecha: Long, desde: Long?, hasta: Long?): Boolean {
        val dia = FechasPendientes.dia(fecha)
        return rangoValido(desde, hasta) &&
            (desde == null || dia >= FechasPendientes.dia(desde)) &&
            (hasta == null || dia <= FechasPendientes.dia(hasta))
    }

    /** Incluye hoy: siete días son hoy y los seis días anteriores. */
    fun desdeUltimosDias(hoy: Long, dias: Int): Long {
        require(dias > 0)
        return hoy - (dias - 1L) * 86_400_000L
    }
}
