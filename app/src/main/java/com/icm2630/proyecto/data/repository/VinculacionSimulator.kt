package com.icm2630.proyecto.data.repository

/**
 * No hay backend todavía (HU-03): esta utilidad centraliza cómo se genera
 * un código de invitación y cómo se "resuelve" uno al validarlo, para que
 * tanto el onboarding como Ajustes de perfil se comporten igual.
 */
object VinculacionSimulator {
    const val LONGITUD_CODIGO = 6
    private const val ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val NOMBRES_SIMULADOS = listOf(
        "Elena Ramírez", "Carlos Gómez", "Rosa Martínez", "Jorge Fernández",
        "Lucía Torres", "Manuel Castro", "Ana Delgado", "Pedro Navarro"
    )

    fun generarCodigo(): String =
        (1..LONGITUD_CODIGO).map { ALFABETO_CODIGO.random() }.joinToString("")

    /** El mismo código siempre resuelve al mismo nombre simulado. */
    fun nombreParaCodigo(codigo: String): String {
        val indice = codigo.sumOf { it.code }.mod(NOMBRES_SIMULADOS.size)
        return NOMBRES_SIMULADOS[indice]
    }
}
