package com.icm2630.proyecto.notificaciones

import android.content.Context
import com.icm2630.proyecto.data.model.Cita
import com.icm2630.proyecto.data.model.EstadoInvitacion
import com.icm2630.proyecto.data.model.Invitacion
import com.icm2630.proyecto.data.model.Medicamento
import com.icm2630.proyecto.data.repository.SesionRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Notificaciones entre usuarios vinculados. Se generan a partir de los listeners
 * en tiempo real de Firestore que ya mantiene PerfilViewModel:
 *  - Te llegó una solicitud de vínculo.
 *  - Aceptaron la solicitud que enviaste.
 *  - Una persona vinculada registró una cita o un medicamento nuevo.
 *
 * Cada aviso se muestra una sola vez (se recuerda en SharedPreferences). La primera vez
 * que se observa una cuenta solo se toma la línea base, para no avisar de lo que ya existía.
 */
object AvisosVinculacion {

    private const val PREFS = "avisos_vinculacion"

    fun procesarSolicitudes(miUid: String, solicitudes: List<Invitacion>) {
        val context = Notificaciones.contexto ?: return
        val prefs = prefs(context)
        val clave = "solicitudes_$miUid"
        val primeraVez = !prefs.contains(clave)
        val vistas = prefs.getStringSet(clave, emptySet()).orEmpty().toMutableSet()

        solicitudes.forEach { solicitud ->
            val marca = "${solicitud.id}:${solicitud.estado}"
            if (!vistas.add(marca)) return@forEach
            val recibida = solicitud.destinatarioUid == miUid && solicitud.estado == EstadoInvitacion.PENDIENTE
            val aceptada = solicitud.emisorUid == miUid && solicitud.estado == EstadoInvitacion.ACEPTADA
            when {
                // Una solicitud pendiente sigue siendo útil aunque sea la primera carga.
                recibida -> Notificaciones.mostrar(
                    context, marca.hashCode(), Notificaciones.CANAL_VINCULOS,
                    "Nueva solicitud de vínculo",
                    "${solicitud.nombreEmisor.ifBlank { "Una persona" }} quiere vincularse contigo. " +
                            "Ábrela en tu perfil para aceptarla o rechazarla."
                )
                aceptada && !primeraVez -> Notificaciones.mostrar(
                    context, marca.hashCode(), Notificaciones.CANAL_VINCULOS,
                    "Solicitud aceptada",
                    "${solicitud.nombreDestinatario.ifBlank { "La otra persona" }} aceptó tu solicitud. " +
                            "Ya pueden consultar sus datos de salud compartidos."
                )
            }
        }
        prefs.edit().putStringSet(clave, vistas).apply()
    }

    fun procesarCitas(miUid: String, cuenta: String, citas: List<Cita>) = procesarRegistros(
        miUid, cuenta, "citas",
        citas.associate { cita ->
            cita.id to "${cita.tipo.titulo} el ${fecha(cita.fechaMillis)} a las ${hora(cita.hora, cita.minuto)}"
        },
        titulo = { nombre -> "$nombre registró una cita" }
    )

    fun procesarMedicamentos(miUid: String, cuenta: String, medicamentos: List<Medicamento>) = procesarRegistros(
        miUid, cuenta, "medicamentos",
        medicamentos.associate { medicamento ->
            medicamento.id to buildString {
                append(medicamento.nombre.ifBlank { "Medicamento" })
                if (medicamento.horarios.isNotEmpty()) append(" · ").append(medicamento.horarios.joinToString(", "))
            }
        },
        titulo = { nombre -> "$nombre registró un medicamento" }
    )

    private fun procesarRegistros(
        miUid: String,
        cuenta: String,
        tipo: String,
        registros: Map<String, String>,
        titulo: (String) -> String
    ) {
        val context = Notificaciones.contexto ?: return
        val prefs = prefs(context)
        val clave = "${tipo}_${miUid}_$cuenta"
        val primeraVez = !prefs.contains(clave)
        val vistos = prefs.getStringSet(clave, emptySet()).orEmpty()
        if (!primeraVez) {
            val nombre = nombreDe(cuenta)
            (registros.keys - vistos).forEach { id ->
                Notificaciones.mostrar(
                    context, "$clave:$id".hashCode(), Notificaciones.CANAL_VINCULOS,
                    titulo(nombre), registros.getValue(id)
                )
            }
        }
        prefs.edit().putStringSet(clave, registros.keys.toSet()).apply()
    }

    private fun nombreDe(uid: String): String =
        SesionRepository.perfilesVinculados[uid]?.nombreCompleto?.takeIf { it.isNotBlank() }
            ?: SesionRepository.perfil?.acompanantes?.find { it.uid == uid }?.nombre?.takeIf { it.isNotBlank() }
            ?: "Una persona vinculada"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** La fecha se guarda como medianoche UTC (DatePicker de Material 3). */
    private fun fecha(utcMillis: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(utcMillis))

    private fun hora(hora: Int, minuto: Int): String {
        val h12 = when {
            hora == 0 -> 12
            hora > 12 -> hora - 12
            else -> hora
        }
        return "%d:%02d %s".format(h12, minuto, if (hora < 12) "AM" else "PM")
    }
}