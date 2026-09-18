package com.icm2630.proyecto.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.icm2630.proyecto.data.model.Persona
import com.icm2630.proyecto.ui.components.map.MapPersonCard
import com.icm2630.proyecto.ui.components.map.MapPreviewCard
import com.icm2630.proyecto.ui.components.map.MapQuickActions
import com.icm2630.proyecto.ui.components.map.SafetyStatusCard
import com.icm2630.proyecto.ui.theme.Blue50
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.viewmodel.MapViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {

    val state by viewModel.uiState.collectAsState()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var showPersonDialog by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // MENSAJES
    // =========================================================

    LaunchedEffect(
        state.mensajeError
    ) {

        state.mensajeError?.let { mensaje ->

            snackbarHostState.showSnackbar(
                message = mensaje
            )

            viewModel.limpiarError()
        }
    }


    // =========================================================
    // PANTALLA
    // =========================================================

    Scaffold(

        containerColor = Blue50,


        // =====================================================
        // BARRA SUPERIOR
        // =====================================================

        topBar = {

            TopAppBar(

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Volver",
                            tint = Blue700
                        )
                    }
                },


                title = {

                    Column {

                        Text(
                            text = "Ubicación y seguridad",
                            color = Blue700,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )


                        Text(
                            text = "Seguimiento del familiar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },


                actions = {

                    Surface(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable {
                                // SOS mock para esta primera entrega
                            },

                        shape = RoundedCornerShape(12.dp),

                        color = Color(0xFFFFE8E8)
                    ) {

                        Text(
                            text = "SOS",

                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 8.dp
                            ),

                            color = Color(0xFFD62828),

                            style = MaterialTheme.typography.labelLarge,

                            fontWeight = FontWeight.Bold
                        )
                    }
                },


                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue50
                )
            )
        },


        snackbarHost = {

            SnackbarHost(
                hostState = snackbarHostState
            )
        }

    ) { paddingValues ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {


            Spacer(
                modifier = Modifier.height(6.dp)
            )


            // =================================================
            // PERSONA MONITOREADA
            // =================================================

            MapPersonCard(

                personName =
                    state.nombrePersona,

                canChangePerson =
                    state.puedeCambiarPersona,

                onChangePerson = {
                    showPersonDialog = true
                }
            )


            // =================================================
            // UBICACIÓN DISPONIBLE
            // =================================================

            state.ubicacion?.let { ubicacion ->


                // =============================================
                // MAPA
                // =============================================

                MapPreviewCard(

                    personName =
                        state.nombrePersona,

                    address =
                        ubicacion.direccion,

                    lastUpdate =
                        "Actualizado ${
                            formatLastUpdate(
                                ubicacion.ultimaActualizacionMillis
                            )
                        }"
                )


                // =============================================
                // ESTADO DE SEGURIDAD
                // =============================================

                SafetyStatusCard()


                // =============================================
                // ACCIONES RÁPIDAS
                // =============================================

                MapQuickActions(

                    onCall = {
                        // Mock por ahora
                        // Después puede abrir el marcador telefónico
                    },

                    onDirections = {
                        // Mock por ahora
                        // Después puede abrir Google Maps
                    },

                    onSafeZone = {
                        // Mock por ahora
                        // Después permitirá configurar la zona segura
                    },

                    onHistory = {
                        // Mock por ahora
                        // Después mostrará historial de ubicaciones
                    }
                )
            }


            // =================================================
            // UBICACIÓN NO DISPONIBLE
            // =================================================

            if (
                state.ubicacion == null &&
                !state.cargando
            ) {

                Surface(
                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(22.dp),

                    color = Color.White,

                    shadowElevation = 2.dp
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "Ubicación no disponible",

                            color = Blue700,

                            style = MaterialTheme.typography.titleMedium,

                            fontWeight = FontWeight.Bold
                        )


                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )


                        Text(
                            text = "Todavía no existe información de ubicación para esta persona.",

                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,

                            style =
                                MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }


            // =================================================
            // ACTUALIZAR UBICACIÓN
            // =================================================

            Button(

                onClick = {
                    viewModel.actualizarUbicacion()
                },

                enabled =
                    !state.cargando,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                shape =
                    RoundedCornerShape(18.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Blue700
                    )
            ) {


                if (state.cargando) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.padding(
                                end = 10.dp
                            ),

                        strokeWidth =
                            2.dp,

                        color =
                            Color.White
                    )

                } else {

                    Icon(
                        imageVector =
                            Icons.Outlined.Refresh,

                        contentDescription =
                            null
                    )


                    Spacer(
                        modifier =
                            Modifier.padding(
                                horizontal = 4.dp
                            )
                    )
                }


                Text(
                    text =
                        if (state.cargando) {
                            "Actualizando..."
                        } else {
                            "Actualizar ubicación"
                        },

                    style =
                        MaterialTheme.typography.bodyLarge,

                    fontWeight =
                        FontWeight.Bold
                )
            }


            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )
        }
    }


    // =========================================================
    // DIÁLOGO PARA CAMBIAR PERSONA
    // =========================================================

    if (showPersonDialog) {

        MapPersonSelectorDialog(

            personas =
                state.personasAsociadas,

            selectedPersonId =
                state.personaSeleccionadaId,

            onDismiss = {
                showPersonDialog = false
            },

            onPersonSelected = { persona ->

                viewModel.seleccionarPersona(
                    persona.id
                )

                showPersonDialog = false
            }
        )
    }
}


