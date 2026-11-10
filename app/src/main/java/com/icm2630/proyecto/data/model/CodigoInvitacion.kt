package com.icm2630.proyecto.data.model

import java.security.SecureRandom
import java.util.Locale

/** El código permite solicitar el vínculo; nunca sustituye el UID ni concede permisos. */
object CodigoInvitacion {
    const val LONGITUD = 6
    private const val ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val random = SecureRandom()

    fun generar(): String = buildString {
        repeat(LONGITUD) { append(ALFABETO[random.nextInt(ALFABETO.length)]) }
    }

    fun normalizar(codigo: String): String = codigo.trim().uppercase(Locale.ROOT)

    fun esValido(codigo: String): Boolean =
        codigo.length == LONGITUD && codigo.all { it in ALFABETO }
}
