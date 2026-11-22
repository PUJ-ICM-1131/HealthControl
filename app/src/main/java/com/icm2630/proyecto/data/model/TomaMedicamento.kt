package com.icm2630.proyecto.data.model

/** Una confirmación corresponde a un horario de un medicamento en un día concreto. */
data class TomaMedicamento(
    val medicamentoId: String,
    val fechaMillis: Long,
    val horario: String,
    val registradaEnMillis: Long
) {
    val clave: String get() = clave(medicamentoId, fechaMillis, horario)

    companion object {
        fun clave(medicamentoId: String, fechaMillis: Long, horario: String) =
            "${medicamentoId}_${FechasPendientes.dia(fechaMillis)}_${horario}"
    }
}

data class TomaProgramada(val medicamento: Medicamento, val fechaMillis: Long, val horario: String) {
    val clave: String get() = TomaMedicamento.clave(medicamento.id, fechaMillis, horario)
}

object TomasProgramadas {
    fun minutos(horario: String): Int? {
        val match = Regex("""^(\d{1,2}):(\d{2})\s*([AaPp][Mm])?$""").matchEntire(horario.trim())
            ?: return null
        var hora = match.groupValues[1].toInt()
        val minuto = match.groupValues[2].toInt()
        val periodo = match.groupValues[3].uppercase()
        if (minuto !in 0..59 || (periodo.isEmpty() && hora !in 0..23)
            || (periodo.isNotEmpty() && hora !in 1..12)) return null
        if (periodo.isNotEmpty()) hora = hora % 12 + if (periodo == "PM") 12 else 0
        return hora * 60 + minuto
    }

    fun listar(medicamentos: List<Medicamento>, hoy: Long): List<TomaProgramada> =
        medicamentos.flatMap { medicamento ->
            val fecha = FechasPendientes.proximoDia(medicamento, hoy) ?: return@flatMap emptyList()
            medicamento.horarios.distinct().filter { minutos(it) != null }.map {
                TomaProgramada(medicamento, fecha, it)
            }
        }.sortedWith(compareBy({ it.fechaMillis }, { minutos(it.horario) }, { it.medicamento.nombre }))

    fun pendientes(medicamentos: List<Medicamento>, hoy: Long, tomadas: Set<String>) =
        listar(medicamentos, hoy).filter { it.clave !in tomadas }

    fun esDeHoy(medicamento: Medicamento, fecha: Long, horario: String, hoy: Long) =
        fecha == hoy && FechasPendientes.proximoDia(medicamento, hoy) == hoy &&
            horario in medicamento.horarios && minutos(horario) != null
}
