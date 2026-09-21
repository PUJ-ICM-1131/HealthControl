package com.icm2630.proyecto.navigation

import androidx.navigation3.runtime.NavKey

// Define todos los posibles destinos de nuestra aplicación.
sealed interface Routes : NavKey {
    data object Login : Routes
    data object Register : Routes
    data object ProfileSetup : Routes
    data object Monitoreo : Routes   // HU-25, home del acompañante

    // Destinos principales con BottomBar
    data object Home : Routes
    data object Recordatorios : Routes
    data object Registrar : Routes
    data object Historial : Routes
    data object Mapa : Routes
    data object Perfil : Routes

    /**
     * [citaId] / [medicamentoId] nulos = se está registrando uno nuevo.
     * Con valor = se está editando el existente con ese id (HU-06):
     * la misma pantalla de registro precarga los datos y guarda sobre
     * el mismo registro en vez de crear uno nuevo.
     */
    data class RegistrarMedicamento(val medicamentoId: String? = null) : Routes
    data class RegistrarCita(val citaId: String? = null) : Routes

    // Detalle de un pendiente (HU-21): ver, editar o eliminar.
    data class DetalleCita(val citaId: String) : Routes
    data class DetalleMedicamento(val medicamentoId: String) : Routes
}