// =============================================================
// SELECTOR DE PERSONA
// =============================================================

@Composable
private fun MapPersonSelectorDialog(
    personas: List<Persona>,
    selectedPersonId: String?,
    onDismiss: () -> Unit,
    onPersonSelected: (Persona) -> Unit
) {

    AlertDialog(

        onDismissRequest =
            onDismiss,


        title = {

            Text(
                text =
                    "¿De quién deseas ver la ubicación?",

                color =
                    Blue700,

                fontWeight =
                    FontWeight.Bold
            )
        },


        text = {

            Column {

                if (personas.isEmpty()) {

                    Text(
                        text =
                            "No tienes personas asociadas disponibles.",

                        style =
                            MaterialTheme.typography.bodyMedium
                    )

                } else {

                    personas.forEachIndexed { index, persona ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    onPersonSelected(
                                        persona
                                    )
                                }
                                .padding(
                                    vertical = 10.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {


                            RadioButton(
                                selected =
                                    persona.id ==
                                            selectedPersonId,

                                onClick = {

                                    onPersonSelected(
                                        persona
                                    )
                                }
                            )


                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(
                                    text =
                                        persona.nombreCompleto,

                                    color =
                                        Blue700,

                                    fontWeight =
                                        FontWeight.SemiBold
                                )


                                Text(
                                    text =
                                        "Persona asociada",

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant,

                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall
                                )
                            }
                        }


                        if (
                            index <
                            personas.lastIndex
                        ) {

                            HorizontalDivider()
                        }
                    }
                }
            }
        },


        confirmButton = {},


        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    text = "Cancelar"
                )
            }
        }
    )
}


// =============================================================
// FORMATEAR ÚLTIMA ACTUALIZACIÓN
// =============================================================

private fun formatLastUpdate(
    millis: Long
): String {

    val diferencia =
        System.currentTimeMillis() -
                millis


    val minutos =
        diferencia /
                (60 * 1000)


    val horas =
        minutos / 60


    val dias =
        horas / 24


    return when {

        diferencia < 0 -> {
            "recientemente"
        }


        minutos < 1 -> {
            "hace unos segundos"
        }


        minutos < 60 -> {

            if (minutos == 1L) {
                "hace 1 minuto"
            } else {
                "hace $minutos minutos"
            }
        }


        horas < 24 -> {

            if (horas == 1L) {
                "hace 1 hora"
            } else {
                "hace $horas horas"
            }
        }


        else -> {

            if (dias == 1L) {
                "hace 1 día"
            } else {
                "hace $dias días"
            }
        }
    }
}