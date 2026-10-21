package com.icm2630.proyecto.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.icm2630.proyecto.data.model.Genero
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.TipoSangre
import com.icm2630.proyecto.data.remote.FirebaseDb


object UsuarioRepository {

    private const val USUARIOS = "usuarios"
    private const val CODIGOS = "codigos"
    private const val MAX_INTENTOS = 5

    private val db get() = FirebaseDb.firestore


    fun crearPerfil(
        uid: String,
        nombre: String,
        correo: String,
        onListo: (codigo: String) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        intentarCrear(uid, nombre, correo, 1, onListo, onError)
    }

    private fun intentarCrear(
        uid: String,
        nombre: String,
        correo: String,
        intento: Int,
        onListo: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val codigo = VinculacionSimulator.generarCodigo()
        val refUsuario = db.collection(USUARIOS).document(uid)
        val refCodigo = db.collection(CODIGOS).document(codigo)

        db.runTransaction { tx ->
            val codigoExistente = tx.get(refUsuario).getString("codigoVinculacion")
            when {
                codigoExistente != null -> codigoExistente      // ya estaba creado
                tx.get(refCodigo).exists() -> null              // código ocupado: reintentar
                else -> {
                    tx.set(refCodigo, mapOf("uid" to uid))
                    tx.set(
                        refUsuario,
                        mapOf(
                            "nombreCompleto" to nombre,
                            "correo" to correo,
                            "codigoVinculacion" to codigo,
                            "configurado" to false,
                            "creadoEn" to FieldValue.serverTimestamp()
                        )
                    )
                    codigo
                }
            }
        }
            .addOnSuccessListener { resultado: String? ->
                when {
                    resultado != null -> onListo(resultado)
                    intento < MAX_INTENTOS ->
                        intentarCrear(uid, nombre, correo, intento + 1, onListo, onError)
                    else -> onError(IllegalStateException("No se pudo generar un código de vinculación único"))
                }
            }
            .addOnFailureListener { e -> onError(e) }
    }


    fun obtenerPerfil(
        uid: String,
        onResultado: (PerfilUsuario?) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        db.collection(USUARIOS).document(uid).get()
            .addOnSuccessListener { doc -> onResultado(if (doc.exists()) doc.aPerfil() else null) }
            .addOnFailureListener { e -> onError(e) }
    }


    fun guardarPerfil(
        uid: String,
        perfil: PerfilUsuario,
        onListo: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        db.collection(USUARIOS).document(uid)
            .set(perfil.aMapa(), SetOptions.merge())
            .addOnSuccessListener { onListo() }
            .addOnFailureListener { e -> onError(e) }
    }

    private fun PerfilUsuario.aMapa(): Map<String, Any?> = mapOf(
        "nombreCompleto" to nombreCompleto,
        "fechaNacimientoMillis" to fechaNacimientoMillis,
        "genero" to genero?.name,
        "contacto" to contacto,
        "contactoEmergencia" to contactoEmergencia,
        "telefonoEmergencia" to telefonoEmergencia,
        "tipoSangre" to tipoSangre?.name,
        "alergias" to alergias,
        "condiciones" to condiciones,
        "condicionRelevante" to condicionRelevante,
        "permisoUbicacion" to permisoUbicacion,
        "notificacionesActivas" to notificacionesActivas,
        "configurado" to true,
        "actualizadoEn" to FieldValue.serverTimestamp()
    )

    private fun DocumentSnapshot.aPerfil() = PerfilUsuario(
        nombreCompleto = getString("nombreCompleto") ?: "",
        fechaNacimientoMillis = getLong("fechaNacimientoMillis"),
        genero = getString("genero")?.let { n -> Genero.entries.find { it.name == n } },
        contacto = getString("contacto") ?: "",
        contactoEmergencia = getString("contactoEmergencia") ?: "",
        telefonoEmergencia = getString("telefonoEmergencia") ?: "",
        tipoSangre = getString("tipoSangre")?.let { n -> TipoSangre.entries.find { it.name == n } },
        alergias = getString("alergias") ?: "",
        condiciones = (get("condiciones") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        condicionRelevante = getString("condicionRelevante") ?: "",
        permisoUbicacion = getBoolean("permisoUbicacion") ?: false,
        notificacionesActivas = getBoolean("notificacionesActivas") ?: true,
        codigoVinculacion = getString("codigoVinculacion"),
        configurado = getBoolean("configurado") ?: false
    )
}