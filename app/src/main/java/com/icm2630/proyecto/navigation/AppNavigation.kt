package com.icm2630.proyecto.navigation

import com.icm2630.proyecto.ui.screens.MapScreen
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
import com.icm2630.proyecto.ui.screens.DetalleCitaScreen
import com.icm2630.proyecto.ui.screens.DetalleMedicamentoScreen
import com.icm2630.proyecto.ui.screens.HistoryScreen
import com.icm2630.proyecto.ui.screens.HomeScreen
import com.icm2630.proyecto.ui.screens.LoginScreen
import com.icm2630.proyecto.ui.screens.MedicationRegisterScreen
import com.icm2630.proyecto.ui.screens.MonitoreoScreen
import com.icm2630.proyecto.ui.screens.ProfileScreen
import com.icm2630.proyecto.ui.screens.ProfileSetupScreen
import com.icm2630.proyecto.ui.screens.RegisterScreen
import com.icm2630.proyecto.ui.screens.RemindersScreen


@Composable
fun AppNavigation() {

    val backStack = remember {
        mutableStateListOf<Routes>(
            Routes.Login
        )
    }


    NavDisplay(

        backStack = backStack,

        onBack = {

            if (backStack.size > 1) {

                backStack.removeAt(
                    backStack.lastIndex
                )
            }
        },

        entryProvider = entryProvider {

            entry<Routes.Login> {

                LoginScreen(

                    onLogin = {

                        backStack.clear()

                        val destino = when {
                            !SesionRepository.perfilConfigurado -> Routes.ProfileSetup
                            SesionRepository.perfil?.tipoPerfil == TipoPerfil.ACOMPANANTE -> Routes.Monitoreo
                            else -> Routes.Home
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

            entry<Routes.Register> {

                RegisterScreen(

                    onRegister = { nombre, correo, _ ->

                        SesionRepository.nombreCuenta = nombre
                        SesionRepository.correoCuenta = correo

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

                    nombre = SesionRepository.nombreCuenta,
                    contacto = SesionRepository.correoCuenta,

                    onContinuar = { perfil ->

                        SesionRepository.perfil =
                            perfil


                        backStack.clear()

                        val destino = if (perfil.tipoPerfil == TipoPerfil.ACOMPANANTE) {
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

                    perfil = SesionRepository.perfil ?: PerfilUsuario(),

                    onNavigate = { route ->

                        navegarATab(
                            backStack = backStack,
                            destino = route
                        )
                    }
                )
            }

            entry<Routes.Monitoreo> {

                MonitoreoScreen(

                    perfil =
                        SesionRepository.perfil
                            ?: PerfilUsuario(),

                    onNavigate = { route ->

                        navegarATab(
                            backStack = backStack,
                            destino = route
                        )
                    },

                    onCerrarSesion = {

                        SesionRepository.cerrarSesion()

                        backStack.clear()

                        backStack.add(
                            Routes.Login
                        )
                    }
                )
            }

            entry<Routes.Mapa> {

                MapScreen(

                    perfil =
                        SesionRepository.perfil
                            ?: PerfilUsuario(),

                    onNavigate = { route ->

                        navegarATab(
                            backStack = backStack,
                            destino = route
                        )
                    }
                )
            }

            entry<Routes.Recordatorios> {

                RemindersScreen(

                    onNavigate = { route ->

                        navegarATab(
                            backStack = backStack,
                            destino = route
                        )
                    },

                    onVerDetalle = { route ->

                        backStack.add(
                            route
                        )
                    }
                )
            }

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
                            Routes.RegistrarCita()
                        )
                    },


                    onRegisterMedication = {

                        backStack.add(
                            Routes.RegistrarMedicamento()
                        )
                    }
                )
            }

            entry<Routes.RegistrarMedicamento> { key ->

                MedicationRegisterScreen(

                    medicamentoId = key.medicamentoId,

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

            entry<Routes.RegistrarCita> { key ->

                AppointmentRegisterScreen(

                    citaId = key.citaId,

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

            entry<Routes.DetalleCita> { key ->

                DetalleCitaScreen(

                    citaId = key.citaId,

                    onBack = {

                        if (backStack.size > 1) {

                            backStack.removeAt(
                                backStack.lastIndex
                            )
                        }
                    },


                    onEditar = {

                        backStack.add(
                            Routes.RegistrarCita(
                                citaId = key.citaId
                            )
                        )
                    },


                    onEliminar = {

                        if (backStack.size > 1) {

                            backStack.removeAt(
                                backStack.lastIndex
                            )
                        }
                    }
                )
            }


            entry<Routes.DetalleMedicamento> { key ->

                DetalleMedicamentoScreen(

                    medicamentoId = key.medicamentoId,

                    onBack = {

                        if (backStack.size > 1) {

                            backStack.removeAt(
                                backStack.lastIndex
                            )
                        }
                    },


                    onEditar = {

                        backStack.add(
                            Routes.RegistrarMedicamento(
                                medicamentoId = key.medicamentoId
                            )
                        )
                    },


                    onEliminar = {

                        if (backStack.size > 1) {

                            backStack.removeAt(
                                backStack.lastIndex
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
                    },

                    onVerDetalle = { route ->

                        backStack.add(
                            route
                        )
                    }
                )
            }

            entry<Routes.Perfil> {

                ProfileScreen(

                    perfil = SesionRepository.perfil ?: PerfilUsuario(),

                    onNavigate = { route ->

                        navegarATab(
                            backStack = backStack,
                            destino = route
                        )
                    },


                    // La edición de cada campo se resuelve dentro de ProfileScreen;
                    // aquí solo se persiste el perfil actualizado.
                    onActualizarPerfil = { actualizado ->

                        SesionRepository.perfil = actualizado
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