package com.icm2630.proyecto.data.model

import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.*
import org.junit.Test

class UbicacionCitasTest {
    private val original = Cita(id = "cita", institucion = "Hospital", direccion = "Carrera 7, Bogotá",
        ubicacionConfirmada = GeoPoint(4.627, -74.064), nombreLugarConfirmado = "Entrada del hospital")

    @Test fun datosIncompletosSonUnaBusquedaYNoUnMarcadorInventado() {
        assertEquals("Hospital", UbicacionCitas.consulta(Cita(institucion = " Hospital ")))
        assertEquals("Bogotá", UbicacionCitas.consulta(Cita(direccion = "Bogotá")))
        assertEquals("", UbicacionCitas.consulta(Cita()))
        assertNull(UbicacionCitas.punto(Cita(institucion = "Hospital")))
    }

    @Test fun unaDireccionDistintaNoContaminaLaBusquedaPorNombre() {
        val cita = Cita(institucion = "  Clínica Marly  ", direccion = "Una dirección de otra ciudad")
        assertEquals("Clínica Marly", UbicacionCitas.consulta(cita))
        assertEquals("Una dirección de otra ciudad", UbicacionCitas.consulta(cita.copy(institucion = " ")))
    }

    @Test fun elPuntoConfirmadoMantieneElOrdenLatitudLongitud() {
        val punto = UbicacionCitas.punto(original)!!
        assertEquals(4.627, punto.latitud, 0.000001)
        assertEquals(-74.064, punto.longitud, 0.000001)
        assertEquals("Entrada del hospital", punto.nombre)
    }

    @Test fun cambiarDireccionInstitucionOModalidadRetiraLaConfirmacion() {
        for (editada in listOf(original.copy(direccion = "Otro lugar"),
            original.copy(institucion = "Otra sede"), original.copy(modalidad = ModalidadCita.VIRTUAL))) {
            val guardada = UbicacionCitas.conservar(editada, original)
            assertNull(guardada.ubicacionConfirmada)
            assertNull(guardada.nombreLugarConfirmado)
        }
    }

    @Test fun editarHoraONotasConservaLaUbicacionMasRecienteDelServidor() {
        val formulario = original.copy(hora = 15, notas = "Llevar orden", ubicacionConfirmada = null,
            nombreLugarConfirmado = null)
        val resultado = UbicacionCitas.conservar(formulario, original)
        assertEquals(original.ubicacionConfirmada, resultado.ubicacionConfirmada)
        assertEquals(original.nombreLugarConfirmado, resultado.nombreLugarConfirmado)
        assertEquals(15, resultado.hora)
    }

    @Test fun unaCitaNuevaNoHeredaCoordenadasDeOtra() {
        assertNull(UbicacionCitas.conservar(original.copy(id = "nueva"), null).ubicacionConfirmada)
        assertNull(UbicacionCitas.punto(original.copy(modalidad = ModalidadCita.VIRTUAL)))
    }
}
