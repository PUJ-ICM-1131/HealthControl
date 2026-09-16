package com.icm2630.proyecto.repository

import com.icm2630.proyecto.model.PerfilUsuario

object SesionRepository {
    var perfil: PerfilUsuario? = null
    val perfilConfigurado: Boolean get() = perfil?.configurado == true

    fun cerrarSesion() {
        perfil = null
    }
}