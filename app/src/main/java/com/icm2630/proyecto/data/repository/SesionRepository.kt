package com.icm2630.proyecto.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.PersonaVinculada

object SesionRepository {

    var perfil: PerfilUsuario? = null

    /** Datos de cuenta capturados en el registro / login; el onboarding no los vuelve a pedir. */
    var nombreCuenta: String = ""
    var correoCuenta: String = ""

    /**
     * Familiar cuyo HealthControl se está consultando (solo lectura).
     * null = se está viendo el propio perfil. Es estado de Compose para que
     * todas las pantallas se actualicen al cambiar de perfil.
     */
    var familiarActivo by mutableStateOf<PersonaVinculada?>(null)
        private set

    /** Cuenta cuyos datos muestran los repositorios: null = la propia. */
    val cuentaActiva: String?
        get() = familiarActivo?.codigo

    val perfilConfigurado: Boolean
        get() = perfil?.configurado == true

    fun verFamiliar(persona: PersonaVinculada) {
        FamiliarSimulator.asegurarDatos(persona.codigo)
        familiarActivo = persona
    }

    fun volverAMiPerfil() {
        familiarActivo = null
    }

    fun cerrarSesion() {
        perfil = null
        nombreCuenta = ""
        correoCuenta = ""
        familiarActivo = null
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
