package com.icm2630.proyecto.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.icm2630.proyecto.data.model.Medicamento
import com.icm2630.proyecto.data.repository.MedicamentoRepository
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

// PANTALLA DE DETALLE DE UN MEDICAMENTO
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMedicamentoScreen(
    medicamentoId: String,
    onBack: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {


    val medicamento = MedicamentoRepository.obtenerPorId(medicamentoId)

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
                        text = "Detalle del medicamento",
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

        if (medicamento == null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Este medicamento ya no está disponible.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        } else {


            val hoyMillis = obtenerHoyUtcMillisMedicamento()
            val esHistorico = !medicamento.tratamientoPermanente &&
                    medicamento.fechaFinMillis != null &&
                    medicamento.fechaFinMillis < hoyMillis

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {

                EncabezadoMedicamento(medicamento)

                Spacer(Modifier.height(20.dp))

                TarjetaDetalleMedicamento {

                    DetalleCampoMedicamento(
                        icono = Icons.Outlined.Medication,
                        etiqueta = "Presentación",
                        valor = medicamento.forma.titulo
                    )
                    DivisorCampoMedicamento()

                    DetalleCampoMedicamento(
                        icono = Icons.Outlined.Science,
                        etiqueta = "Dosis",
                        valor = "${medicamento.dosis} ${medicamento.unidad}"
                    )

                    if (medicamento.cantidadPorToma.isNotBlank()) {
                        DivisorCampoMedicamento()
                        DetalleCampoMedicamento(
                            icono = Icons.Outlined.Medication,
                            etiqueta = "Cantidad por toma",
                            valor = medicamento.cantidadPorToma
                        )
                    }

                    DivisorCampoMedicamento()

                    DetalleCampoMedicamento(
                        icono = Icons.Outlined.Schedule,
                        etiqueta = "Horarios",
                        valor = if (medicamento.horarios.isNotEmpty()) {
                            medicamento.horarios.joinToString(" · ")
                        } else {
                            "Sin horarios definidos"
                        }
                    )

                    if (medicamento.personaId != null) {
                        DivisorCampoMedicamento()
                        DetalleCampoMedicamento(
                            icono = Icons.Outlined.Group,
                            etiqueta = "Paciente",
                            valor = PersonaRepository.obtenerPorId(medicamento.personaId)?.nombreCompleto
                                ?: "Familiar"
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                TarjetaDetalleMedicamento {

                    DetalleCampoMedicamento(
                        icono = Icons.Outlined.CalendarMonth,
                        etiqueta = "Inicio del tratamiento",
                        valor = formatearFechaMedicamento(medicamento.fechaInicioMillis)
                    )

                    DivisorCampoMedicamento()

                    DetalleCampoMedicamento(
                        icono = Icons.Outlined.EventAvailable,
                        etiqueta = "Duración",
                        valor = if (medicamento.tratamientoPermanente) {
                            "Tratamiento permanente"
                        } else {
                            "Hasta el ${formatearFechaMedicamento(medicamento.fechaFinMillis)}"
                        }
                    )
                }

                if (medicamento.indicaciones.isNotBlank()) {

                    Spacer(Modifier.height(16.dp))

                    TarjetaDetalleMedicamento {
                        DetalleCampoMedicamento(
                            icono = Icons.Outlined.EditNote,
                            etiqueta = "Indicaciones",
                            valor = medicamento.indicaciones
                        )
                    }
                }

                if (medicamento.ordenMedicaUri != null) {

                    Spacer(Modifier.height(16.dp))

                    TarjetaDetalleMedicamento {
                        Column(modifier = Modifier.padding(vertical = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AttachFile,
                                    contentDescription = null,
                                    tint = Blue500,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Orden médica",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = medicamento.nombreOrdenMedica ?: "Archivo adjunto",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            val context = LocalContext.current
                            val isImage = remember(medicamento.ordenMedicaUri) {
                                val uriStr = medicamento.ordenMedicaUri.lowercase()
                                uriStr.endsWith(".jpg") || uriStr.endsWith(".jpeg") ||
                                        uriStr.endsWith(".png") || uriStr.contains("image") ||
                                        uriStr.startsWith("file://")
                            }

                            if (isImage) {
                                AsyncImage(
                                    model = medicamento.ordenMedicaUri,
                                    contentDescription = "Vista previa de la orden médica",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { abrirArchivo(context, medicamento.ordenMedicaUri) },
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(12.dp))
                            }

                            OutlinedButton(
                                onClick = { abrirArchivo(context, medicamento.ordenMedicaUri) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isImage) Icons.Outlined.Visibility else Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(if (isImage) "Ver imagen completa" else "Abrir orden médica / PDF")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                val familiar = SesionRepository.familiarActivo

                if (familiar != null) {

                    AvisoSoloLecturaMedicamento(
                        mensaje = "Este medicamento pertenece al perfil de ${familiar.descripcionConParentesco()}. Solo puedes consultarlo."
                    )

                } else if (esHistorico) {

                    AvisoSoloLecturaMedicamento(
                        mensaje = "Este tratamiento ya finalizó, por lo que queda como registro del historial y no se puede editar ni eliminar."
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
                        Text("Editar medicamento")
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
                        Text("Eliminar medicamento")
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            if (mostrarDialogoEliminar) {

                AlertDialog(
                    onDismissRequest = { mostrarDialogoEliminar = false },

                    title = {
                        Text("Eliminar medicamento")
                    },

                    text = {
                        Text("¿Seguro que deseas eliminar este medicamento? Esta acción no se puede deshacer.")
                    },

                    confirmButton = {
                        TextButton(
                            onClick = {
                                MedicamentoRepository.eliminar(medicamentoId)
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
private fun EncabezadoMedicamento(medicamento: Medicamento) {

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
                    imageVector = Icons.Outlined.Medication,
                    contentDescription = null,
                    tint = Blue700,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column {

            Text(
                text = medicamento.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "${medicamento.dosis}${medicamento.unidad} · ${medicamento.forma.titulo}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun TarjetaDetalleMedicamento(contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        content = contenido
    )
}

@Composable
private fun DivisorCampoMedicamento() {
    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
}

@Composable
private fun DetalleCampoMedicamento(
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
private fun AvisoSoloLecturaMedicamento(mensaje: String) {

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

private fun abrirArchivo(context: Context, uriString: String) {
    try {
        val uri = Uri.parse(uriString)
        val mimeType = context.contentResolver.getType(uri) ?: when {
            uriString.lowercase().endsWith(".pdf") -> "application/pdf"
            uriString.lowercase().endsWith(".png") -> "image/png"
            uriString.lowercase().endsWith(".jpg") || uriString.lowercase().endsWith(".jpeg") -> "image/jpeg"
            else -> "*/*"
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Abrir orden médica"))
    } catch (e: Exception) {
        Toast.makeText(context, "No se encontró una aplicación para abrir este archivo", Toast.LENGTH_SHORT).show()
    }
}

// FORMATO DE FECHA
private fun obtenerHoyUtcMillisMedicamento(): Long {

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

private fun formatearFechaMedicamento(millis: Long?): String {

    if (millis == null) {
        return ""
    }

    val formatter = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    formatter.timeZone = TimeZone.getTimeZone("UTC")

    return formatter.format(Date(millis))
}