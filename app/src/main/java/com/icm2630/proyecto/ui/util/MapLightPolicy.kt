package com.icm2630.proyecto.ui.util

data class IluminacionMapa(
    val sensorDisponible: Boolean = false,
    val lux: Float? = null,
    val pocaLuz: Boolean = false
)

/** Primera lectura: 50 lx. Después: entra bajo 30 lx y sale desde 70 lx. */
internal fun actualizarIluminacionMapa(anterior: IluminacionMapa, lux: Float): IluminacionMapa {
    if (!lux.isFinite() || lux < 0f) return anterior
    val pocaLuz = when {
        anterior.lux == null -> lux < 50f
        anterior.pocaLuz -> lux < 70f
        else -> lux < 30f
    }
    return IluminacionMapa(sensorDisponible = true, lux = lux, pocaLuz = pocaLuz)
}

// El modo oscuro del celular tiene prioridad, incluso con mucha luz ambiental.
internal fun usarMapaOscuro(oscuroSistema: Boolean, luz: IluminacionMapa): Boolean =
    oscuroSistema || (luz.sensorDisponible && luz.pocaLuz)