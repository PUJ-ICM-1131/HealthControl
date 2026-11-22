package com.icm2630.proyecto.data.model

import org.junit.Assert.*
import org.junit.Test

class InvitacionGestionTest {
    private val invitacion = Invitacion("c_a", "cuidador", "abuelo", "Cuidador", "Abuelo",
        EstadoInvitacion.ACEPTADA, solicitaGestion = true, titularGestionUid = "abuelo")

    @Test fun aceptarNoDaGestionEnAmbasDirecciones() {
        assertTrue(invitacion.permiteGestionar("cuidador", "abuelo"))
        assertFalse(invitacion.permiteGestionar("abuelo", "cuidador"))
        assertFalse(invitacion.permiteGestionar("otro", "abuelo"))
        assertFalse(invitacion.permiteGestionar("cuidador", "otro"))
    }
    @Test fun unVinculoAnteriorNoConcedeGestion() {
        assertFalse(invitacion.copy(solicitaGestion = false, titularGestionUid = "")
            .permiteGestionar("cuidador", "abuelo"))
    }
    @Test fun retirarPermisoORevocarImpideGestionar() {
        assertFalse(invitacion.copy(titularGestionUid = "").permiteGestionar("cuidador", "abuelo"))
        for (estado in EstadoInvitacion.entries.filter { it != EstadoInvitacion.ACEPTADA }) {
            assertFalse(invitacion.copy(estado = estado).permiteGestionar("cuidador", "abuelo"))
        }
    }
}
