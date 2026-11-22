package com.icm2630.proyecto.notificaciones

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.icm2630.proyecto.data.model.Cita
import com.icm2630.proyecto.data.model.ModalidadCita
import com.icm2630.proyecto.data.model.Medicamento
import java.util.Calendar
import java.util.TimeZone

/**
 * Programa con AlarmManager los recordatorios del usuario que inició sesión:
 *  - Una alarma por cada horario de cada medicamento (se repite cada día hasta la fecha fin).
 *  - Una alarma por cita, ANTICIPACION_CITA_MIN minutos antes.
 *
 * Se reprograma cada vez que Firestore entrega la lista actualizada de citas o medicamentos,
 * así que crear, editar o eliminar un registro actualiza sus recordatorios.
 */
object RecordatoriosProgramados {

    const val ANTICIPACION_CITA_MIN = 60

    internal const val ACCION = "com.icm2630.proyecto.RECORDATORIO"
    internal const val EXTRA_CLAVE = "clave"
    internal const val EXTRA_TIPO = "tipo"
    internal const val EXTRA_TITULO = "titulo"
    internal const val EXTRA_TEXTO = "texto"
    internal const val EXTRA_MOMENTO = "momento"
    internal const val EXTRA_FIN = "fin"

    private const val TIPO_MEDICAMENTO = "medicamento"
    private const val TIPO_CITA = "cita"
    private const val PREFS = "recordatorios_programados"
    private const val DIA_MS = 24L * 60 * 60 * 1000

    private data class Alarma(
        val clave: String,
        val momento: Long,
        val titulo: String,
        val texto: String,
        /** Solo medicamentos: último instante en que puede repetirse (null = permanente). */
        val finMillis: Long? = null
    )

    fun programarMedicamentos(medicamentos: List<Medicamento>) {
        val context = Notificaciones.contexto ?: return
        val ahora = System.currentTimeMillis()
        val alarmas = medicamentos.flatMap { medicamento ->
            medicamento.horarios.mapNotNull { horario -> alarmaDeToma(medicamento, horario, ahora) }
        }
        reemplazar(context, TIPO_MEDICAMENTO, alarmas)
    }

    fun programarCitas(citas: List<Cita>) {
        val context = Notificaciones.contexto ?: return
        val ahora = System.currentTimeMillis()
        val disparadas = prefs(context).getStringSet("disparadas", emptySet()).orEmpty()
        val alarmas = citas.mapNotNull { alarmaDeCita(it, ahora) }.filter { it.clave !in disparadas }
        reemplazar(context, TIPO_CITA, alarmas)
    }

    /** Al cerrar sesión: los recordatorios de esa cuenta no deben seguir sonando. */
    fun cancelarTodo() {
        val context = Notificaciones.contexto ?: return
        reemplazar(context, TIPO_MEDICAMENTO, emptyList())
        reemplazar(context, TIPO_CITA, emptyList())
    }

    /** Lo llama RecordatorioReceiver después de mostrar la notificación. */
    internal fun despuesDeDisparar(context: Context, intent: Intent) {
        val clave = intent.getStringExtra(EXTRA_CLAVE) ?: return
        val tipo = intent.getStringExtra(EXTRA_TIPO) ?: return
        val prefs = prefs(context)
        if (tipo == TIPO_MEDICAMENTO) {
            // Siguiente toma: mismo horario al día siguiente, si el tratamiento sigue vigente.
            val fin = intent.getLongExtra(EXTRA_FIN, -1L).takeIf { it >= 0 }
            var siguiente = intent.getLongExtra(EXTRA_MOMENTO, System.currentTimeMillis()) + DIA_MS
            while (siguiente <= System.currentTimeMillis()) siguiente += DIA_MS
            if (fin == null || siguiente <= fin) {
                programar(context, TIPO_MEDICAMENTO, Alarma(
                    clave = clave,
                    momento = siguiente,
                    titulo = intent.getStringExtra(EXTRA_TITULO).orEmpty(),
                    texto = intent.getStringExtra(EXTRA_TEXTO).orEmpty(),
                    finMillis = fin
                ))
                return
            }
        } else {
            // Una cita avisa una sola vez, aunque la app se vuelva a abrir antes de la hora.
            val disparadas = prefs.getStringSet("disparadas", emptySet()).orEmpty() + clave
            prefs.edit().putStringSet("disparadas", disparadas).apply()
        }
        val claves = prefs.getStringSet("claves_$tipo", emptySet()).orEmpty() - clave
        prefs.edit().putStringSet("claves_$tipo", claves).apply()
    }

    // ── Construcción de alarmas ────────────────────────────────────────────

