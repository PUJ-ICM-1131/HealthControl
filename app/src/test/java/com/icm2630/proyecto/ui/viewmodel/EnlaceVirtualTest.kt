package com.icm2630.proyecto.ui.viewmodel

import org.junit.Assert.*
import org.junit.Test

class EnlaceVirtualTest {
    @Test fun normalizaEsquemaYPermiteDejarElEnlaceParaDespues() {
        assertTrue(AppointmentRegisterUiState().enlaceVirtualValido)
        val estado = AppointmentRegisterUiState(enlaceVirtual = "  meet.google.com/abc-defg-hij  ")
        assertEquals("https://meet.google.com/abc-defg-hij", estado.enlaceVirtualNormalizado)
        assertTrue(estado.enlaceVirtualValido)
        assertTrue(AppointmentRegisterUiState(enlaceVirtual = "HTTPS://meet.google.com/abc").enlaceVirtualValido)
    }
    @Test fun rechazaEspaciosEsquemasInvalidosYDireccionesSinServidor() {
        listOf("meet.google.com/a b", "ftp://example.com/a", "https://", "Google Meet", "https://example.com@otro.com")
            .forEach { assertFalse(it, AppointmentRegisterUiState(enlaceVirtual = it).enlaceVirtualValido) }
    }
}
