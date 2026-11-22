package com.icm2630.proyecto.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.icm2630.proyecto.MainActivity


object Notificaciones {

    const val CANAL_RECORDATORIOS = "recordatorios"
    const val CANAL_VINCULOS = "vinculos"

    /** Contexto de la aplicación; se fija en MainActivity.onCreate. */
    var contexto: Context? = null
        private set

    fun inicializar(context: Context) {
        contexto = context.applicationContext
        crearCanales(context.applicationContext)
    }

    fun crearCanales(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CANAL_RECORDATORIOS,
                "Recordatorios de dosis y citas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Avisa a la hora de cada dosis y antes de cada cita médica." }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CANAL_VINCULOS,
                "Personas vinculadas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Solicitudes de vínculo y nuevos registros de las personas vinculadas." }
        )
    }


    fun permitidas(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun mostrar(
        context: Context,
        id: Int,
        canal: String,
        titulo: String,
        texto: String
    ) {
        if (!permitidas(context)) return
        crearCanales(context)
        val abrir = PendingIntent.getActivity(
            context,
            id,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val esRecordatorio = canal == CANAL_RECORDATORIOS
        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(
                if (esRecordatorio) android.R.drawable.ic_popup_reminder
                else android.R.drawable.ic_dialog_info
            )
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(
                if (esRecordatorio) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setCategory(
                if (esRecordatorio) NotificationCompat.CATEGORY_REMINDER
                else NotificationCompat.CATEGORY_SOCIAL
            )
            .setContentIntent(abrir)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notificacion)
        } catch (_: SecurityException) {

        }
    }
}

