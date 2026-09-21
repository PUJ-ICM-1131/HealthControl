
package com.icm2630.proyecto.data.repository

import com.icm2630.proyecto.data.model.FormaMedicamento
import com.icm2630.proyecto.data.model.Medicamento


object MedicamentoRepository {

    /*
     * Datos simulados para la primera entrega.
     *
     * Por ahora se almacenan únicamente en memoria.
     * Al cerrar completamente la aplicación se pierden
     * los medicamentos registrados durante la ejecución.
     */
    private val medicamentos = mutableListOf(

        // Medicamento del propio usuario
        Medicamento(
            id = "medicamento-1",
            personaId = null,
            nombre = "Losartán",
            forma = FormaMedicamento.PASTILLA,
            dosis = "50",
            unidad = "mg",
            cantidadPorToma = "1",
            horarios = listOf(
                "8:00 AM"
            ),
            fechaInicioMillis = 1758067200000L,
            fechaFinMillis = null,
            tratamientoPermanente = true,
            indicaciones = "Tomar después del desayuno",
            ordenMedicaUri = null
        ),

        // Medicamento de una persona asociada
        Medicamento(
            id = "medicamento-2",
            personaId = "persona-1",
            nombre = "Acetaminofén",
            forma = FormaMedicamento.PASTILLA,
            dosis = "500",
            unidad = "mg",
            cantidadPorToma = "1",
            horarios = listOf(
                "8:00 AM",
                "8:00 PM"
            ),
            fechaInicioMillis = 1758067200000L,
            fechaFinMillis = 1758672000000L,
            tratamientoPermanente = false,
            indicaciones = "Tomar después de los alimentos",
            ordenMedicaUri = null
        )
    )

    // REGISTRAR MEDICAMENTO
    fun registrar(medicamento: Medicamento) {
        medicamentos.add(medicamento)
    }

    // ACTUALIZAR MEDICAMENTO
    fun actualizar(medicamento: Medicamento) {
        val indice = medicamentos.indexOfFirst { existente ->
                existente.id == medicamento.id
            }

        if (indice >= 0) {
            medicamentos[indice] = medicamento
        }
    }

    // OBTENER TODOS
    fun obtenerTodos(): List<Medicamento> {
        return medicamentos.toList()
    }

    // MEDICAMENTOS DEL PROPIO USUARIO
    fun obtenerPropios(): List<Medicamento> {
        return medicamentos.filter { medicamento ->
            medicamento.personaId == null
        }
    }

    // MEDICAMENTOS DE UNA PERSONA ASOCIADA
    fun obtenerPorPersona(personaId: String): List<Medicamento> {
        return medicamentos.filter { medicamento ->
            medicamento.personaId == personaId
        }
    }

    // BUSCAR MEDICAMENTO POR ID
    fun obtenerPorId(medicamentoId: String): Medicamento? {
        return medicamentos.find { medicamento ->
            medicamento.id == medicamentoId
        }
    }

    // ELIMINAR
    fun eliminar(medicamentoId: String) {
        medicamentos.removeAll { medicamento ->
            medicamento.id == medicamentoId
        }
    }
}