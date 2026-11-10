package com.icm2630.proyecto.data.model

enum class Genero(val etiqueta: String) {
    MASCULINO("Masculino"),
    FEMENINO("Femenino"),
    OTRO("Otro"),
    SIN_ESPECIFICAR("Prefiero no decirlo")
}

enum class TipoSangre(val etiqueta: String) {
    A_POSITIVO("A+"),
    A_NEGATIVO("A-"),
    B_POSITIVO("B+"),
    B_NEGATIVO("B-"),
    AB_POSITIVO("AB+"),
    AB_NEGATIVO("AB-"),
    O_POSITIVO("O+"),
    O_NEGATIVO("O-")
}

/** Opciones rápidas para el selector de condiciones; lo demás va en "otra condición". */
val CONDICIONES_COMUNES = listOf(
    "Diabetes",
    "Hipertensión",
    "Asma",
    "Cardiopatía",
    "Colesterol alto",
    "Artritis",
    "Hipotiroidismo"
)

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
    val telefonoEmergencia: String = "",
    val tipoSangre: TipoSangre? = null,
    val alergias: String = "",
    /** Condiciones elegidas de [CONDICIONES_COMUNES]. */
    val condiciones: List<String> = emptyList(),
    /** Texto libre para cualquier otra condición que no esté en la lista. */
    val condicionRelevante: String = "",
    /** Preferencia de uso; el permiso real siempre se consulta a Android. */
    val permisoUbicacion: Boolean = false,
    val notificacionesActivas: Boolean = true,
    /** Consentimiento independiente del permiso de Android. Solo se publica con el mapa visible. */
    val compartirUbicacion: Boolean = false,
    val fotoUri: String? = null,
    /** Código propio reservado para invitar a otras cuentas. */
    val codigoVinculacion: String? = null,
    /** Proyección local de vínculos aceptados; nunca se usa como autorización en Firestore. */
    val acompanantes: List<PersonaVinculada> = emptyList(),
    val configurado: Boolean = false
)
