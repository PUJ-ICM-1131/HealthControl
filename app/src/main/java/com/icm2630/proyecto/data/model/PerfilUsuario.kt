package com.icm2630.proyecto.data.model

/**
 * define el rol con el que el usuario usa la app.
 * Determina la interfaz y el destino después del onboarding.
 *
 * - TITULAR: la app gestiona la salud de esta misma persona. Puede generar
 *   un código de vinculación para que un familiar o cuidador de confianza
 *   (un ACOMPANANTE) dé seguimiento a su salud.
 * - ACOMPANANTE: la persona no registra su propia salud aquí, sino que
 *   da seguimiento a la de un TITULAR ingresando el código que este le
 *   comparta (por ejemplo, un hijo que cuida a un padre mayor).
 */
enum class TipoPerfil(val titulo: String, val descripcion: String) {
    TITULAR(
        titulo = "Titular",
        descripcion = "Gestiono mi propia salud y puedo compartir mi seguimiento con un acompañante de confianza"
    ),
    ACOMPANANTE(
        titulo = "Acompañante",
        descripcion = "Doy seguimiento a la salud de un familiar mediante el código que él o ella me comparta"
    )
}

enum class Genero(val etiqueta: String) {
    MASCULINO("Masculino"),
    FEMENINO("Femenino"),
    OTRO("Otro"),
    SIN_ESPECIFICAR("Prefiero no decirlo")
}

/**
 * Representa al otro extremo de una vinculación titular-acompañante.
 * Vista desde un ACOMPANANTE: el titular al que está dando seguimiento.
 * Vista desde un TITULAR: uno de los acompañantes que ve su salud.
 */
data class PersonaVinculada(
    val nombre: String,
    val relacion: String,
    val codigo: String
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
    val tipoPerfil: TipoPerfil = TipoPerfil.TITULAR,
    val condicionRelevante: String = "",
    val permisoUbicacion: Boolean = false,
    val notificacionesActivas: Boolean = true,
    /** Código propio, vigente mientras no se genere uno nuevo (rol TITULAR). */
    val codigoVinculacion: String? = null,
    /** Acompañantes que, con ese código, quedaron vinculados a este titular. */
    val acompanantes: List<PersonaVinculada> = emptyList(),
    /** Si el rol es ACOMPANANTE, el titular al que se vinculó. */
    val personaVinculada: PersonaVinculada? = null, //cambiar esto
    val configurado: Boolean = false
)
