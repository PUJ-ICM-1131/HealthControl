package com.icm2630.proyecto.data.repository

import com.icm2630.proyecto.data.model.Persona

object PersonaRepository {

    private val personasAsociadas = mutableListOf(

        Persona(
            id = "persona-1",
            nombre = "Carlos",
            apellido = "Rodríguez"
        ),

        Persona(
            id = "persona-2",
            nombre = "María",
            apellido = "Rodríguez"
        )
    )


    fun obtenerPersonasAsociadas(): List<Persona> {
        return personasAsociadas.toList()
    }


    fun obtenerPorId(
        personaId: String
    ): Persona? {

        return personasAsociadas.find { persona ->
            persona.id == personaId
        }
    }
}