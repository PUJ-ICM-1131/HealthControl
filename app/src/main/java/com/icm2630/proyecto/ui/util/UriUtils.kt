package com.icm2630.proyecto.ui.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns


fun obtenerNombreArchivo(
    context: Context,
    uri: Uri
): String {

    var nombreArchivo: String? = null

    context.contentResolver
        .query(
            uri,
            null,
            null,
            null,
            null
        )
        ?.use { cursor ->

            val indiceNombre =
                cursor.getColumnIndex(
                    OpenableColumns.DISPLAY_NAME
                )

            if (
                indiceNombre >= 0 &&
                cursor.moveToFirst()
            ) {

                nombreArchivo =
                    cursor.getString(
                        indiceNombre
                    )
            }
        }


    return nombreArchivo
        ?: uri.lastPathSegment
        ?: "Orden médica"
}