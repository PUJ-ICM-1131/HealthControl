package com.icm2630.proyecto.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Los archivos locales no son URLs compartibles ni se publican en los documentos de salud. */
object AdjuntosLocales {
    private var preferencias: SharedPreferences? = null
    private var contexto: Context? = null
    fun inicializar(context: Context) {
        contexto = context.applicationContext
        preferencias = context.applicationContext.getSharedPreferences("adjuntos_locales", Context.MODE_PRIVATE)
    }
    /** Copia el archivo a almacenamiento privado y entrega una URI content:// compartible. */
    suspend fun conservar(uri: String?, nombre: String?): String? = withContext(Dispatchers.IO) {
        if (uri == null) return@withContext null
        val app = checkNotNull(contexto) { "Almacenamiento no inicializado." }
        val origen = Uri.parse(uri)
        if (origen.scheme == "content" && origen.authority == "${app.packageName}.fileprovider" &&
            origen.pathSegments.firstOrNull() == "soportes") return@withContext uri
        val carpeta = File(app.filesDir, "soportes")
        check(carpeta.isDirectory || carpeta.mkdirs()) { "No se pudo crear la carpeta de soportes." }
        val mime = if (origen.scheme == "file") null else app.contentResolver.getType(origen)
        val extension = when {
            mime == "application/pdf" || nombre?.endsWith(".pdf", true) == true -> ".pdf"
            mime == "image/png" || nombre?.endsWith(".png", true) == true -> ".png"
            mime == "image/webp" || nombre?.endsWith(".webp", true) == true -> ".webp"
            else -> ".jpg"
        }
        val archivo = File.createTempFile("soporte_", extension, carpeta)
        try {
            val entrada = if (origen.scheme == "file") File(checkNotNull(origen.path)).inputStream()
                else checkNotNull(app.contentResolver.openInputStream(origen)) { "No se pudo leer el archivo." }
            entrada.use { input -> archivo.outputStream().use { output -> input.copyTo(output) } }
            check(archivo.length() > 0) { "El archivo está vacío." }
            FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", archivo).toString()
        } catch (e: Exception) {
            archivo.delete()
            throw e
        }
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
