package com.icm2630.proyecto.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

/** Identidad de la sesión y fechas de servidor. En registros antiguos se conserva el autor desconocido. */
internal object AuditoriaRegistros {
    fun paraGuardar(anterior: DocumentSnapshot): Map<String, Any?> {
        val uid = SesionRepository.uidActual
        val nombre = checkNotNull(SesionRepository.perfil).nombreCompleto
        val previa = anterior.get("auditoria") as? Map<*, *>
        return mapOf(
            "creadoPorUid" to if (anterior.exists()) previa?.get("creadoPorUid") else uid,
            "creadoPorNombre" to if (anterior.exists()) previa?.get("creadoPorNombre") else nombre,
            "creadoEn" to if (anterior.exists()) previa?.get("creadoEn") else FieldValue.serverTimestamp(),
            "actualizadoPorUid" to uid,
            "actualizadoPorNombre" to nombre,
            "actualizadoEn" to FieldValue.serverTimestamp()
        )
    }
}
