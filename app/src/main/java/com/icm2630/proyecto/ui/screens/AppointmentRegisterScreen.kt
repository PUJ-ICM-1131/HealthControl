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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.icm2630.proyecto.data.model.ModalidadCita
import com.icm2630.proyecto.data.model.Persona
import com.icm2630.proyecto.data.model.TipoCita
import com.icm2630.proyecto.data.model.TipoPerfil
import com.icm2630.proyecto.ui.components.appointment.AppointmentAttachmentCard
import com.icm2630.proyecto.ui.components.appointment.AppointmentDateTimeCard
import com.icm2630.proyecto.ui.components.appointment.AppointmentLocationCard
import com.icm2630.proyecto.ui.components.appointment.AppointmentPersonCard
import com.icm2630.proyecto.ui.components.appointment.AppointmentSectionCard
import com.icm2630.proyecto.ui.components.appointment.AppointmentSummaryCard
import com.icm2630.proyecto.ui.components.appointment.AppointmentTypeSelector
import com.icm2630.proyecto.ui.theme.Blue50
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.util.obtenerNombreArchivo
import com.icm2630.proyecto.ui.viewmodel.AppointmentRegisterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentRegisterScreen(
    citaId: String? = null,
    onBack: () -> Unit,
    viewModel: AppointmentRegisterViewModel = viewModel()
) {

    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var showPersonDialog by remember {
        mutableStateOf(false)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val modoEdicion = citaId != null

    // CARGAR CITA A EDITAR (o limpiar el formulario si es nueva)
    LaunchedEffect(citaId) {

        if (citaId != null) {

            viewModel.cargarCitaParaEditar(
                citaId
            )

        } else {

            viewModel.iniciarNuevaCita()
        }
    }

    // MENSAJES DE GUARDADO / ERROR
    LaunchedEffect(
        state.guardadoExitoso,
        state.mensajeError
    ) {

        if (state.guardadoExitoso) {

            snackbarHostState.showSnackbar(
                message =
                    if (modoEdicion) {
                        "Cita actualizada correctamente"
                    } else {
                        "Cita registrada correctamente"
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

    // SELECTOR DE SOPORTE
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

                viewModel.onSoporteSeleccionado(
                    uri = uri.toString(),
                    nombreArchivo = nombreArchivo
                )
            }
        }

    // PERSONA A MOSTRAR
    val nombrePersona =
        when (state.perfilUsuario?.tipoPerfil) {

            TipoPerfil.TITULAR -> {
                state.perfilUsuario
                    ?.nombreCompleto
                    ?.ifBlank { "Yo" }
                    ?: "Yo"
            }

            TipoPerfil.ACOMPANANTE -> {
                if (state.citaParaMi) {
                    state.perfilUsuario
                        ?.nombreCompleto
                        ?.ifBlank { "Yo" }
                        ?: "Yo"

                } else {
                    state.personaSeleccionada
                        ?.nombreCompleto
                        ?: "Selecciona una persona"
                }
            }

            null -> {
                "Yo"
            }
        }


    val tipoPersona =
        when {

            state.perfilUsuario?.tipoPerfil == TipoPerfil.TITULAR -> {
                "Mi perfil"
            }

            state.citaParaMi -> {
                "Mi perfil"
            }

            else -> {
                "Persona asociada"
            }
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
                                    "Editar cita"
                                } else {
                                    "Registrar cita"
                                },
                            color = Blue700,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text =
                                if (modoEdicion) {
                                    "Actualiza la información de la cita médica"
                                } else {
                                    "Agrega la información de la cita médica"
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

        // BOTÓN GUARDAR
        bottomBar = {

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 10.dp
            ) {

                Button(
                    onClick = {
                        viewModel.guardarCita()
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

                    shape = RoundedCornerShape(16.dp),

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
                                "Guardar cita"
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
            AppointmentPersonCard(
                personName = nombrePersona,

                personType = tipoPersona,

                canChangePerson =
                    state.perfilUsuario?.tipoPerfil ==
                            TipoPerfil.ACOMPANANTE,

                onChangePerson = {
                    showPersonDialog = true
                }
            )

            // 1. INFORMACIÓN DE LA CITA
            AppointmentSectionCard(
                step = 1,
                title = "Información de la cita",
                subtitle = "Indica el tipo y el motivo de la consulta"
            ) {

                Text(
                    text = "Tipo de cita",
                    color = Blue700,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )


                Spacer(
                    modifier = Modifier.height(10.dp)
                )


                AppointmentTypeSelector(
                    selectedType =
                        state.tipoCita.titulo,

                    onTypeSelected = { titulo ->

                        val tipo =
                            TipoCita.entries
                                .firstOrNull {
                                    it.titulo == titulo
                                }

                        if (tipo != null) {
                            viewModel.onTipoCitaChange(
                                tipo
                            )
                        }
                    }
                )


                if (
                    state.tipoCita ==
                    TipoCita.ESPECIALISTA
                ) {

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )


                    OutlinedTextField(
                        value =
                            state.especialidad,

                        onValueChange =
                            viewModel::onEspecialidadChange,

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {
                            Text("Especialidad")
                        },

                        placeholder = {
                            Text("Ej. Cardiología")
                        },

                        singleLine = true,

                        shape =
                            RoundedCornerShape(16.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                OutlinedTextField(
                    value =
                        state.motivo,

                    onValueChange =
                        viewModel::onMotivoChange,

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text("Motivo de la cita")
                    },

                    placeholder = {
                        Text(
                            "Ej. Control de presión arterial"
                        )
                    },

                    minLines = 2,

                    shape =
                        RoundedCornerShape(16.dp)
                )

                // NOMBRE DEL MÉDICO O ESPECIALISTA
                Spacer(
                    modifier = Modifier.height(16.dp)
                )


                OutlinedTextField(
                    value = state.nombreMedico,

                    onValueChange =
                        viewModel::onNombreMedicoChange,

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "Nombre del médico o especialista"
                        )
                    },

                    placeholder = {
                        Text(
                            "Ej. Dra. Laura Gómez"
                        )
                    },

                    singleLine = true,

                    shape =
                        RoundedCornerShape(16.dp)
                )
            }

            // 2. FECHA Y HORA

            AppointmentSectionCard(
                step = 2,
                title = "Fecha y hora",
                subtitle = "Selecciona cuándo se realizará la cita"
            ) {

                AppointmentDateTimeCard(
                    date =
                        formatAppointmentDate(
                            state.fechaMillis
                        ),

                    time =
                        formatAppointmentTime(
                            state.hora,
                            state.minuto
                        ),

                    onDateClick = {
                        showDatePicker = true
                    },

                    onTimeClick = {

                        TimePickerDialog(
                            context,

                            { _, hour, minute ->

                                viewModel.onHoraChange(
                                    hora = hour,
                                    minuto = minute
                                )
                            },

                            state.hora ?: 8,
                            state.minuto ?: 0,
                            false
                        ).show()
                    }
                )
            }

            // 3. LUGAR
            AppointmentSectionCard(
                step = 3,
                title = "Lugar",
                subtitle = "Indica dónde se realizará la cita"
            ) {

                AppointmentLocationCard(
                    modality =
                        state.modalidad.titulo,

                    institution =
                        state.institucion,

                    address =
                        state.direccion,

                    virtualLink =
                        state.enlaceVirtual,

                    onModalityChange = { modalidad ->

                        val nuevaModalidad =
                            ModalidadCita.entries
                                .firstOrNull {
                                    it.titulo == modalidad
                                }

                        if (nuevaModalidad != null) {

                            viewModel.onModalidadChange(
                                nuevaModalidad
                            )
                        }
                    },

                    onInstitutionChange =
                        viewModel::onInstitucionChange,

                    onAddressChange =
                        viewModel::onDireccionChange,

                    onVirtualLinkChange =
                        viewModel::onEnlaceVirtualChange
                )
            }

            // 4. INFORMACIÓN ADICIONAL
            AppointmentSectionCard(
                step = 4,
                title = "Información adicional",
                subtitle = "Agrega información útil para recordar la cita"
            ) {
                // NOTAS
                OutlinedTextField(
                    value = state.notas,

                    onValueChange =
                        viewModel::onNotasChange,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),

                    label = {
                        Text("Notas")
                    },

                    placeholder = {
                        Text(
                            "Ej. Llevar resultados de laboratorio"
                        )
                    },

                    shape =
                        RoundedCornerShape(16.dp)
                )


                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                // SOPORTE
                Text(
                    text = "Soporte",
                    color = Blue700,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )


                Spacer(
                    modifier = Modifier.height(8.dp)
                )


                AppointmentAttachmentCard(
                    fileName =
                        state.nombreSoporte,

                    onAttachClick = {

                        fileLauncher.launch(
                            arrayOf(
                                "image/*",
                                "application/pdf"
                            )
                        )
                    },

                    onRemoveClick = {
                        viewModel.eliminarSoporte()
                    }
                )
            }

            // RESUMEN
            AppointmentSummaryCard(
                personName = nombrePersona,

                appointmentType = state.tipoCita.titulo,

                specialty = state.especialidad,

                date = formatAppointmentDate(
                        state.fechaMillis
                    ),

                time = formatAppointmentTime(
                        state.hora,
                        state.minuto
                    ),

                modality = state.modalidad.titulo,

                institution = state.institucion,

                address = state.direccion,

                virtualLink = state.enlaceVirtual,

                doctorName = state.nombreMedico
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }

    // DIÁLOGO DE PERSONA
    if (showPersonDialog) {

        AppointmentPersonSelectorDialog(

            personas = state.personasAsociadas,

            citaParaMi = state.citaParaMi,

            selectedPersonId = state.personaSeleccionadaId,

            onDismiss = {
                showPersonDialog = false
            },

            onSelectSelf = {

                viewModel.seleccionarYo()

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

    // DATE PICKER
    if (showDatePicker) {

        AppointmentDatePickerDialog(
            initialDateMillis =
                state.fechaMillis,

            onDismiss = {
                showDatePicker = false
            },

            onDateSelected = { millis ->

                viewModel.onFechaChange(
                    millis
                )

                showDatePicker = false
            }
        )
    }
}

// SELECTOR DE PERSONA
@Composable
private fun AppointmentPersonSelectorDialog(
    personas: List<Persona>,
    citaParaMi: Boolean,
    selectedPersonId: String?,
    onDismiss: () -> Unit,
    onSelectSelf: () -> Unit,
    onPersonSelected: (Persona) -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {

            Text(
                text = "¿Para quién es la cita?",
                color = Blue700,
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            Column {

                // YO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectSelf()
                        }
                        .padding(
                            vertical = 10.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    RadioButton(
                        selected = citaParaMi,

                        onClick = {
                            onSelectSelf()
                        }
                    )


                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Yo",
                            fontWeight =
                                FontWeight.SemiBold
                        )


                        Text(
                            text = "Registrar la cita para mí",

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }
                }


                if (personas.isNotEmpty()) {

                    HorizontalDivider()


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
                                    !citaParaMi &&
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
private fun AppointmentDatePickerDialog(
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

                Text("Aceptar")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("Cancelar")
            }
        }

    ) {

        DatePicker(
            state = datePickerState
        )
    }
}

// FORMATEAR FECHA
private fun formatAppointmentDate(
    millis: Long?
): String {

    if (millis == null) {
        return ""
    }

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

// FORMATEAR HORA
private fun formatAppointmentTime(
    hour: Int?,
    minute: Int?
): String {
    if (
        hour == null ||
        minute == null
    ) {
        return ""
    }

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
            else -> hour
        }

    return String.format(
        Locale.getDefault(),
        "%d:%02d %s",
        hour12,
        minute,
        amPm
    )
}