package com.icm2630.proyecto.data.model

/** El texto de una institución es una búsqueda, no una ubicación verificada. */
object UbicacionCitas {
    // El nombre y la dirección se prueban por separado: uno puede estar equivocado.
    fun consulta(cita: Cita): String = cita.institucion.trim().ifBlank { cita.direccion.trim() }

    fun mismoLugar(a: Cita, b: Cita): Boolean =
        a.modalidad == ModalidadCita.PRESENCIAL && b.modalidad == ModalidadCita.PRESENCIAL &&
            a.institucion == b.institucion && a.direccion == b.direccion

    fun punto(cita: Cita): PuntoMapa? {
        if (cita.modalidad != ModalidadCita.PRESENCIAL) return null
        val coordenada = cita.ubicacionConfirmada ?: return null
        val nombre = cita.nombreLugarConfirmado?.takeIf { it.isNotBlank() } ?: return null
        return PuntoMapa(coordenada.latitude, coordenada.longitude, nombre)
    }

    /** Una edición conserva el punto del servidor solo si sigue describiendo el mismo lugar. */
    fun conservar(cita: Cita, anterior: Cita?): Cita {
        val conservar = anterior != null && mismoLugar(cita, anterior)
        return cita.copy(
            ubicacionConfirmada = if (conservar) anterior?.ubicacionConfirmada else null,
            nombreLugarConfirmado = if (conservar) anterior?.nombreLugarConfirmado else null
        )
    }
}
