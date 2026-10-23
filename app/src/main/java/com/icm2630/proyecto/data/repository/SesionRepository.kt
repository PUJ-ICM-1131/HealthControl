package com.icm2630.proyecto.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.PersonaVinculada

object SesionRepository {

    private val auth get() = FirebaseAuth.getInstance()

    /** UID de Firebase del usuario autenticado. Solo se usa con sesión iniciada. */
    val uidActual: String
        get() = checkNotNull(auth.currentUser) { "No hay sesión de Firebase" }.uid

    /** true si Firebase recuerda una sesión de un inicio anterior. */
    val haySesionGuardada: Boolean
        get() = auth.currentUser != null

    var perfil: PerfilUsuario? = null

    /** Datos de cuenta capturados en el registro / login; el onboarding no los vuelve a pedir. */
    var nombreCuenta: String = ""
    var correoCuenta: String = ""

    /**
     * Familiar cuyo HealthControl se está consultando (solo lectura).
     * null = se está viendo el propio perfil. Es estado de Compose para que
     * todas las pantallas se actualicen al cambiar de perfil.
     */
    var familiarActivo by mutableStateOf<PersonaVinculada?>(null)
        private set

    /** Cuenta cuyos datos muestran los repositorios: null = la propia. */
    val cuentaActiva: String?
        get() = familiarActivo?.codigo

    val perfilConfigurado: Boolean
        get() = perfil?.configurado == true

    fun verFamiliar(persona: PersonaVinculada) {
        FamiliarSimulator.asegurarDatos(persona.codigo)
        familiarActivo = persona
    }

    fun volverAMiPerfil() {
        familiarActivo = null
    }

    fun cerrarSesion() {
        auth.signOut()
        perfil = null
        nombreCuenta = ""
        correoCuenta = ""
        familiarActivo = null
    }

    // AUTENTICACIÓN

    fun iniciarSesion(
        correo: String,
        password: String,
        onListo: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(correo.trim(), password)
            .addOnSuccessListener { result -> cargarSesion(result.user!!, "", onListo, onError) }
            .addOnFailureListener { e -> onError(mensajeDeError(e)) }
    }

    fun registrar(
        nombre: String,
        correo: String,
        password: String,
        onListo: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(correo.trim(), password)
            .addOnSuccessListener { result -> cargarSesion(result.user!!, nombre.trim(), onListo, onError) }
            .addOnFailureListener { e -> onError(mensajeDeError(e)) }
    }

    /** [idToken] viene del selector de cuentas de Google (ver GoogleAuth). */
    fun iniciarSesionConGoogle(
        idToken: String,
        onListo: () -> Unit,
        onError: (String) -> Unit
    ) {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credencial)
            .addOnSuccessListener { result -> cargarSesion(result.user!!, "", onListo, onError) }
            .addOnFailureListener { e -> onError(mensajeDeError(e)) }
    }

    /** Entra con la sesión que Firebase ya tenía guardada (p. ej. tras validar la huella). */
    fun reanudarSesion(
        onListo: () -> Unit,
        onError: (String) -> Unit
    ) {
        val usuario = auth.currentUser
        if (usuario == null) {
            onError("Primero inicia sesión con tu correo o con Google.")
            return
        }
        cargarSesion(usuario, "", onListo, onError)
    }

    /**
     * Trae el perfil de Firestore. Si es la primera vez que entra, crea el
     * documento del usuario (con su código de vinculación) y lo deja sin
     * configurar para que pase por el onboarding.
     */
    private fun cargarSesion(
        usuario: FirebaseUser,
        nombreRegistro: String,
        onListo: () -> Unit,
        onError: (String) -> Unit
    ) {
        val nombre = nombreRegistro.ifBlank { usuario.displayName.orEmpty() }
        val correo = usuario.email.orEmpty()

        UsuarioRepository.obtenerPerfil(
            uid = usuario.uid,
            onResultado = { existente ->
                if (existente != null) {
                    perfil = existente
                    nombreCuenta = existente.nombreCompleto.ifBlank { nombre }
                    correoCuenta = correo
                    onListo()
                } else {
                    UsuarioRepository.crearPerfil(
                        uid = usuario.uid,
                        nombre = nombre,
                        correo = correo,
                        onListo = { codigo ->
                            perfil = PerfilUsuario(nombreCompleto = nombre, codigoVinculacion = codigo)
                            nombreCuenta = nombre
                            correoCuenta = correo
                            onListo()
                        },
                        onError = { e -> onError(mensajeDeError(e)) }
                    )
                }
            },
            onError = { e -> onError(mensajeDeError(e)) }
        )
    }

    private fun mensajeDeError(e: Exception): String = when {
        e is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres."
        e is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo."
        e is FirebaseAuthInvalidCredentialsException && e.errorCode == "ERROR_INVALID_EMAIL" ->
            "El correo no tiene un formato válido."
        e is FirebaseAuthInvalidCredentialsException || e is FirebaseAuthInvalidUserException ->
            "Correo o contraseña incorrectos."
        e is FirebaseNetworkException -> "Sin conexión. Revisa tu internet e inténtalo de nuevo."
        else -> e.localizedMessage ?: "Ocurrió un error inesperado."
    }
}
