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
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.compose.viewModel
import com.icm2630.proyecto.ui.viewmodel.AuthViewModel
import com.icm2630.proyecto.ui.viewmodel.PerfilViewModel
import com.icm2630.proyecto.ui.viewmodel.PerfilUiState
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.repository.SesionRepository
import com.icm2630.proyecto.ui.components.AlertaCaidaDialog
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.ui.util.CaidaDetector
import com.icm2630.proyecto.ui.screens.DetalleCitaScreen
import com.icm2630.proyecto.ui.screens.DetalleMedicamentoScreen
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
    // Una sola instancia compartida por login, registro y el resto del grafo.
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val perfilViewModel: PerfilViewModel? =
        if (authState.autenticado) viewModel(key = "perfil:${authState.uid}") else null
    val estadoPerfil = perfilViewModel?.uiState?.collectAsStateWithLifecycle()?.value
        ?: PerfilUiState()
    DisposableEffect(perfilViewModel, authState.uid) {
        authState.uid?.let { perfilViewModel?.iniciar(it) }
        onDispose { perfilViewModel?.detener() }
    }

    val backStack = rememberNavBackStack(Routes.Login)

    // Tras autenticarse: onboarding si el perfil no está configurado, si no, al inicio.
    val entrarALaApp: () -> Unit = {
        backStack.clear()

        val destino = when {
            !SesionRepository.perfilConfigurado -> Routes.ProfileSetup
            else -> Routes.Home
        }

        backStack.add(
            destino
        )
    }

    LaunchedEffect(authState.autenticado, authState.cargando) {
        if (!authState.cargando) {
            val acceso = backStack.lastOrNull() == Routes.Login || backStack.lastOrNull() == Routes.Register
            if (authState.autenticado && acceso) entrarALaApp()
            else if (!authState.autenticado && !acceso) {
                backStack.clear()
                backStack.add(Routes.Login)
            }
        }
    }

    val context = LocalContext.current
    var mostrarAlertaCaida by remember { mutableStateOf(false) }

    // El sensor pertenece a la cuenta autenticada y se pausa al salir de primer plano.
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val sesionActiva = authState.autenticado && SesionRepository.perfilConfigurado &&
        backStack.lastOrNull() != Routes.Login && backStack.lastOrNull() != Routes.Register
    DisposableEffect(lifecycleOwner, sesionActiva) {
        val detector = CaidaDetector(context) { mostrarAlertaCaida = true }
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> if (sesionActiva) detector.iniciar()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> detector.detener()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (sesionActiva && lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
            detector.iniciar()
        }
        onDispose {
            detector.detener()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (mostrarAlertaCaida) {
        val telefonoEmergencia = SesionRepository.perfil?.telefonoEmergencia.orEmpty()

        AlertaCaidaDialog(
            telefonoCuidador = telefonoEmergencia,
            onCerrarAlerta = {
                mostrarAlertaCaida = false
            }
        )
    }


    if (authState.restaurando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    NavDisplay(
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),

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

                    viewModel = authViewModel,
                    onLogin = entrarALaApp,


                    onIrARegister = {

                        backStack.add(
                            Routes.Register
                        )
                    }
                )
            }

            entry<Routes.Register> {

                RegisterScreen(

                    viewModel = authViewModel,
                    onRegister = entrarALaApp,


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

                    onContinuar = {
                        backStack.clear()
                        backStack.add(Routes.Home)
                    },

                    onCerrarSesion = {

                        authViewModel.cerrarSesion()


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

                    familiar = SesionRepository.familiarActivo,

                    onNavigate = { route ->

                        navegarATab(
                            backStack = backStack,
                            destino = route
                        )
                    },

                    onVolverAMiPerfil = {

                        SesionRepository.volverAMiPerfil()
                    }
                )
            }

            entry<Routes.Mapa> {

                MapScreen(

                    perfil =
                        SesionRepository.perfil
                            ?: PerfilUsuario(),

                    familiar = SesionRepository.familiarActivo,

                    onVolverAMiPerfil = {
                        SesionRepository.volverAMiPerfil()
                    },

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

                    familiar = SesionRepository.familiarActivo,

                    onNavigate = { route ->

                        navegarATab(
                            backStack = backStack,
                            destino = route
                        )
                    },


                    // La edición de cada campo se resuelve dentro de ProfileScreen;
                    // aquí solo se persiste el perfil actualizado.
                    miUid = authState.uid.orEmpty(),
                    estado = estadoPerfil,
                    onActualizarPerfil = { perfilViewModel?.guardarPerfil(it) },
                    onEnviarCodigo = { perfilViewModel?.enviar(it) },
                    onResponder = { invitacion, aceptar -> perfilViewModel?.responder(invitacion, aceptar) },
                    onCancelarSolicitud = { perfilViewModel?.cancelar(it) },
                    onDesvincular = { perfilViewModel?.desvincular(it) },
                    onConsumirMensaje = { perfilViewModel?.consumirMensaje() },
                    onConsumirEnvio = { perfilViewModel?.consumirEnvio() },

                    // Al elegir un familiar se consulta su HealthControl desde el inicio.
                    onVerFamiliar = { persona ->

                        SesionRepository.verFamiliar(persona)

                        navegarATab(
                            backStack = backStack,
                            destino = Routes.Home
                        )
                    },


                    onVolverAMiPerfil = {

                        SesionRepository.volverAMiPerfil()
                    },


                    onCerrarSesion = {

                        authViewModel.cerrarSesion()


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
    backStack: MutableList<NavKey>,
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
    backStack: MutableList<NavKey>
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