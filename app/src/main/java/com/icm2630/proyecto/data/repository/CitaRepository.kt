package com.icm2630.proyecto.data.repository

import com.icm2630.proyecto.data.model.Cita
import com.icm2630.proyecto.data.model.ModalidadCita
import com.icm2630.proyecto.data.model.TipoCita


object CitaRepository {

    /*
     * Datos simulados para la primera entrega.
     *
     * Se almacenan únicamente en memoria.
     * Al cerrar completamente la aplicación,
     * las citas creadas durante la ejecución se pierden.
     */
    private val citas = mutableListOf(

        // Cita del propio usuario
        Cita(
            id = "cita-1",
            personaId = null,
            tipo = TipoCita.MEDICINA_GENERAL,
            especialidad = "",
            motivo = "Control general",
            fechaMillis = 1790053200000L,
            hora = 9,
            minuto = 30,
            modalidad = ModalidadCita.PRESENCIAL,
            institucion = "Clínica del Country",
            direccion = "Carrera 16 # 82-57",
            enlaceVirtual = "",
            nombreMedico = "Dra. Laura Gómez",
            notas = "Llevar resultados de laboratorio",
            soporteUri = null
        ),

        // Cita de una persona asociada
        Cita(
            id = "cita-2",
            personaId = "persona-1",
            tipo = TipoCita.ESPECIALISTA,
            especialidad = "Cardiología",
            motivo = "Control de presión arterial",
            fechaMillis = 1790658000000L,
            hora = 14,
            minuto = 0,
            modalidad = ModalidadCita.VIRTUAL,
            institucion = "",
            direccion = "",
            enlaceVirtual = "https://meet.google.com/ejemplo",
            nombreMedico = "Dr. Andrés Pérez",
            notas = "",
            soporteUri = null
        )
    )


    // =========================================================
    // REGISTRAR CITA
    // =========================================================

    fun registrar(
        cita: Cita
    ) {
        citas.add(cita)
    }


    // =========================================================
    // ACTUALIZAR CITA
    // =========================================================

    fun actualizar(
        cita: Cita
    ) {

        val indice =
            citas.indexOfFirst { existente ->
                existente.id == cita.id
            }

        if (indice >= 0) {
            citas[indice] = cita
        }
    }


    // =========================================================
    // OBTENER TODAS
    // =========================================================

    fun obtenerTodas(): List<Cita> {
        return citas.toList()
    }


    // =========================================================
    // CITAS DEL PROPIO USUARIO
    // =========================================================

    fun obtenerPropias(): List<Cita> {

        return citas.filter { cita ->
            cita.personaId == null
        }
    }


    // =========================================================
    // CITAS DE UNA PERSONA ASOCIADA
    // =========================================================

    fun obtenerPorPersona(
        personaId: String
    ): List<Cita> {

        return citas.filter { cita ->
            cita.personaId == personaId
        }
    }


    // =========================================================
    // BUSCAR CITA POR ID
    // =========================================================

    fun obtenerPorId(
        citaId: String
    ): Cita? {

        return citas.find { cita ->
            cita.id == citaId
        }
    }


    // =========================================================
    // ELIMINAR CITA
    // =========================================================

    fun eliminar(
        citaId: String
    ) {

        citas.removeAll { cita ->
            cita.id == citaId
        }
    }
}