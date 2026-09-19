package com.icm2630.proyecto.data.repository

import com.icm2630.proyecto.data.model.UbicacionPersona


object UbicacionRepository {

    /*
     * Datos simulados de ubicación para la primera entrega.
     *
     * Más adelante este Repository podría obtener información
     * desde GPS, Firebase, una API o algún servicio de ubicación.
     */
    private val ubicaciones = mutableListOf(

        // Ubicación del propio usuario
        UbicacionPersona(
            personaId = null,
            latitud = 4.6533,
            longitud = -74.0836,
            direccion = "Carrera 7 con Calle 72",
            ciudad = "Bogotá",
            ultimaActualizacionMillis =
                System.currentTimeMillis() - 5 * 60 * 1000,
            simulada = true
        ),

        // Ubicación de Carlos Rodríguez
        UbicacionPersona(
            personaId = "persona-1",
            latitud = 4.6766,
            longitud = -74.0488,
            direccion = "Calle 93 con Carrera 15",
            ciudad = "Bogotá",
            ultimaActualizacionMillis =
                System.currentTimeMillis() - 8 * 60 * 1000,
            simulada = true
        ),

        // Ubicación de María Rodríguez
        UbicacionPersona(
            personaId = "persona-2",
            latitud = 4.6486,
            longitud = -74.0779,
            direccion = "Carrera 13 con Calle 63",
            ciudad = "Bogotá",
            ultimaActualizacionMillis =
                System.currentTimeMillis() - 12 * 60 * 1000,
            simulada = true
        )
    )


    // =========================================================
    // UBICACIÓN DEL PROPIO USUARIO
    // =========================================================

    fun obtenerUbicacionPropia(): UbicacionPersona? {

        return ubicaciones.find { ubicacion ->
            ubicacion.personaId == null
        }
    }


    // =========================================================
    // UBICACIÓN DE PERSONA ASOCIADA
    // =========================================================

    fun obtenerPorPersona(
        personaId: String
    ): UbicacionPersona? {

        return ubicaciones.find { ubicacion ->
            ubicacion.personaId == personaId
        }
    }


    // =========================================================
    // ACTUALIZAR UBICACIÓN MOCK
    // =========================================================

    fun actualizar(
        nuevaUbicacion: UbicacionPersona
    ) {

        val indice =
            ubicaciones.indexOfFirst { ubicacion ->
                ubicacion.personaId ==
                        nuevaUbicacion.personaId
            }


        if (indice >= 0) {

            ubicaciones[indice] =
                nuevaUbicacion

        } else {

            ubicaciones.add(
                nuevaUbicacion
            )
        }
    }
}