package com.icm2630.proyecto.data.repository

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.icm2630.proyecto.data.model.ContactoDispositivo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ContactosRepository {
    /** Lee únicamente la fila de teléfono autorizada por el selector del sistema. */
    suspend fun obtenerTelefono(context: Context, uri: Uri): ContactoDispositivo = withContext(Dispatchers.IO) {
        // El proveedor puede variar entre versiones de Android; se consulta solo la URI elegida.
        require(uri.scheme == "content" && !uri.authority.isNullOrBlank()) {
            "El contacto seleccionado no es válido."
        }
        val columnas = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        context.contentResolver.query(uri, columnas, null, null, null)?.use { cursor ->
            check(cursor.moveToFirst()) { "Ese contacto ya no está disponible. Selecciónalo de nuevo." }
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(columnas[0])).orEmpty().trim()
            val numero = cursor.getString(cursor.getColumnIndexOrThrow(columnas[1])).orEmpty()
            val telefono = PhoneNumberUtils.normalizeNumber(numero)
            require(telefono.any { it.isDigit() } && telefono.length <= 30) {
                "El contacto no tiene un número válido. Puedes escribirlo manualmente."
            }
            require(nombre.length <= 200) { "El nombre es demasiado largo. Puedes escribirlo manualmente." }
            ContactoDispositivo(nombre.ifBlank { "Contacto de emergencia" }, telefono)
        } ?: error("No se pudo leer el contacto seleccionado.")
    }
}
