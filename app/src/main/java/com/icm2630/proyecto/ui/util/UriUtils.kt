package com.icm2630.proyecto.ui.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun obtenerNombreArchivo(
    context: Context,
    uri: Uri
): String {
    var nombreArchivo: String? = null
    context.contentResolver
        .query(uri, null, null, null, null)
        ?.use { cursor ->
            val indiceNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (indiceNombre >= 0 && cursor.moveToFirst()) {
                nombreArchivo = cursor.getString(indiceNombre)
            }
        }

    return nombreArchivo
        ?: uri.lastPathSegment
        ?: "Soporte"
}

/**
 * Copia un archivo desde una Uri (ej. galería o selector) al almacenamiento interno persistente.
 * Esto asegura que los soportes se conserven y puedan consultarse siempre.
 */
fun copiarUriAAlmacenamientoInterno(
    context: Context,
    uri: Uri,
    prefijo: String
): Uri? {
    return try {
        val extension = when (context.contentResolver.getType(uri)) {
            "application/pdf" -> ".pdf"
            "image/png" -> ".png"
            "image/jpeg" -> ".jpg"
            else -> ".jpg"
        }
        val archivoSalida = File(context.filesDir, "${prefijo}_${System.currentTimeMillis()}$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(archivoSalida).use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(archivoSalida)
    } catch (e: Exception) {
        uri
    }
}

/**
 * Convierte una URI de archivo local a una URI de FileProvider segura
 * para que otras aplicaciones puedan abrir los soportes sin errores de permisos.
 */
fun obtenerUriCompartible(
    context: Context,
    uriString: String
): Uri {
    val uri = Uri.parse(uriString)
    return if (uri.scheme == "file" || (uri.scheme == null && uriString.startsWith("/"))) {
        val file = File(uri.path ?: uriString)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    } else {
        uri
    }
}
