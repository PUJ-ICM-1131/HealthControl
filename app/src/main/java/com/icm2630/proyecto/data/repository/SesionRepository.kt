package com.icm2630.proyecto.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.PersonaVinculada

/** Datos de la cuenta en uso. Las operaciones de Firebase Auth pertenecen a AuthViewModel. */
object SesionRepository {
    val uidActual: String
        get() = checkNotNull(FirebaseAuth.getInstance().currentUser) { "No hay sesión de Firebase" }.uid
    val haySesionGuardada: Boolean get() = FirebaseAuth.getInstance().currentUser != null

    var perfil by mutableStateOf<PerfilUsuario?>(null)
    var perfilesVinculados by mutableStateOf<Map<String, PerfilUsuario>>(emptyMap())
    val perfilFamiliar: PerfilUsuario? get() = familiarActivo?.uid?.let { perfilesVinculados[it] }
    var nombreCuenta: String = ""
    var correoCuenta: String = ""
    var familiarActivo by mutableStateOf<PersonaVinculada?>(null)
        private set

    val perfilConfigurado: Boolean get() = perfil?.configurado == true
    val uidConsultado: String get() = familiarActivo?.uid ?: uidActual

    fun puedeGestionar(uid: String): Boolean = haySesionGuardada &&
        (uid == uidActual || VinculacionRepository.obtenerPorId(uid)?.puedeGestionar == true)

    fun nombreDe(uid: String): String = if (haySesionGuardada && uid == uidActual)
        perfil?.nombreCompleto.orEmpty() else VinculacionRepository.obtenerPorId(uid)?.nombre.orEmpty()

    fun verFamiliar(persona: PersonaVinculada) {
        VinculacionRepository.obtenerPorId(persona.id)?.let { familiarActivo = it }
    }

    fun volverAMiPerfil() { familiarActivo = null }

    fun limpiar() {
        perfilesVinculados = emptyMap()
        familiarActivo = null
        perfil = null
        nombreCuenta = ""
        correoCuenta = ""
    }
}
