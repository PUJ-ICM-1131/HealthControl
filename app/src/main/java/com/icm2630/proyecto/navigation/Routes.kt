package com.icm2630.proyecto.navigation

import androidx.navigation3.runtime.NavKey

// Define todos los posibles destinos de nuestra aplicación.
sealed interface Routes : NavKey {
    data object Login : Routes
    data object Register : Routes
    
    // Destinos principales con BottomBar
    data object Home : Routes
    data object Recordatorios : Routes
    data object Registrar : Routes
    data object Historial : Routes
    data object Perfil : Routes
}