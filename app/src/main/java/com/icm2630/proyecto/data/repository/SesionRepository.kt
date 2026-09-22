package com.icm2630.proyecto.data.repository

import com.icm2630.proyecto.data.model.PerfilUsuario

object SesionRepository {

    var perfil: PerfilUsuario? = null

    /** Datos de cuenta capturados en el registro / login; el onboarding no los vuelve a pedir. */
    var nombreCuenta: String = ""
    var correoCuenta: String = ""

    val perfilConfigurado: Boolean
        get() = perfil?.configurado == true

    fun cerrarSesion() {
        perfil = null
        nombreCuenta = ""
        correoCuenta = ""
    }

    /**
     * Valida credenciales de forma simulada
     * Correo aceptado: test@gmail.com
     * Contraseña aceptada: 12345678
     */
    fun validarCredenciales(correo: String, password: String): Boolean {
        // Mock simple: campos no vacíos y coinciden con datos fijos
        val valido = correo == "test@gmail.com" && password == "12345678"
        if (valido) correoCuenta = correo
        return valido
    }
}