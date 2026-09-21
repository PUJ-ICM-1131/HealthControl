package com.icm2630.proyecto.ui.screens

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.icm2630.proyecto.data.model.FormaMedicamento
import com.icm2630.proyecto.data.model.Persona
import com.icm2630.proyecto.data.model.TipoPerfil
import com.icm2630.proyecto.ui.components.medication.MedicationAttachmentCard
import com.icm2630.proyecto.ui.components.medication.MedicationDoseSelector
import com.icm2630.proyecto.ui.components.medication.MedicationDurationSelector
import com.icm2630.proyecto.ui.components.medication.MedicationFormSelector
import com.icm2630.proyecto.ui.components.medication.MedicationPersonCard
import com.icm2630.proyecto.ui.components.medication.MedicationScheduleSelector
import com.icm2630.proyecto.ui.components.medication.MedicationSectionCard
import com.icm2630.proyecto.ui.components.medication.MedicationSummaryCard
import com.icm2630.proyecto.ui.theme.Blue50
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.util.obtenerNombreArchivo
import com.icm2630.proyecto.ui.viewmodel.MedicationRegisterViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationRegisterScreen(
    medicamentoId: String? = null,
    onBack: () -> Unit,
    viewModel: MedicationRegisterViewModel = viewModel()
) {

    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val coroutineScope = rememberCoroutineScope()

    val modoEdicion = medicamentoId != null

    // ESTADO EXCLUSIVAMENTE VISUAL

    var showPersonDialog by remember {
        mutableStateOf(false)
    }

    var showStartDatePicker by remember {
        mutableStateOf(false)
    }

    var showEndDatePicker by remember {
        mutableStateOf(false)
    }

    // CARGAR MEDICAMENTO A EDITAR (o limpiar el formulario)
    LaunchedEffect(medicamentoId) {

        if (medicamentoId != null) {

            viewModel.cargarMedicamentoParaEditar(
                medicamentoId
            )

        } else {

            viewModel.iniciarNuevoMedicamento()
        }
    }

    // RESULTADO DE GUARDADO
    LaunchedEffect(
        state.guardadoExitoso,
        state.mensajeError
    ) {

        if (state.guardadoExitoso) {

            snackbarHostState.showSnackbar(
                message =
                    if (modoEdicion) {
                        "Medicamento actualizado correctamente"
                    } else {
                        "Medicamento registrado correctamente"
                    }
            )

            viewModel.consumirGuardadoExitoso()
        }


        state.mensajeError?.let { mensaje ->

            snackbarHostState.showSnackbar(
                message = mensaje
            )

            viewModel.limpiarError()
        }
    }

    // SELECTOR DE ARCHIVO
    val fileLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                val nombreArchivo =
                    obtenerNombreArchivo(
                        context = context,
                        uri = uri
                    )


                viewModel.onOrdenMedicaSeleccionada(
                    uri = uri.toString(),
                    nombreArchivo = nombreArchivo
                )
            }
        }

    // PERSONA QUE SE MOSTRARÁ
    val nombrePersona =
        when (state.perfilUsuario?.tipoPerfil) {

            TipoPerfil.TITULAR -> {
                state.perfilUsuario
                    ?.nombreCompleto
                    ?.ifBlank {
                        "Mi perfil"
                    }
                    ?: "Mi perfil"
            }

            TipoPerfil.ACOMPANANTE -> {
                state.personaSeleccionada
                    ?.nombreCompleto
                    ?: "Selecciona una persona"
            }

            null -> {
                "Mi perfil"
            }
        }


    val tipoPersona =
        if (
            state.perfilUsuario?.tipoPerfil ==
            TipoPerfil.ACOMPANANTE
        ) {

            "Persona asociada"

        } else {

            "Perfil personal"
        }


    Scaffold(

        containerColor = Blue50,

        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },

        topBar = {

            TopAppBar(

                title = {

                    Column {

                        Text(
                            text =
                                if (modoEdicion) {
                                    "Editar medicamento"
                                } else {
                                    "Registrar medicamento"
                                },
                            color = Blue700,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text =
                                if (modoEdicion) {
                                    "Actualiza la información del tratamiento"
                                } else {
                                    "Agrega la información del tratamiento"
                                },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },

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

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Blue50
                    )
            )
        },

        // BOTÓN INFERIOR
        bottomBar = {

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 10.dp
            ) {

                Button(

                    onClick = {
                        viewModel.guardarMedicamento()
                    },

                    enabled =
                        state.formularioValido &&
                                !state.guardando,

                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            horizontal = 18.dp,
                            vertical = 14.dp
                        )
                        .height(56.dp),

                    shape =
                        RoundedCornerShape(16.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Blue700
                        )
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.padding(
                            horizontal = 4.dp
                        )
                    )

                    Text(
                        text =
                            if (state.guardando) {
                                "Guardando..."
                            } else if (modoEdicion) {
                                "Guardar cambios"
                            } else {
                                "Guardar medicamento"
                            },

                        style =
                            MaterialTheme.typography.bodyLarge,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }

    ) { innerPadding ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(horizontal = 16.dp),

            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            // PERSONA
            MedicationPersonCard(

                personName = nombrePersona,

                personType = tipoPersona,

                canChangePerson =
                    state.perfilUsuario?.tipoPerfil ==
                            TipoPerfil.ACOMPANANTE,

                onChangePerson = {
                    showPersonDialog = true
                }
            )

            // 1. MEDICAMENTO
            MedicationSectionCard(
                step = 1,
                title = "Medicamento",
                subtitle =
                    "Indica cuál medicamento deseas registrar"
            ) {

                OutlinedTextField(

                    value = state.nombre,

                    onValueChange =
                        viewModel::onNombreChange,

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "Nombre del medicamento"
                        )
                    },

                    placeholder = {
                        Text(
                            "Ej. Losartán"
                        )
                    },

                    singleLine = true,

                    shape =
                        RoundedCornerShape(16.dp)
                )


                Spacer(
                    modifier = Modifier.height(18.dp)
                )


                Text(
                    text = "Presentación",
                    color = Blue700,
                    style =
                        MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )


                Spacer(
                    modifier = Modifier.height(10.dp)
                )


                MedicationFormSelector(

                    selectedForm =
                        state.forma.titulo,

                    onFormSelected = { titulo ->

                        val forma =
                            FormaMedicamento.entries
                                .firstOrNull {
                                    it.titulo == titulo
                                }

                        if (forma != null) {
                            viewModel.onFormaChange(
                                forma
                            )
                        }
                    }
                )
            }

            // 2. DOSIS
            MedicationSectionCard(
                step = 2,
                title = "Dosis",
                subtitle =
                    "Indica cuánto debe tomar en cada horario"
            ) {

                MedicationDoseSelector(

                    selectedForm =
                        state.forma.titulo,

                    dose =
                        state.dosis,

                    unit =
                        state.unidad,

                    quantityPerDose =
                        state.cantidadPorToma,

                    onDoseChange =
                        viewModel::onDosisChange,

                    onUnitChange =
                        viewModel::onUnidadChange,

                    onQuantityPerDoseChange =
                        viewModel::onCantidadPorTomaChange
                )
            }

            // 3. HORARIOS
            MedicationSectionCard(
                step = 3,
                title = "Horarios",
                subtitle =
                    "Define cuándo debe tomarse"
            ) {

                MedicationScheduleSelector(

                    schedules =
                        state.horarios,

                    onAddSchedule = {

                        TimePickerDialog(
                            context,

                            { _, hour, minute ->

                                val horario =
                                    formatMedicationTime(
                                        hour = hour,
                                        minute = minute
                                    )

                                viewModel.agregarHorario(
                                    horario
                                )
                            },

                            8,
                            0,
                            false
                        ).show()
                    },

                    onDeleteSchedule =
                        viewModel::eliminarHorario
                )
            }

            // 4. DURACIÓN
            MedicationSectionCard(
                step = 4,
                title = "Duración",
                subtitle =
                    "Indica durante cuánto tiempo debe tomarse"
            ) {

                MedicationDurationSelector(

                    startDate =
                        formatMedicationDate(
                            state.fechaInicioMillis
                        ),

                    endDate =
                        formatMedicationDate(
                            state.fechaFinMillis
                        ),

                    permanentTreatment =
                        state.tratamientoPermanente,

                    endDateError =
                        state.fechaFinInvalida,

                    onStartDateClick = {
                        showStartDatePicker = true
                    },

                    onEndDateClick = {
                        showEndDatePicker = true
                    },

                    onPermanentTreatmentChange =
                        viewModel::onTratamientoPermanenteChange
                )
            }

            // 5. INDICACIONES Y ORDEN
            MedicationSectionCard(
                step = 5,
                title = "Indicaciones",
                subtitle =
                    "Añade información adicional si es necesaria"
            ) {

                OutlinedTextField(

                    value =
                        state.indicaciones,

                    onValueChange =
                        viewModel::onIndicacionesChange,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),

                    label = {
                        Text(
                            "Indicaciones adicionales"
                        )
                    },

                    placeholder = {
                        Text(
                            "Ej. Tomar después del desayuno"
                        )
                    },

                    shape =
                        RoundedCornerShape(16.dp)
                )


                Spacer(
                    modifier = Modifier.height(18.dp)
                )


                Text(
                    text = "Orden médica",
                    color = Blue700,
                    style =
                        MaterialTheme.typography.bodyLarge,
                    fontWeight =
                        FontWeight.SemiBold
                )


                Spacer(
                    modifier = Modifier.height(8.dp)
                )


                MedicationAttachmentCard(

                    fileName =
                        state.nombreOrdenMedica,

                    onAttachClick = {

                        fileLauncher.launch(
                            arrayOf(
                                "image/*",
                                "application/pdf"
                            )
                        )
                    },

                    onRemoveClick = {
                        viewModel.eliminarOrdenMedica()
                    }
                )
            }

            // RESUMEN
            MedicationSummaryCard(

                medicationName =
                    state.nombre,

                presentation =
                    state.forma.titulo,

                dose =
                    if (state.dosis.isBlank()) {
                        ""
                    } else {
                        "${state.dosis} ${state.unidad}"
                    },

                quantityPerDose =
                    state.cantidadPorToma,

                personName =
                    nombrePersona,

                schedules =
                    state.horarios,

                startDate =
                    formatMedicationDate(
                        state.fechaInicioMillis
                    ),

                endDate =
                    formatMedicationDate(
                        state.fechaFinMillis
                    ),

                permanentTreatment =
                    state.tratamientoPermanente
            )


            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }

    // DIÁLOGO DE PERSONAS ASOCIADAS
    if (showPersonDialog) {

        PersonSelectorDialog(

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

    // FECHA INICIAL
    if (showStartDatePicker) {

        MedicationDatePickerDialog(

            initialDateMillis =
                state.fechaInicioMillis,

            onDismiss = {
                showStartDatePicker = false
            },

            onDateSelected = { millis ->

                viewModel.onFechaInicioChange(
                    millis
                )

                showStartDatePicker = false
            }
        )
    }

    // FECHA FINAL
    if (showEndDatePicker) {

        MedicationDatePickerDialog(

            initialDateMillis =
                state.fechaFinMillis,

            onDismiss = {
                showEndDatePicker = false
            },

            onDateSelected = { millis ->

                viewModel.onFechaFinChange(
                    millis
                )

                showEndDatePicker = false
            }
        )
    }
}

// SELECTOR DE PERSONA
@Composable
private fun PersonSelectorDialog(
    personas: List<Persona>,
    selectedPersonId: String?,
    onDismiss: () -> Unit,
    onPersonSelected: (Persona) -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {

            Text(
                text = "¿Para quién es el medicamento?",
                color = Blue700,
                fontWeight = FontWeight.Bold
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

                                    fontWeight =
                                        FontWeight.SemiBold
                                )

                                Text(
                                    text =
                                        "Persona asociada",

                                    style =
                                        MaterialTheme.typography.bodySmall
                                )
                            }
                        }


                        if (index < personas.lastIndex) {

                            HorizontalDivider()
                        }
                    }
                }
            }
        },

        confirmButton = {},

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    text = "Cancelar"
                )
            }
        }
    )
}

