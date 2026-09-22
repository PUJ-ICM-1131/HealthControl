package com.icm2630.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.Cita
import com.icm2630.proyecto.data.model.ModalidadCita
import com.icm2630.proyecto.data.repository.CitaRepository
import com.icm2630.proyecto.data.repository.PersonaRepository
import com.icm2630.proyecto.data.repository.SesionRepository
import com.icm2630.proyecto.ui.components.descripcionConParentesco
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue500
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.ErrorRed
import com.icm2630.proyecto.ui.theme.TextPrimary
import com.icm2630.proyecto.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val Fondo = Color(0xFFF8FAFF)

// PANTALLA DE DETALLE DE UNA CITA
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCitaScreen(
    citaId: String,
    onBack: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {


    val cita = CitaRepository.obtenerPorId(citaId)

    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    Scaffold(

        topBar = {

            TopAppBar(

                navigationIcon = {

                    IconButton(onClick = onBack) {

                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Volver",
                            tint = Blue700
                        )
                    }
                },

                title = {

                    Text(
                        text = "Detalle de la cita",
                        color = Blue700,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Fondo
                )
            )
        },

        containerColor = Fondo

    ) { paddingValues ->

        if (cita == null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Esta cita ya no está disponible.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        } else {


            val esHistorico = cita.fechaMillis < obtenerHoyUtcMillisCita()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {

                EncabezadoCita(cita)

                Spacer(Modifier.height(20.dp))

                TarjetaDetalle {

                    DetalleCampo(
                        icono = Icons.Outlined.CalendarMonth,
                        etiqueta = "Fecha",
                        valor = formatearFechaCita(cita.fechaMillis)
                    )
                    DivisorCampo()

                    DetalleCampo(
                        icono = Icons.Outlined.Schedule,
                        etiqueta = "Hora",
                        valor = formatearHoraCita(cita.hora, cita.minuto)
                    )
                    DivisorCampo()

                    DetalleCampo(
                        icono = Icons.Outlined.LocalHospital,
                        etiqueta = "Tipo de cita",
                        valor = cita.tipo.titulo
                    )

                    if (cita.especialidad.isNotBlank()) {
                        DivisorCampo()
                        DetalleCampo(
                            icono = Icons.Outlined.MedicalServices,
                            etiqueta = "Especialidad",
                            valor = cita.especialidad
                        )
                    }

                    DivisorCampo()

                    if (cita.modalidad == ModalidadCita.VIRTUAL) {

                        DetalleCampo(
                            icono = Icons.Outlined.VideoCall,
                            etiqueta = "Modalidad",
                            valor = "Virtual"
                        )

                        if (cita.enlaceVirtual.isNotBlank()) {
                            DivisorCampo()
                            DetalleCampo(
                                icono = Icons.Outlined.Link,
                                etiqueta = "Enlace virtual",
                                valor = cita.enlaceVirtual
                            )
                        }

                    } else {

                        DetalleCampo(
                            icono = Icons.Outlined.Place,
                            etiqueta = "Modalidad",
                            valor = "Presencial"
                        )

                        if (cita.institucion.isNotBlank()) {
                            DivisorCampo()
                            DetalleCampo(
                                icono = Icons.Outlined.LocationOn,
                                etiqueta = "Institución",
                                valor = cita.institucion
                            )
                        }

                        if (cita.direccion.isNotBlank()) {
                            DivisorCampo()
                            DetalleCampo(
                                icono = Icons.Outlined.Map,
                                etiqueta = "Dirección",
                                valor = cita.direccion
                            )
                        }
                    }

                    if (cita.nombreMedico.isNotBlank()) {
                        DivisorCampo()
                        DetalleCampo(
                            icono = Icons.Outlined.Person,
                            etiqueta = "Médico",
                            valor = cita.nombreMedico
                        )
                    }

                    if (cita.personaId != null) {
                        DivisorCampo()
                        DetalleCampo(
                            icono = Icons.Outlined.Group,
                            etiqueta = "Paciente",
                            valor = PersonaRepository.obtenerPorId(cita.personaId)?.nombreCompleto
                                ?: "Familiar"
                        )
                    }
                }

                if (cita.motivo.isNotBlank() || cita.notas.isNotBlank()) {

                    Spacer(Modifier.height(16.dp))

                    TarjetaDetalle {

                        if (cita.motivo.isNotBlank()) {
                            DetalleCampo(
                                icono = Icons.Outlined.Notes,
                                etiqueta = "Motivo",
                                valor = cita.motivo
                            )
                        }

                        if (cita.motivo.isNotBlank() && cita.notas.isNotBlank()) {
                            DivisorCampo()
                        }

                        if (cita.notas.isNotBlank()) {
                            DetalleCampo(
                                icono = Icons.Outlined.EditNote,
                                etiqueta = "Notas",
                                valor = cita.notas
                            )
                        }
                    }
                }

                if (cita.soporteUri != null) {

                    Spacer(Modifier.height(16.dp))

                    TarjetaDetalle {
                        DetalleCampo(
                            icono = Icons.Outlined.AttachFile,
                            etiqueta = "Soporte adjunto",
                            valor = "Archivo adjunto al registrar la cita"
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                val familiar = SesionRepository.familiarActivo

                if (familiar != null) {

                    AvisoSoloLectura(
                        mensaje = "Esta cita pertenece al perfil de ${familiar.descripcionConParentesco()}. Solo puedes consultarla."
                    )

                } else if (esHistorico) {

                    AvisoSoloLectura(
                        mensaje = "Esta cita ya ocurrió, por lo que queda como registro del historial y no se puede editar ni eliminar."
                    )

                } else {

                    OutlinedButton(
                        onClick = onEditar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Editar cita")
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { mostrarDialogoEliminar = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar cita")
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            if (mostrarDialogoEliminar) {

                AlertDialog(
                    onDismissRequest = { mostrarDialogoEliminar = false },

                    title = {
                        Text("Eliminar cita")
                    },

                    text = {
                        Text("¿Seguro que deseas eliminar esta cita? Esta acción no se puede deshacer.")
                    },

                    confirmButton = {
                        TextButton(
                            onClick = {
                                CitaRepository.eliminar(citaId)
                                mostrarDialogoEliminar = false
                                onEliminar()
                            }
                        ) {
                            Text("Eliminar", color = ErrorRed)
                        }
                    },

                    dismissButton = {
                        TextButton(
                            onClick = { mostrarDialogoEliminar = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

// ENCABEZADO
@Composable
private fun EncabezadoCita(cita: Cita) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(16.dp),
            color = Blue100.copy(alpha = 0.4f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.MedicalServices,
                    contentDescription = null,
                    tint = Blue700,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column {

            Text(
                text = if (cita.especialidad.isNotBlank()) cita.especialidad else cita.tipo.titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "${formatearFechaCita(cita.fechaMillis)} · ${formatearHoraCita(cita.hora, cita.minuto)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}




@Composable
private fun TarjetaDetalle(contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        content = contenido
    )
}

@Composable
private fun DivisorCampo() {
    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
}

@Composable
private fun DetalleCampo(
    icono: ImageVector,
    etiqueta: String,
    valor: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {

        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Blue500,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )

        Spacer(Modifier.width(14.dp))

        Column {

            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}



@Composable
private fun AvisoSoloLectura(mensaje: String) {

    Surface(
        color = Blue100.copy(alpha = 0.25f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Blue700,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// FORMATO DE FECHA / HORA
private fun obtenerHoyUtcMillisCita(): Long {

    val hoyLocal = Calendar.getInstance()

    val hoyUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    hoyUtc.set(
        hoyLocal.get(Calendar.YEAR),
        hoyLocal.get(Calendar.MONTH),
        hoyLocal.get(Calendar.DAY_OF_MONTH),
        0,
        0,
        0
    )
    hoyUtc.set(Calendar.MILLISECOND, 0)

    return hoyUtc.timeInMillis
}

private fun formatearFechaCita(millis: Long): String {

    val formatter = SimpleDateFormat("EEEE d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    formatter.timeZone = TimeZone.getTimeZone("UTC")

    val texto = formatter.format(Date(millis))

    return texto.replaceFirstChar { primero -> primero.uppercase() }
}

private fun formatearHoraCita(hora: Int, minuto: Int): String {

    val amPm = if (hora < 12) "AM" else "PM"

    val hora12 = when {
        hora == 0 -> 12
        hora > 12 -> hora - 12
        else -> hora
    }

    return String.format(Locale.getDefault(), "%d:%02d %s", hora12, minuto, amPm)
}