package com.icm2630.proyecto.data.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class FechasHistorialTest {
    private fun fecha(dia: Int) = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(2026, Calendar.NOVEMBER, dia)
    }.timeInMillis
    private val hoy = fecha(22)

    @Test fun citaPasadaNoImplicaAsistenciaConfirmada() {
        assertEquals(FechasHistorial.EstadoCita.PASADA, FechasHistorial.estadoCita(fecha(13), hoy))
        assertEquals(FechasHistorial.EstadoCita.HOY, FechasHistorial.estadoCita(hoy + 3600000, hoy))
        assertEquals(FechasHistorial.EstadoCita.PROGRAMADA, FechasHistorial.estadoCita(fecha(24), hoy))
    }

    @Test fun tratamientoFuturoInclusoPermanenteEstaProgramado() {
        assertEquals(FechasHistorial.EstadoTratamiento.PROGRAMADO,
            FechasHistorial.estadoTratamiento(Medicamento(fechaInicioMillis = fecha(24), tratamientoPermanente = true), hoy))
    }

    @Test fun tratamientoFinalizaDespuesDelUltimoDiaIncluido() {
        val med = Medicamento(fechaInicioMillis = fecha(1), fechaFinMillis = hoy)
        assertEquals(FechasHistorial.EstadoTratamiento.VIGENTE, FechasHistorial.estadoTratamiento(med, hoy))
        assertEquals(FechasHistorial.EstadoTratamiento.FINALIZADO, FechasHistorial.estadoTratamiento(med, fecha(23)))
    }

    @Test fun filtrosIncluyenAmbosDiasYTodoElUltimoDia() {
        assertTrue(FechasHistorial.enRango(hoy + 20 * 3600000L, fecha(13), hoy))
        assertTrue(FechasHistorial.enRango(fecha(13), fecha(13), hoy))
        assertFalse(FechasHistorial.enRango(fecha(23), fecha(13), hoy))
        assertFalse(FechasHistorial.enRango(fecha(12), fecha(13), hoy))
    }

    @Test fun rangosAbiertosEInvertidos() {
        assertTrue(FechasHistorial.enRango(fecha(1), null, null))
        assertTrue(FechasHistorial.enRango(hoy, null, hoy))
        assertTrue(FechasHistorial.enRango(hoy, hoy, null))
        assertFalse(FechasHistorial.rangoValido(hoy, fecha(13)))
        assertFalse(FechasHistorial.enRango(hoy, hoy, fecha(13)))
    }

    @Test fun ultimosSieteYTreintaDiasIncluyenHoy() {
        assertEquals(fecha(16), FechasHistorial.desdeUltimosDias(hoy, 7))
        assertEquals(hoy - 29L * 86400000, FechasHistorial.desdeUltimosDias(hoy, 30))
        assertFalse(FechasHistorial.enRango(fecha(15), FechasHistorial.desdeUltimosDias(hoy, 7), hoy))
    }
}
