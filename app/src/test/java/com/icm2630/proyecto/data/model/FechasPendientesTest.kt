package com.icm2630.proyecto.data.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class FechasPendientesTest {
    private fun fecha(dia: Int) = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(2026, Calendar.NOVEMBER, dia)
    }.timeInMillis
    private val hoy = fecha(22)

    @Test fun citasAnterioresNoSonPendientes() {
        assertFalse(FechasPendientes.citaPendiente(fecha(13), hoy))
        assertTrue(FechasPendientes.citaPendiente(hoy, hoy))
        assertTrue(FechasPendientes.citaPendiente(fecha(24), hoy))
    }

    @Test fun tratamientoFinalizadoNoGeneraTomasDeHoy() {
        val med = Medicamento(fechaInicioMillis = fecha(1), fechaFinMillis = fecha(13))
        assertNull(FechasPendientes.proximoDia(med, hoy))
    }

    @Test fun ultimoDiaDelTratamientoSigueIncluido() {
        val med = Medicamento(fechaInicioMillis = fecha(1), fechaFinMillis = hoy)
        assertEquals(hoy, FechasPendientes.proximoDia(med, hoy))
    }

    @Test fun tratamientoFuturoApareceEnSuFechaDeInicio() {
        val med = Medicamento(fechaInicioMillis = fecha(24), fechaFinMillis = fecha(28))
        assertEquals(fecha(24), FechasPendientes.proximoDia(med, hoy))
    }

    @Test fun permanenteTieneTomasHoyPeroNoAntesDelInicio() {
        assertEquals(hoy, FechasPendientes.proximoDia(
            Medicamento(fechaInicioMillis = fecha(1), tratamientoPermanente = true), hoy))
        assertEquals(fecha(24), FechasPendientes.proximoDia(
            Medicamento(fechaInicioMillis = fecha(24), tratamientoPermanente = true), hoy))
    }

    @Test fun hoyRespetaElDiaLocalAunqueUtcYaSeaManana() {
        val instante = fecha(23) + 2 * 60 * 60 * 1000L
        assertEquals(hoy, FechasPendientes.hoy(instante, TimeZone.getTimeZone("America/Bogota")))
    }

    @Test fun normalizaLaHoraParaAgruparCitasDelMismoDia() {
        assertEquals(hoy, FechasPendientes.dia(hoy + 14 * 60 * 60 * 1000L))
    }
}
