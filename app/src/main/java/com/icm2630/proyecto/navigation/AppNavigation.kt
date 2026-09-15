package com.icm2630.proyecto.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.icm2630.proyecto.components.HealthBottomNavigation
import com.icm2630.proyecto.screens.HomeScreen
import com.icm2630.proyecto.screens.LoginScreen
import com.icm2630.proyecto.screens.RegisterScreen

@Composable
fun AppNavigation() {

    // Lista que guarda las pantallas por las que vamos navegando.
    val backStack = remember {
        mutableStateListOf<Routes>(Routes.Login)
    }

    // Se encarga de mostrar la pantalla correspondiente
    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        entryProvider = entryProvider {
            entry<Routes.Login> {
                LoginScreen(
                    onLogin = {
                        backStack.clear()
                        backStack.add(Routes.Home)
                    },
                    onIrARegister = {
                        backStack.add(Routes.Register)
                    }
                )
            }

            entry<Routes.Register> {
                RegisterScreen(
                    onIrALogin = {
                        backStack.removeAt(backStack.lastIndex)
                    }
                )
            }

            entry<Routes.Home> {
                HomeScreen(
                    onNavigate = { route ->
                        if (route != Routes.Home) {
                            backStack.add(route)
                        }
                    }
                )
            }

            entry<Routes.Recordatorios> { PlaceholderScreen("Recordatorios", backStack) }
            entry<Routes.Registrar> { PlaceholderScreen("Registrar", backStack) }
            entry<Routes.Historial> { PlaceholderScreen("Historial", backStack) }
            entry<Routes.Perfil> { PlaceholderScreen("Perfil", backStack) }
        }
    )
}

@Composable
fun PlaceholderScreen(title: String, backStack: MutableList<Routes>) {
    Scaffold(
        bottomBar = {
            val currentRoute = when (title) {
                "Recordatorios" -> Routes.Recordatorios
                "Registrar" -> Routes.Registrar
                "Historial" -> Routes.Historial
                "Perfil" -> Routes.Perfil
                else -> Routes.Home
            }
            HealthBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route == Routes.Home) {
                        backStack.clear()
                        backStack.add(Routes.Home)
                    } else if (route != currentRoute) {
                        backStack.add(route)
                    }
                }
            )
        }
    ) { p ->
        Box(Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.headlineLarge)
        }
    }
}
