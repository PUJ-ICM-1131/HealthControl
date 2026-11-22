package com.icm2630.proyecto.data.model

import com.google.firebase.firestore.util.CustomClassMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SerializacionRegistrosTest {
    @Test
    fun citaSoloEnviaLosCamposPermitidosPorFirestore() {
        val cita = Cita(
            id = "cita-prueba",
            soporteUri = "content://com.icm2630.proyecto.fileprovider/soportes/soporte.jpg"
        )

        val datos = CustomClassMapper.convertToPlainJavaTypes(cita) as Map<*, *>

        assertEquals(
            setOf(
                "id", "personaId", "tipo", "especialidad", "motivo", "fechaMillis",
                "hora", "minuto", "modalidad", "institucion", "direccion",
                "enlaceVirtual", "nombreMedico", "notas", "nombreSoporte", "auditoria"
            ),
            datos.keys
        )
        assertFalse(datos.containsKey("soporteUri"))
        assertEquals("MEDICINA_GENERAL", datos["tipo"])
    }

    @Test
    fun medicamentoSoloEnviaLosCamposPermitidosPorFirestore() {
        val medicamento = Medicamento(
            id = "medicamento-prueba",
            ordenMedicaUri = "content://com.icm2630.proyecto.fileprovider/soportes/orden.pdf"
        )

        val datos = CustomClassMapper.convertToPlainJavaTypes(medicamento) as Map<*, *>

        assertEquals(
            setOf(
                "id", "personaId", "nombre", "forma", "dosis", "unidad",
                "cantidadPorToma", "horarios", "fechaInicioMillis", "fechaFinMillis",
                "tratamientoPermanente", "indicaciones", "nombreOrdenMedica", "auditoria"
            ),
            datos.keys
        )
        assertFalse(datos.containsKey("ordenMedicaUri"))
        assertEquals("PASTILLA", datos["forma"])
    }
}
