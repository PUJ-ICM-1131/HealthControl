package com.icm2630.proyecto.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.icm2630.proyecto.data.model.*
import com.icm2630.proyecto.data.remote.FirebaseDb
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * usuarios/{uid}: cuenta privada.
 * perfilesCompartidos/{uid}: salud consultable exclusivamente con un vínculo aceptado.
 * codigos/{codigo}: directorio mínimo para enviar solicitudes, sin datos de salud.
 */
object UsuarioRepository {
    private val db get() = FirebaseDb.firestore

    suspend fun asegurarPerfil(uid: String, nombre: String, correo: String): PerfilUsuario {
        repeat(5) {
            val candidato = CodigoInvitacion.generar()
            val resultado = db.runTransaction { tx ->
                val usuarioRef = db.collection("usuarios").document(uid)
                val doc = tx.get(usuarioRef)
                val existente = if (doc.exists()) doc.aPerfil() else null
                val codigo = existente?.codigoVinculacion?.takeIf { it.isNotBlank() } ?: candidato
                val codigoRef = db.collection("codigos").document(codigo)
                val indice = tx.get(codigoRef)
                if (indice.exists() && indice.getString("uid") != uid) {
                    if (existente?.codigoVinculacion != null) error("El código de esta cuenta necesita revisarse.")
                    return@runTransaction null
                }
                val perfil = (existente ?: PerfilUsuario(nombreCompleto = nombre))
                    .copy(codigoVinculacion = codigo)
                if (!doc.exists()) {
                    tx.set(usuarioRef, mapOf(
                        "nombreCompleto" to nombre,
                        "correo" to correo,
                        "codigoVinculacion" to codigo,
                        "configurado" to false,
                        "creadoEn" to FieldValue.serverTimestamp()
                    ))
                } else if (existente?.codigoVinculacion.isNullOrBlank()) {
                    tx.set(usuarioRef, mapOf("codigoVinculacion" to codigo), SetOptions.merge())
                }
                tx.set(codigoRef, mapOf("uid" to uid, "nombre" to perfil.nombreCompleto))
                if (perfil.configurado) {
                    tx.set(db.collection("perfilesCompartidos").document(uid), perfil.aCompartido())
                }
                perfil
            }.await()
            if (resultado != null) return resultado
        }
        error("No se pudo reservar un código único. Inténtalo de nuevo.")
    }

    suspend fun guardarPerfil(uid: String, perfil: PerfilUsuario) {
        val codigo = checkNotNull(perfil.codigoVinculacion) { "La cuenta aún no tiene código de invitación." }
        db.runBatch { batch ->
            batch.set(db.collection("usuarios").document(uid), perfil.aMapa(), SetOptions.merge())
            batch.set(db.collection("codigos").document(codigo),
                mapOf("uid" to uid, "nombre" to perfil.nombreCompleto))
            batch.set(db.collection("perfilesCompartidos").document(uid), perfil.aCompartido())
            if (!perfil.compartirUbicacion) batch.delete(db.collection("ubicaciones").document(uid))
        }.await()
    }

    fun observarPerfil(uid: String): Flow<PerfilUsuario?> = observar("usuarios", uid)
    fun observarPerfilCompartido(uid: String): Flow<PerfilUsuario?> = observar("perfilesCompartidos", uid)

    private fun observar(coleccion: String, uid: String): Flow<PerfilUsuario?> = callbackFlow {
        val listener = db.collection(coleccion).document(uid).addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { doc, error ->
            if (error != null) close(error)
            else if (doc?.metadata?.hasPendingWrites() != true) trySend(doc?.takeIf { it.exists() }?.aPerfil())
        }
        awaitClose { listener.remove() }
    }

    private fun PerfilUsuario.aCompartido(): Map<String, Any?> = mapOf(
        "nombreCompleto" to nombreCompleto,
        "fechaNacimientoMillis" to fechaNacimientoMillis,
        "genero" to genero?.name,
        "tipoSangre" to tipoSangre?.name,
        "alergias" to alergias,
        "condiciones" to condiciones,
        "condicionRelevante" to condicionRelevante,
        "codigoVinculacion" to codigoVinculacion,
        "configurado" to configurado,
        "actualizadoEn" to FieldValue.serverTimestamp()
    )

    private fun PerfilUsuario.aMapa(): Map<String, Any?> = aCompartido() + mapOf(
        "contacto" to contacto,
        "contactoEmergencia" to contactoEmergencia,
        "telefonoEmergencia" to telefonoEmergencia,
        "permisoUbicacion" to permisoUbicacion,
        "notificacionesActivas" to notificacionesActivas,
        "compartirUbicacion" to compartirUbicacion,
        "fotoUri" to fotoUri
    )

    private fun DocumentSnapshot.aPerfil() = PerfilUsuario(
        nombreCompleto = getString("nombreCompleto").orEmpty(),
        fechaNacimientoMillis = getLong("fechaNacimientoMillis"),
        genero = getString("genero")?.let { n -> Genero.entries.find { it.name == n } },
        contacto = getString("contacto").orEmpty(),
        contactoEmergencia = getString("contactoEmergencia").orEmpty(),
        telefonoEmergencia = getString("telefonoEmergencia").orEmpty(),
        tipoSangre = getString("tipoSangre")?.let { n -> TipoSangre.entries.find { it.name == n } },
        alergias = getString("alergias").orEmpty(),
        condiciones = (get("condiciones") as? List<*>)?.filterIsInstance<String>().orEmpty(),
        condicionRelevante = getString("condicionRelevante").orEmpty(),
        permisoUbicacion = getBoolean("permisoUbicacion") ?: false,
        notificacionesActivas = getBoolean("notificacionesActivas") ?: false,
        compartirUbicacion = getBoolean("compartirUbicacion") ?: false,
        fotoUri = getString("fotoUri"),
        codigoVinculacion = getString("codigoVinculacion"),
        configurado = getBoolean("configurado") ?: false
    )
}
