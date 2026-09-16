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
import com.icm2630.proyecto.model.PerfilUsuario
import com.icm2630.proyecto.model.TipoPerfil
import com.icm2630.proyecto.repository.SesionRepository
import com.icm2630.proyecto.screens.HomeScreen
import com.icm2630.proyecto.screens.LoginScreen
import com.icm2630.proyecto.screens.ProfileScreen
import com.icm2630.proyecto.screens.ProfileSetupScreen
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
                        backStack.add(
                            when {
                                !SesionRepository.perfilConfigurado -> Routes.ProfileSetup
                                SesionRepository.perfil?.tipoPerfil == TipoPerfil.ASOCIADO -> Routes.Monitoreo
                                else -> Routes.Home
                            }
                        )
                    },
                    onIrARegister = {
                        backStack.add(Routes.Register)
                    }
                )
            }

            entry<Routes.Register> {
                RegisterScreen(
                    onRegister = { _, _, _ ->
                        backStack.clear()
                        backStack.add(Routes.ProfileSetup) // no va a Home todavía
                    },
                    onIrALogin = {
                        backStack.removeAt(backStack.lastIndex)
                    }
                )
            }

            entry<Routes.ProfileSetup> {
                ProfileSetupScreen(
                    onContinuar = { perfil ->
                        SesionRepository.perfil = perfil
                        backStack.clear()
                        backStack.add(
                            if (perfil.tipoPerfil == TipoPerfil.ASOCIADO) Routes.Monitoreo else Routes.Home
                        )
                    },
                    onCerrarSesion = {
                        backStack.clear()
                        backStack.add(Routes.Login)
                    }
                )
            }

            entry<Routes.Home> {
                HomeScreen(
                    onNavigate = { route -> navegarATab(backStack, route) },
                    onCerrarSesion = {
                        SesionRepository.perfil = null
                        backStack.clear()
                        backStack.add(Routes.Login)
                    }
                )
            }

            entry<Routes.Perfil> {
                ProfileScreen(
                    perfil = SesionRepository.perfil ?: PerfilUsuario(),
                    onNavigate = { route -> navegarATab(backStack, route) },
                    onEditarCampo = { /* TODO: conectar edición real de perfil */ },
                    onCambiarTipoPerfil = { /* TODO: conectar persistencia real del tipo de perfil */ },
                    onCerrarSesion = {
                        SesionRepository.perfil = null
                        backStack.clear()
                        backStack.add(Routes.Login)
                    }
                )
            }

            entry<Routes.Recordatorios> { PlaceholderScreen(Routes.Recordatorios, "Recordatorios", backStack) }
            entry<Routes.Registrar> { PlaceholderScreen(Routes.Registrar, "Registrar", backStack) }
            entry<Routes.Historial> { PlaceholderScreen(Routes.Historial, "Historial", backStack) }
        }
    )
}

/**
 * Las pantallas de la barra inferior son destinos hermanos, no una pila:
 * cada tap reemplaza el back stack por el destino elegido en vez de apilar.
 */
private fun navegarATab(backStack: MutableList<Routes>, destino: Routes) {
    if (backStack.lastOrNull() != destino) {
        backStack.clear()
        backStack.add(destino)
    }
}

@Composable
fun PlaceholderScreen(currentRoute: Routes, title: String, backStack: MutableList<Routes>) {
    Scaffold(
        bottomBar = {
            HealthBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route -> navegarATab(backStack, route) }
            )
        }
    ) { p ->
        Box(Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.headlineLarge)
        }
    }
}