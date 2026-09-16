package com.icm2630.proyecto.model

/**
 * HU-02: define el rol con el que el usuario usa la app.
 * Determina la interfaz y el destino después del onboarding.
 */
enum class TipoPerfil(val titulo: String, val descripcion: String) {
    INDIVIDUAL(
        titulo = "Persona individual",
        descripcion = "Gestiono mi propia salud: citas, medicamentos y signos vitales"
    ),
    ASOCIADO(
        titulo = "Persona asociada",
        descripcion = "Superviso la salud de un familiar cercano"
    )
}

enum class Genero(val etiqueta: String) {
    MASCULINO("Masculino"),
    FEMENINO("Femenino"),
    OTRO("Otro"),
    SIN_ESPECIFICAR("Prefiero no decirlo")
}

/**
 * Perfil que se arma en la pantalla de configuración inicial.
 * `configurado` es la bandera que decide si al iniciar sesión se va
 * al onboarding o directo al home.
 */
data class PerfilUsuario(
    val nombreCompleto: String = "",
    val fechaNacimientoMillis: Long? = null,
    val genero: Genero? = null,
    val contacto: String = "",
    val contactoEmergencia: String = "",
    val tipoPerfil: TipoPerfil = TipoPerfil.INDIVIDUAL,
    val condicionRelevante: String = "",
    val permisoUbicacion: Boolean = false,
    val notificacionesActivas: Boolean = true,
    val configurado: Boolean = false
)