package com.icm2630.proyecto.data.repository

import com.icm2630.proyecto.data.model.PerfilUsuario

object SesionRepository {

    var perfil: PerfilUsuario? = null

    val perfilConfigurado: Boolean
        get() = perfil?.configurado == true

    fun cerrarSesion() {
        perfil = null
    }
}