    private fun alarmaDeToma(medicamento: Medicamento, horario: String, ahora: Long): Alarma? {
        val (hora, minuto) = parsearHora(horario) ?: return null
        val inicio = medicamento.fechaInicioMillis.takeIf { it > 0 }?.let { diaLocal(it, inicioDelDia = true) }
        val fin = if (medicamento.tratamientoPermanente) null
        else medicamento.fechaFinMillis?.let { diaLocal(it, inicioDelDia = false) }

        val proxima = Calendar.getInstance().apply {
            timeInMillis = maxOf(ahora, inicio ?: ahora)
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (proxima.timeInMillis <= ahora) proxima.add(Calendar.DAY_OF_MONTH, 1)
        if (fin != null && proxima.timeInMillis > fin) return null

        val cantidad = listOf(
            medicamento.cantidadPorToma.trim(),
            "${medicamento.dosis} ${medicamento.unidad}".trim()
        ).filter { it.isNotBlank() }.joinToString(" · ")
        return Alarma(
            clave = "med:${medicamento.id}:$horario",
            momento = proxima.timeInMillis,
            titulo = "Hora de tu medicamento",
            texto = buildString {
                append(medicamento.nombre.ifBlank { "Medicamento" })
                append(" (").append(medicamento.forma.titulo.lowercase()).append(")")
                if (cantidad.isNotBlank()) append(": ").append(cantidad)
                append(" · ").append(horario)
                if (medicamento.indicaciones.isNotBlank()) append("\n").append(medicamento.indicaciones)
            },
            finMillis = fin
        )
    }

    private fun alarmaDeCita(cita: Cita, ahora: Long): Alarma? {
        if (cita.fechaMillis <= 0) return null
        val momentoCita = Calendar.getInstance().apply {
            timeInMillis = diaLocal(cita.fechaMillis, inicioDelDia = true)
            set(Calendar.HOUR_OF_DAY, cita.hora)
            set(Calendar.MINUTE, cita.minuto)
        }.timeInMillis
        if (momentoCita <= ahora) return null
        // Si la cita se registró con menos anticipación, avisa enseguida.
        val aviso = maxOf(momentoCita - ANTICIPACION_CITA_MIN * 60_000L, ahora + 5_000L)
        val lugar = when {
            cita.modalidad == ModalidadCita.VIRTUAL -> "Cita virtual"
            cita.institucion.isNotBlank() -> cita.institucion
            else -> cita.direccion
        }
        return Alarma(
            clave = "cita:${cita.id}:$momentoCita",
            momento = aviso,
            titulo = "Tienes una cita a las ${horaLegible(cita.hora, cita.minuto)}",
            texto = buildString {
                append(cita.tipo.titulo)
                if (cita.especialidad.isNotBlank()) append(" · ").append(cita.especialidad)
                if (lugar.isNotBlank()) append("\n").append(lugar)
                if (cita.nombreMedico.isNotBlank()) append("\nCon ").append(cita.nombreMedico)
            }
        )
    }

    // ── AlarmManager ───────────────────────────────────────────────────────

    private fun reemplazar(context: Context, tipo: String, alarmas: List<Alarma>) {
        val prefs = prefs(context)
        prefs.getStringSet("claves_$tipo", emptySet()).orEmpty().forEach { cancelar(context, it) }
        alarmas.forEach { programar(context, tipo, it) }
        prefs.edit().putStringSet("claves_$tipo", alarmas.map { it.clave }.toSet()).apply()
    }

    private fun programar(context: Context, tipo: String, alarma: Alarma) {
        val intent = Intent(context, RecordatorioReceiver::class.java)
            .setAction(ACCION)
            .putExtra(EXTRA_CLAVE, alarma.clave)
            .putExtra(EXTRA_TIPO, tipo)
            .putExtra(EXTRA_TITULO, alarma.titulo)
            .putExtra(EXTRA_TEXTO, alarma.texto)
            .putExtra(EXTRA_MOMENTO, alarma.momento)
            .putExtra(EXTRA_FIN, alarma.finMillis ?: -1L)
        val pendiente = PendingIntent.getBroadcast(
            context, alarma.clave.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmas = context.getSystemService(AlarmManager::class.java)
        val exacta = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmas.canScheduleExactAlarms()
        try {
            if (exacta) alarmas.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarma.momento, pendiente)
            else alarmas.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarma.momento, pendiente)
        } catch (_: SecurityException) {
            alarmas.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarma.momento, pendiente)
        }
    }

    private fun cancelar(context: Context, clave: String) {
        val intent = Intent(context, RecordatorioReceiver::class.java).setAction(ACCION)
        PendingIntent.getBroadcast(
            context, clave.hashCode(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )?.let { pendiente ->
            context.getSystemService(AlarmManager::class.java).cancel(pendiente)
            pendiente.cancel()
        }
    }

    // ── Utilidades de fecha y hora ─────────────────────────────────────────

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** "8:05 AM", "8:05 p. m." o "20:05" → (20, 5). */
    private fun parsearHora(texto: String): Pair<Int, Int>? {
        val coincidencia = Regex("""(\d{1,2}):(\d{2})\s*([AaPp])?""").find(texto) ?: return null
        var hora = coincidencia.groupValues[1].toInt()
        val minuto = coincidencia.groupValues[2].toInt()
        when (coincidencia.groupValues[3].lowercase()) {
            "a" -> if (hora == 12) hora = 0
            "p" -> if (hora < 12) hora += 12
        }
        return if (hora in 0..23 && minuto in 0..59) hora to minuto else null
    }

    /**
     * Los DatePicker de Material 3 guardan la medianoche en UTC.
     * Se toma ese día del calendario y se lleva a la zona horaria del celular.
     */
    private fun diaLocal(utcMillis: Long, inicioDelDia: Boolean): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
        return Calendar.getInstance().apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
            if (!inicioDelDia) {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
        }.timeInMillis
    }

    private fun horaLegible(hora: Int, minuto: Int): String {
        val h12 = when {
            hora == 0 -> 12
            hora > 12 -> hora - 12
            else -> hora
        }
        return "%d:%02d %s".format(h12, minuto, if (hora < 12) "AM" else "PM")
    }
}