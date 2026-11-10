package com.icm2630.proyecto.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Define todos los posibles destinos de nuestra aplicación.
@Serializable
sealed interface Routes : NavKey {
    @Serializable
    data object Login : Routes
    @Serializable
    data object Register : Routes
    @Serializable
    data object ProfileSetup : Routes

    // Destinos principales con BottomBar
    @Serializable
    data object Home : Routes
    @Serializable
    data object Recordatorios : Routes
    @Serializable
    data object Registrar : Routes
    @Serializable
    data object Historial : Routes
    @Serializable
    data object Mapa : Routes
    @Serializable
    data object Perfil : Routes

    /**
     * [citaId] / [medicamentoId] nulos = se está registrando uno nuevo.
     * Con valor = se está editando el existente con ese id:
     * la misma pantalla de registro precarga los datos y guarda sobre
     * el mismo registro en vez de crear uno nuevo.
     */
    @Serializable
    data class RegistrarMedicamento(val medicamentoId: String? = null) : Routes
    @Serializable
    data class RegistrarCita(val citaId: String? = null) : Routes

    // Detalle de un pendiente (HU-21): ver, editar o eliminar.
    @Serializable
    data class DetalleCita(val citaId: String) : Routes
    @Serializable
    data class DetalleMedicamento(val medicamentoId: String) : Routes
}