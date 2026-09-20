package com.icm2630.proyecto.data.repository

import com.icm2630.proyecto.data.model.PerfilUsuario

object SesionRepository {

    var perfil: PerfilUsuario? = null

    val perfilConfigurado: Boolean
        get() = perfil?.configurado == true

    fun cerrarSesion() {
        perfil = null
    }

    /**
     * Valida credenciales de forma simulada.
     * Correo aceptado: test@gmail.com
     * Contraseña aceptada: 12345678
     */
    fun validarCredenciales(correo: String, password: String): Boolean {
        // Mock simple: campos no vacíos y coinciden con datos fijos
        return correo == "test@gmail.com" && password == "12345678"
    }
}