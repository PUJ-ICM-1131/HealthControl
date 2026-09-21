package com.icm2630.proyecto.data.repository

/**
 * No hay backend todavía (HU-03): esta utilidad centraliza cómo se genera
 * un código de invitación y cómo se "resuelve" uno al validarlo, para que
 * tanto el onboarding como Ajustes de perfil se comporten igual.
 */
object VinculacionSimulator {

    const val LONGITUD_CODIGO = 6

    private const val ALFABETO_CODIGO =
        "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"


    private val NOMBRES_SIMULADOS = listOf(
        "Elena Ramírez",
        "Carlos Gómez",
        "Rosa Martínez",
        "Jorge Fernández",
        "Lucía Torres",
        "Manuel Castro",
        "Ana Delgado",
        "Pedro Navarro"
    )


    private val TELEFONOS_SIMULADOS = listOf(
        "3001111111",
        "3002222222",
        "3003333333",
        "3004444444",
        "3005555555",
        "3006666666",
        "3007777777",
        "3008888888"
    )


    fun generarCodigo(): String =
        (1..LONGITUD_CODIGO)
            .map {
                ALFABETO_CODIGO.random()
            }
            .joinToString("")


    /**
     * Calcula siempre el mismo índice para un mismo código.
     *
     * De esta forma el mismo código obtiene siempre
     * el mismo nombre y el mismo teléfono simulados.
     */
    private fun indiceParaCodigo(
        codigo: String
    ): Int {

        return codigo
            .sumOf { it.code }
            .mod(
                NOMBRES_SIMULADOS.size
            )
    }


    /**
     * El mismo código siempre resuelve
     * al mismo nombre simulado.
     */
    fun nombreParaCodigo(
        codigo: String
    ): String {

        val indice =
            indiceParaCodigo(
                codigo
            )

        return NOMBRES_SIMULADOS[
            indice
        ]
    }


    /**
     * El mismo código siempre resuelve
     * al teléfono correspondiente a esa
     * persona simulada.
     */
    fun telefonoParaCodigo(
        codigo: String
    ): String {

        val indice =
            indiceParaCodigo(
                codigo
            )

        return TELEFONOS_SIMULADOS[
            indice
        ]
    }
}