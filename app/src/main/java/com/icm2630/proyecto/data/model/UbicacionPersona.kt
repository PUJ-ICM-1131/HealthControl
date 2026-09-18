package com.icm2630.proyecto.data.model


data class UbicacionPersona(

    /**
     * null = ubicación del propio usuario.
     * Si tiene valor, corresponde al id de una Persona asociada.
     */
    val personaId: String? = null,

    val latitud: Double,

    val longitud: Double,

    /**
     * Dirección aproximada que mostraremos en pantalla.
     */
    val direccion: String,

    val ciudad: String,

    /**
     * Momento de la última actualización.
     */
    val ultimaActualizacionMillis: Long,

    /**
     * Para esta primera entrega nos permite indicar
     * que la ubicación proviene de datos simulados.
     */
    val simulada: Boolean = true
)