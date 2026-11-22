package com.icm2630.proyecto.ui.util

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
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

/**
 * Extrae el nombre y teléfono de un contacto seleccionado de la agenda del dispositivo.
 */
fun extraerContacto(context: Context, uri: Uri): Pair<String, String>? {
    var nombre = ""
    var telefono = ""
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    nombre = cursor.getString(nameIndex) ?: ""
                }
                val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                val hasPhone = if (hasPhoneIndex >= 0) cursor.getInt(hasPhoneIndex) else 0
                if (hasPhone > 0 && idIndex >= 0) {
                    val contactId = cursor.getString(idIndex)
                    context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )?.use { phoneCursor ->
                        if (phoneCursor.moveToFirst()) {
                            val phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (phoneIndex >= 0) {
                                telefono = phoneCursor.getString(phoneIndex) ?: ""
                            }
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return if (nombre.isNotEmpty() || telefono.isNotEmpty()) Pair(nombre, telefono) else null
}