// DATE PICKER
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationDatePickerDialog(
    initialDateMillis: Long?,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {

    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                initialDateMillis
                    ?: System.currentTimeMillis()
        )


    DatePickerDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            TextButton(
                onClick = {

                    datePickerState
                        .selectedDateMillis
                        ?.let { millis ->

                            onDateSelected(
                                millis
                            )
                        }
                }
            ) {

                Text(
                    text = "Aceptar"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    text = "Cancelar"
                )
            }
        }

    ) {

        DatePicker(
            state = datePickerState
        )
    }
}

// FORMATO DE HORA
private fun formatMedicationTime(
    hour: Int,
    minute: Int
): String {

    val amPm =
        if (hour < 12) {
            "AM"
        } else {
            "PM"
        }


    val hour12 =
        when {
            hour == 0 -> 12

            hour > 12 -> hour - 12
            else ->
                hour
        }


    return String.format(
        Locale.getDefault(),
        "%d:%02d %s",
        hour12,
        minute,
        amPm
    )
}

// FORMATO DE FECHA
private fun formatMedicationDate(
    millis: Long?
): String {

    if (millis == null) {
        return ""
    }


    /*
     * Material DatePicker trabaja con fechas en UTC.
     * Definir explícitamente UTC evita que una fecha seleccionada
     * termine apareciendo como el día anterior debido a la zona
     * horaria del dispositivo.
     */
    val formatter =
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )

    formatter.timeZone =
        TimeZone.getTimeZone("UTC")


    return formatter.format(
        Date(millis)
    )
}