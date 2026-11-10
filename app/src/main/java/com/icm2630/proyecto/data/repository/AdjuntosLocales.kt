package com.icm2630.proyecto.data.repository

import android.content.Context
import android.content.SharedPreferences

/** Los archivos locales no son URLs compartibles ni se publican en los documentos de salud. */
object AdjuntosLocales {
    private var preferencias: SharedPreferences? = null
    fun inicializar(context: Context) {
        preferencias = context.applicationContext.getSharedPreferences("adjuntos_locales", Context.MODE_PRIVATE)
    }
    fun guardar(uid: String, tipo: String, id: String, uri: String?) {
        preferencias?.edit()?.putString("$uid:$tipo:$id", uri)?.apply()
    }
    fun obtener(uid: String, tipo: String, id: String): String? =
        preferencias?.getString("$uid:$tipo:$id", null)
    fun eliminar(uid: String, tipo: String, id: String) {
        preferencias?.edit()?.remove("$uid:$tipo:$id")?.apply()
    }
}
