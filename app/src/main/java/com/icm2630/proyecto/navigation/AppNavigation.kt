package com.icm2630.proyecto.navigation

import com.icm2630.proyecto.ui.screens.RegisterOptionsScreen
import com.icm2630.proyecto.ui.screens.AppointmentRegisterScreen
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
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.TipoPerfil
import com.icm2630.proyecto.data.repository.SesionRepository
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.ui.screens.HistoryScreen
import com.icm2630.proyecto.ui.screens.HomeScreen
import com.icm2630.proyecto.ui.screens.LoginScreen
import com.icm2630.proyecto.ui.screens.MedicationRegisterScreen
import com.icm2630.proyecto.ui.screens.ProfileScreen
import com.icm2630.proyecto.ui.screens.ProfileSetupScreen
import com.icm2630.proyecto.ui.screens.RegisterScreen
import com.icm2630.proyecto.ui.screens.RemindersScreen


@Composable
fun AppNavigation() {

    /*
     * Pila de navegación de la aplicación.
     *
     * La aplicación inicia en Login.
     */
    val backStack = remember {
        mutableStateListOf<Routes>(
            Routes.Login
        )
    }


    NavDisplay(

        backStack = backStack,

        /*
         * Comportamiento general del botón Atrás.
         */
        onBack = {

            if (backStack.size > 1) {

                backStack.removeAt(
                    backStack.lastIndex
                )
            }
        },

        entryProvider = entryProvider {


            // =================================================
            // LOGIN
            // =================================================

            entry<Routes.Login> {

                LoginScreen(

                    onLogin = {

                        backStack.clear()


                        val destino =
                            when {

                                !SesionRepository.perfilConfigurado -> {

                                    Routes.ProfileSetup
                                }

                                SesionRepository
                                    .perfil
                                    ?.tipoPerfil ==
                                        TipoPerfil.ASOCIADO -> {

                                    Routes.Monitoreo
                                }

                                else -> {

                                    Routes.Home
                                }
                            }


                        backStack.add(
                            destino
                        )
                    },


                    onIrARegister = {

                        backStack.add(
                            Routes.Register
                        )
                    }
                )
            }


            // =================================================
            // REGISTRO DE USUARIO
            // =================================================

            entry<Routes.Register> {

                RegisterScreen(

                    onRegister = { _, _, _ ->

                        backStack.clear()

                        backStack.add(
                            Routes.ProfileSetup
                        )
                    },


                    onIrALogin = {

                        if (backStack.size > 1) {

                            backStack.removeAt(
                                backStack.lastIndex
                            )
                        }
                    }
                )
            }

            entry<Routes.ProfileSetup> {

                ProfileSetupScreen(

                    onContinuar = { perfil ->

                        SesionRepository.perfil =
                            perfil


                        backStack.clear()


                        val destino =
                            if (
                                perfil.tipoPerfil ==
                                TipoPerfil.ASOCIADO
                            ) {

                                Routes.Monitoreo

                            } else {

                                Routes.Home
                            }


                        backStack.add(
                            destino
                        )
                    },


                    onCerrarSesion = {

                        SesionRepository
                            .cerrarSesion()


                        backStack.clear()

                        backStack.add(
                            Routes.Login
                        )
                    }
                )
            }

            entry<Routes.Home> {

                HomeScreen(

                    onNavigate = { route ->

                        navegarATab(
                            backStack = backStack,
                            destino = route
                        )
                    },


                    onCerrarSesion = {

                        SesionRepository
                            .cerrarSesion()


                        backStack.clear()

                        backStack.add(
                            Routes.Login
                        )
                    }
                )
            }

            entry<Routes.Monitoreo> {

                PlaceholderScreen(
                    currentRoute =
                        Routes.Monitoreo,

                    title =
                        "Monitoreo",

                    backStack =
                        backStack
                )
            }

            entry<Routes.Recordatorios> {

                RemindersScreen(

                    onNavigate = { route ->

                        navegarATab(
                            backStack = backStack,
                            destino = route
                        )
                    }
                )
            }

// =================================================
// REGISTRAR
// =================================================

            entry<Routes.Registrar> {

                RegisterOptionsScreen(

                    onNavigate = { route ->

                        navegarATab(
                            backStack = backStack,
                            destino = route
                        )
                    },


                    onRegisterAppointment = {

                        backStack.add(
                            Routes.RegistrarCita
                        )
                    },


                    onRegisterMedication = {

                        backStack.add(
                            Routes.RegistrarMedicamento
                        )
                    }
                )
            }

            entry<Routes.RegistrarMedicamento> {

                MedicationRegisterScreen(

                    onBack = {

                        if (backStack.size > 1) {

                            backStack.removeAt(
                                backStack.lastIndex
                            )

                        } else {

                            backStack.clear()

                            backStack.add(
                                Routes.Registrar
                            )
                        }
                    }
                )
            }

// =================================================
// REGISTRAR CITA
// =================================================

            entry<Routes.RegistrarCita> {

                AppointmentRegisterScreen(

                    onBack = {

                        if (backStack.size > 1) {

                            backStack.removeAt(
                                backStack.lastIndex
                            )

                        } else {

                            backStack.clear()

                            backStack.add(
                                Routes.Registrar
                            )
                        }
                    }
                )
            }

            entry<Routes.Historial> {

                HistoryScreen(

                    onNavigate = { route ->

                        navegarATab(
                            backStack = backStack,
                            destino = route
                        )
                    }
                )
            }

            entry<Routes.Perfil> {

                ProfileScreen(

                    perfil =
                        SesionRepository.perfil
                            ?: PerfilUsuario(),


                    onNavigate = { route ->

                        navegarATab(
                            backStack = backStack,
                            destino = route
                        )
                    },


                    onEditarCampo = {

                        /*
                         * TODO:
                         * Conectar edición real del perfil.
                         */
                    },


                    onCambiarTipoPerfil = {


                    },


                    onCerrarSesion = {

                        SesionRepository
                            .cerrarSesion()


                        backStack.clear()

                        backStack.add(
                            Routes.Login
                        )
                    }
                )
            }
        }
    )
}


/**
 * Las pantallas principales de la barra inferior
 * son destinos hermanos.
 *
 * Cuando el usuario cambia de pestaña,
 * reemplazamos el destino actual para evitar:
 *
 * Home -> Recordatorios -> Registrar -> Historial...
 *
 * acumulándose en el backStack.
 */
private fun navegarATab(
    backStack: MutableList<Routes>,
    destino: Routes
) {

    if (
        backStack.lastOrNull() !=
        destino
    ) {

        backStack.clear()

        backStack.add(
            destino
        )
    }
}


// =============================================================
// PLACEHOLDER
// =============================================================

@Composable
fun PlaceholderScreen(
    currentRoute: Routes,
    title: String,
    backStack: MutableList<Routes>
) {

    Scaffold(

        bottomBar = {

            HealthBottomNavigation(

                currentRoute =
                    currentRoute,

                onNavigate = { route ->

                    navegarATab(
                        backStack = backStack,
                        destino = route
                    )
                }
            )
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    paddingValues
                ),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = title,

                style =
                    MaterialTheme
                        .typography
                        .headlineLarge
            )
        }
    }
}