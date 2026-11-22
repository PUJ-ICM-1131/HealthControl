package com.icm2630.proyecto.data.model

import java.util.Calendar
import java.util.TimeZone

/** Las fechas del DatePicker son días civiles codificados como medianoche UTC. */
object FechasPendientes {
    private const val DIA = 86_400_000L
    fun dia(fecha: Long): Long = Math.floorDiv(fecha, DIA) * DIA

    fun hoy(ahora: Long = System.currentTimeMillis(), zona: TimeZone = TimeZone.getDefault()): Long {
        val local = Calendar.getInstance(zona).apply { timeInMillis = ahora }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    fun citaPendiente(fecha: Long, hoy: Long): Boolean = dia(fecha) >= hoy

    /** Hoy para tratamientos vigentes; inicio para futuros; null para finalizados. */
    fun proximoDia(medicamento: Medicamento, hoy: Long): Long? {
        val inicio = dia(medicamento.fechaInicioMillis)
        val proximo = maxOf(inicio, hoy)
        val fin = medicamento.fechaFinMillis?.let(::dia)
        if (!medicamento.tratamientoPermanente && fin != null && proximo > fin) return null
        return proximo
    }
}
