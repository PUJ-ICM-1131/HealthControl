package com.icm2630.proyecto.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
//importación de las demás pantallas
import com.icm2630.proyecto.screens.RegisterScreen
import com.icm2630.proyecto.screens.HomeScreen
import com.icm2630.proyecto.screens.LoginScreen

@Composable
fun AppNavigation() {

    // Lista que guarda las pantallas por las que vamos navegando.
    // Inicialmente estamos en Main.
    val backStack = remember {
        mutableStateListOf<Routes>(Routes.Main)
    }

    // Se encarga de mostrar la pantalla correspondiente
    // según lo que haya en el backStack.
    NavDisplay(
        backStack = backStack,

        // Elimina la pantalla actual para volver a la anterior.
        onBack = {
            backStack.removeAt(backStack.lastIndex)
        },

        // Define qué pantalla mostrar para cada tipo de Screen.
        entryProvider = entryProvider {

            // Configuración de la pantalla principal.
            entry<Routes.Main> {
                RegisterScreen()
            }
        }
    )
}