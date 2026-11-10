package com.icm2630.proyecto.ui.viewmodel

import android.content.Context
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.icm2630.proyecto.data.remote.GoogleAuth
import com.icm2630.proyecto.data.repository.SesionRepository
import com.icm2630.proyecto.data.repository.UsuarioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val uid: String? = null,
    val mensaje: String? = null,
    val restaurando: Boolean = false
) {
    val autenticado: Boolean get() = uid != null
}

/** Sesión 11.1: Firebase Auth, operaciones y estado pertenecen al ViewModel. */
class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(AuthUiState(restaurando = true))
    val uiState = _uiState.asStateFlow()
    private var operacion: Job? = null
    private var operacionPropia = false

    private val authListener = FirebaseAuth.AuthStateListener { firebase ->
        if (!operacionPropia) {
            val usuario = firebase.currentUser
            if (usuario == null) {
                operacion?.cancel()
                SesionRepository.limpiar()
                _uiState.value = AuthUiState(cargando = false)
            } else if (_uiState.value.uid != usuario.uid) {
                ejecutar { cargarSesion(usuario) }
            }
        }
    }

    init { auth.addAuthStateListener(authListener) }

    fun limpiarError() = _uiState.update { it.copy(error = null, mensaje = null) }
    fun mostrarError(mensaje: String) = _uiState.update { it.copy(error = mensaje.ifBlank { null }) }

    fun iniciarSesion(correo: String, password: String) {
        if (correo.isBlank() || password.isBlank()) {
            mostrarError("Por favor, completa todos los campos")
            return
        }
        ejecutar {
            val usuario = checkNotNull(auth.signInWithEmailAndPassword(correo.trim(), password).await().user)
            cargarSesion(usuario)
        }
    }

    fun registrar(nombre: String, correo: String, password: String, confirmar: String) {
        val error = when {
            nombre.isBlank() || correo.isBlank() || password.isBlank() -> "Por favor, completa todos los campos"
            password.length < 8 -> "La contraseña debe tener mínimo 8 caracteres"
            password != confirmar -> "Las contraseñas no coinciden"
            else -> null
        }
        if (error != null) {
            mostrarError(error)
            return
        }
        ejecutar {
            val usuario = checkNotNull(auth.createUserWithEmailAndPassword(correo.trim(), password).await().user)
            usuario.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(nombre.trim()).build()).await()
            cargarSesion(usuario)
        }
    }

    fun reanudarSesion() = ejecutar {
        cargarSesion(checkNotNull(auth.currentUser) { "Primero inicia sesión con tu correo o con Google." })
    }

    fun iniciarSesionGoogle(context: Context) = ejecutar {
        val token = GoogleAuth.obtenerIdToken(context)
        val usuario = checkNotNull(auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await().user)
        cargarSesion(usuario)
    }

    fun recuperarPassword(correo: String) {
        if (correo.isBlank()) {
            mostrarError("Escribe tu correo para recuperar la contraseña.")
            return
        }
        ejecutar {
            auth.sendPasswordResetEmail(correo.trim()).await()
            _uiState.update { it.copy(mensaje = "Si existe una cuenta con ese correo, recibirás las instrucciones.") }
        }
    }

    fun cerrarSesion() {
        operacion?.cancel()
        operacionPropia = true
        auth.signOut()
        SesionRepository.limpiar()
        _uiState.value = AuthUiState(cargando = false)
        operacionPropia = false
    }

    private fun ejecutar(bloque: suspend () -> Unit) {
        if (operacion?.isActive == true) return
        _uiState.update { it.copy(cargando = true, error = null, mensaje = null) }
        operacion = viewModelScope.launch {
            operacionPropia = true
            try {
                bloque()
                _uiState.update { it.copy(cargando = false, restaurando = false) }
            } catch (e: GetCredentialCancellationException) {
                _uiState.update { it.copy(cargando = false, restaurando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, restaurando = false, error = mensajeDeError(e)) }
            } finally {
                operacionPropia = false
            }
        }
    }

    private suspend fun cargarSesion(usuario: FirebaseUser) {
        val uid = usuario.uid
        val perfil = UsuarioRepository.asegurarPerfil(uid, usuario.displayName.orEmpty(), usuario.email.orEmpty())
        check(auth.currentUser?.uid == uid) { "La sesión cambió. Vuelve a iniciar sesión." }
        SesionRepository.perfil = perfil
        SesionRepository.nombreCuenta = perfil.nombreCompleto.ifBlank { usuario.displayName.orEmpty() }
        SesionRepository.correoCuenta = usuario.email.orEmpty()
        SesionRepository.volverAMiPerfil()
        _uiState.value = AuthUiState(cargando = false, uid = uid)
    }

    private fun mensajeDeError(error: Exception): String = when {
        error is FirebaseNetworkException -> "Sin conexión. Revisa tu internet e inténtalo de nuevo."
        error is FirebaseAuthException -> when (error.errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con ese correo."
            "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido."
            "ERROR_WEAK_PASSWORD" -> "La contraseña no cumple los requisitos de seguridad."
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Espera un momento antes de reintentar."
            else -> "No se pudo autenticar la cuenta. Comprueba los datos e inténtalo de nuevo."
        }
        else -> "No se pudo completar la operación. Comprueba tu conexión e inténtalo de nuevo."
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
        super.onCleared()
    }
}
