package com.icm2630.proyecto.data.model

import org.junit.Assert.*
import org.junit.Test

class TomasProgramadasTest {
    private val hoy = 1795305600000L
    private val med = Medicamento(id = "med-1", nombre = "Prueba", fechaInicioMillis = hoy,
        fechaFinMillis = hoy + 86400000L, horarios = listOf("8:00 AM", "8:00 PM"))

    @Test fun confirmarUnaTomaNoConfirmaTodoElMedicamento() {
        val primera = TomasProgramadas.listar(listOf(med), hoy).first()
        val pendientes = TomasProgramadas.pendientes(listOf(med), hoy, setOf(primera.clave))
        assertEquals(listOf("8:00 PM"), pendientes.map { it.horario })
    }
    @Test fun unaTomaDeAyerNoSeArrastraAlDiaSiguiente() {
        val ayer = TomaMedicamento.clave(med.id, hoy - 86400000L, "8:00 AM")
        assertEquals(2, TomasProgramadas.pendientes(listOf(med), hoy, setOf(ayer)).size)
    }
    @Test fun cadaMedicamentoTieneSuPropiaConfirmacion() {
        val otro = med.copy(id = "med-2")
        val tomada = TomaMedicamento.clave(med.id, hoy, "8:00 AM")
        assertEquals(3, TomasProgramadas.pendientes(listOf(med, otro), hoy, setOf(tomada)).size)
    }
    @Test fun noPermiteMarcarTomasFuturasNiFueraDelTratamiento() {
        assertFalse(TomasProgramadas.esDeHoy(med, hoy + 86400000L, "8:00 AM", hoy))
        assertFalse(TomasProgramadas.esDeHoy(med.copy(fechaFinMillis = hoy - 86400000L), hoy, "8:00 AM", hoy))
        assertFalse(TomasProgramadas.esDeHoy(med, hoy, "9:00 AM", hoy))
    }
    @Test fun ordenarHorariosUsaLaHoraYNoElOrdenEnQueSeAgregaron() {
        val horarios = TomasProgramadas.listar(listOf(med.copy(horarios = listOf("8:00 PM", "12:00 AM", "8:00 AM"))), hoy)
        assertEquals(listOf("12:00 AM", "8:00 AM", "8:00 PM"), horarios.map { it.horario })
        assertEquals(0, TomasProgramadas.minutos("12:00 AM"))
        assertEquals(720, TomasProgramadas.minutos("12:00 PM"))
        assertEquals(1200, TomasProgramadas.minutos("20:00"))
        assertNull(TomasProgramadas.minutos("25:00"))
        assertNull(TomasProgramadas.minutos("8:90 AM"))
    }
    @Test fun elUltimoDiaIncluyeLasTomasYElSiguienteLasExcluye() {
        assertEquals(2, TomasProgramadas.listar(listOf(med), hoy + 86400000L).size)
        assertTrue(TomasProgramadas.listar(listOf(med), hoy + 2 * 86400000L).isEmpty())
    }
    @Test fun deshacerRestauraUnicamenteEsaToma() {
        val programadas = TomasProgramadas.listar(listOf(med), hoy)
        val tomadas = programadas.map { it.clave }.toSet()
        assertTrue(TomasProgramadas.pendientes(listOf(med), hoy, tomadas).isEmpty())
        assertEquals(1, TomasProgramadas.pendientes(listOf(med), hoy, tomadas - programadas.first().clave).size)
    }
}
