package com.icm2630.proyecto.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Recibe la alarma programada, muestra el recordatorio y agenda la siguiente toma. */
class RecordatorioReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != RecordatoriosProgramados.ACCION) return
        val clave = intent.getStringExtra(RecordatoriosProgramados.EXTRA_CLAVE) ?: return
        Notificaciones.mostrar(
            context = context,
            id = clave.hashCode(),
            canal = Notificaciones.CANAL_RECORDATORIOS,
            titulo = intent.getStringExtra(RecordatoriosProgramados.EXTRA_TITULO).orEmpty(),
            texto = intent.getStringExtra(RecordatoriosProgramados.EXTRA_TEXTO).orEmpty()
        )
        RecordatoriosProgramados.despuesDeDisparar(context, intent)
    }
}