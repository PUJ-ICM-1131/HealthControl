package com.icm2630.proyecto.navigation

import androidx.navigation3.runtime.NavKey

// Define todos los posibles destinos de nuestra aplicación.
// Al ser sealed, solo podemos crear los tipos de pantalla
// definidos dentro de esta interfaz.
sealed interface Routes : NavKey {
    // Representa la pantalla principal.
    data object Main : Routes

}