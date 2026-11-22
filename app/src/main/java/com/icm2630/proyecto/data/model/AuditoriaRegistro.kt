package com.icm2630.proyecto.data.model

import com.google.firebase.Timestamp

/** Los registros anteriores a esta función no inventan un autor ni una fecha de creación. */
data class AuditoriaRegistro(
    val creadoPorUid: String? = null,
    val creadoPorNombre: String? = null,
    val creadoEn: Timestamp? = null,
    val actualizadoPorUid: String = "",
    val actualizadoPorNombre: String = "",
    val actualizadoEn: Timestamp? = null
)
