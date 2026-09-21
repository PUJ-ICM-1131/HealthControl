package com.icm2630.proyecto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.Cita
import com.icm2630.proyecto.data.model.Medicamento
import com.icm2630.proyecto.data.model.TipoCita
import com.icm2630.proyecto.data.repository.CitaRepository
import com.icm2630.proyecto.data.repository.MedicamentoRepository
import com.icm2630.proyecto.data.repository.PersonaRepository
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val Fondo = Color(0xFFF8FAFF)
private val CampoBg = Color(0xFFEFF5FC)
private val PlaceholderGris = Color(0xFF9CA9BC)

//Tipo de registro por el que se puede filtrar el historial:
private enum class FiltroHistorial(val etiqueta: String) {
    TODOS("Todo"),
    CITAS("Citas"),
    MEDICAMENTOS("Medicamentos")
}

private enum class TipoRegistroHistorial {
    CITA,
    MEDICAMENTO
}

private data class RegistroHistorial(
    val id: String,
    val tipo: TipoRegistroHistorial,
    val fechaMillis: Long,
    val dia: String,
    val mes: String,
    val icono: ImageVector,
    val colorIcono: Color,
    val titulo: String,
    val colorPaciente: Color,
    val paciente: String,
    val estado: String,
    val estadoColor: Color,
    val textoBusqueda: String
)

private data class GrupoHistorial(
    val mesAnio: String,
    val items: List<RegistroHistorial>
)


private data class RangoFechas(
    val desdeMillis: Long?,
    val hastaMillis: Long?
)

private val MESES = arrayOf(
    "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO",
    "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"
)

private val MESES_ABREV = arrayOf(
    "ENE", "FEB", "MAR", "ABR", "MAY", "JUN",
    "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"
)


@Composable
fun HistoryScreen(
    onNavigate: (Routes) -> Unit = {},
    onVerDetalle: (Routes) -> Unit = {}
) {
    var busqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf(FiltroHistorial.TODOS) }
    var rangoFechas by remember { mutableStateOf<RangoFechas?>(null) }
    var mostrarDialogoFiltros by remember { mutableStateOf(false) }


    val historialCompleto = construirHistorial()

    val historialFiltrado = historialCompleto.filter { item ->

        val coincideTipo = when (filtroSeleccionado) {
            FiltroHistorial.TODOS -> true
            FiltroHistorial.CITAS -> item.tipo == TipoRegistroHistorial.CITA
            FiltroHistorial.MEDICAMENTOS -> item.tipo == TipoRegistroHistorial.MEDICAMENTO
        }

        val coincideBusqueda = busqueda.isBlank() ||
                item.textoBusqueda.contains(busqueda.trim().lowercase())

        val coincideFecha = rangoFechas?.let { rango ->
            (rango.desdeMillis == null || item.fechaMillis >= rango.desdeMillis) &&
                    (rango.hastaMillis == null || item.fechaMillis <= rango.hastaMillis)
        } ?: true

        coincideTipo && coincideBusqueda && coincideFecha
    }

    val grupos = agruparPorMes(historialFiltrado)

    Scaffold(
        bottomBar = {
            HealthBottomNavigation(
                currentRoute = Routes.Historial,
                onNavigate = onNavigate
            )
        },
        containerColor = Fondo
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            //Cabeza de pagina
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial Clínico",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Blue700
                )

                BotonFiltros(
                    activo = rangoFechas != null,
                    onClick = { mostrarDialogoFiltros = true }
                )
            }

            Spacer(Modifier.height(20.dp))

            //Buscador:
            TextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(50),
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = {
                    Text("Buscar por medicamento, doctor o cita...", color = PlaceholderGris)
                },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, null, tint = Blue500)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CampoBg,
                    unfocusedContainerColor = CampoBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Blue700
                )
            )

            Spacer(Modifier.height(20.dp))

            //Filtro por tipo:
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FiltroHistorial.entries.forEach { filtro ->
                    FiltroChip(
                        texto = filtro.etiqueta,
                        seleccionado = filtro == filtroSeleccionado,
                        onClick = { filtroSeleccionado = filtro }
                    )
                }
            }

            //Chip informativo del rango de fecha activo (si aplica)
            if (rangoFechas != null) {

                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = Blue500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = etiquetaRango(rangoFechas!!),
                        style = MaterialTheme.typography.labelMedium,
                        color = Blue700
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Quitar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ErrorRed,
                        modifier = Modifier.clickable { rangoFechas = null }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            //Lista por mes
            if (grupos.isEmpty()) {

                EstadoVacioHistorial()

            } else {

                grupos.forEachIndexed { index, grupo ->
                    GrupoHistorialSeccion(
                        grupo = grupo,
                        onVerDetalle = onVerDetalle
                    )
                    if (index != grupos.lastIndex) {
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (mostrarDialogoFiltros) {
        FiltroFechaDialog(
            rangoActual = rangoFechas,
            onDismiss = { mostrarDialogoFiltros = false },
            onAplicar = { nuevoRango ->
                rangoFechas = nuevoRango
                mostrarDialogoFiltros = false
            }
        )
    }
}

@Composable
private fun EstadoVacioHistorial() {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = Blue100,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "No se encontraron registros",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Intenta con otros filtros o términos de búsqueda",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

//Titulo del mes con sus tarjetas
@Composable
private fun GrupoHistorialSeccion(
    grupo: GrupoHistorial,
    onVerDetalle: (Routes) -> Unit
) {
    Text(
        text = grupo.mesAnio,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = TextSecondary
    )

    Spacer(Modifier.height(12.dp))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        grupo.items.forEach { item ->
            RegistroHistorialTarjeta(
                item = item,
                onClick = {
                    val destino = if (item.tipo == TipoRegistroHistorial.CITA) {
                        Routes.DetalleCita(citaId = item.id)
                    } else {
                        Routes.DetalleMedicamento(medicamentoId = item.id)
                    }
                    onVerDetalle(destino)
                }
            )
        }
    }
}

//Tarjeta de un registro (ahora clickable: navega al detalle de la cita o del medicamento)
@Composable
private fun RegistroHistorialTarjeta(
    item: RegistroHistorial,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(44.dp)
                    .background(Blue100.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(vertical = 6.dp)
            ) {
                Text(item.dia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Blue700)
                Text(item.mes, style = MaterialTheme.typography.labelSmall, color = Blue500)
            }

            Spacer(Modifier.width(12.dp))

            Surface(
                color = item.colorIcono.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(item.icono, null, tint = item.colorIcono, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, null, tint = item.colorPaciente, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(item.paciente, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(" • ", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        text = item.estado,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = item.estadoColor
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = "Ver detalle",
                tint = TextSecondary,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
private fun FiltroChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (seleccionado) Blue700 else Color.White,
        shape = RoundedCornerShape(50),
        border = if (!seleccionado) BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
        onClick = onClick
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (seleccionado) Color.White else TextSecondary,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
        )
    }
}


@Composable
private fun BotonFiltros(activo: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = Blue100, spotColor = Blue100)
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = "Opciones de filtro",
                tint = Blue700
            )
        }
        if (activo) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(8.dp)
                    .background(Color(0xFFEF4444), CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltroFechaDialog(
    rangoActual: RangoFechas?,
    onDismiss: () -> Unit,
    onAplicar: (RangoFechas?) -> Unit
) {
    var desdeMillis by remember { mutableStateOf(rangoActual?.desdeMillis) }
    var hastaMillis by remember { mutableStateOf(rangoActual?.hastaMillis) }
    var mostrarPickerDesde by remember { mutableStateOf(false) }
    var mostrarPickerHasta by remember { mutableStateOf(false) }

    val hoyMillis = obtenerHoyUtcMillisHistorial()
    val unDiaMillis = 24L * 60L * 60L * 1000L

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Filtrar por fecha")
        },

        text = {
            Column {

                Text(
                    text = "Accesos rápidos",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { desdeMillis = null; hastaMillis = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Todo el historial")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        desdeMillis = hoyMillis - 7 * unDiaMillis
                        hastaMillis = hoyMillis
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Últimos 7 días")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        desdeMillis = hoyMillis - 30 * unDiaMillis
                        hastaMillis = hoyMillis
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Últimos 30 días")
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE5E7EB))
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Rango personalizado",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    OutlinedButton(
                        onClick = { mostrarPickerDesde = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(desdeMillis?.let { formatearFechaCortaHistorial(it) } ?: "Desde")
                    }

                    OutlinedButton(
                        onClick = { mostrarPickerHasta = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(hastaMillis?.let { formatearFechaCortaHistorial(it) } ?: "Hasta")
                    }
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    val rango = if (desdeMillis == null && hastaMillis == null) {
                        null
                    } else {
                        RangoFechas(desdeMillis, hastaMillis)
                    }
                    onAplicar(rango)
                }
            ) {
                Text("Aplicar")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (mostrarPickerDesde) {
        HistorialDatePickerDialog(
            initialDateMillis = desdeMillis,
            onDismiss = { mostrarPickerDesde = false },
            onDateSelected = { millis ->
                desdeMillis = millis
                mostrarPickerDesde = false
            }
        )
    }

    if (mostrarPickerHasta) {
        HistorialDatePickerDialog(
            initialDateMillis = hastaMillis,
            onDismiss = { mostrarPickerHasta = false },
            onDateSelected = { millis ->
                hastaMillis = millis
                mostrarPickerHasta = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistorialDatePickerDialog(
    initialDateMillis: Long?,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,

        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(millis)
                    }
                }
            ) {
                Text("Aceptar")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun etiquetaRango(rango: RangoFechas): String {

    val desde = rango.desdeMillis
    val hasta = rango.hastaMillis

    return when {
        desde != null && hasta != null ->
            "${formatearFechaCortaHistorial(desde)} - ${formatearFechaCortaHistorial(hasta)}"

        desde != null ->
            "Desde ${formatearFechaCortaHistorial(desde)}"

        hasta != null ->
            "Hasta ${formatearFechaCortaHistorial(hasta)}"

        else ->
            "Filtro de fecha activo"
    }
}



private fun construirHistorial(): List<RegistroHistorial> {

    val hoyMillis = obtenerHoyUtcMillisHistorial()

    val citas = CitaRepository.obtenerTodas().map { cita ->
        construirRegistroCita(cita, hoyMillis)
    }

    val medicamentos = MedicamentoRepository.obtenerTodos().map { medicamento ->
        construirRegistroMedicamento(medicamento, hoyMillis)
    }

    return (citas + medicamentos).sortedByDescending { it.fechaMillis }
}

private fun construirRegistroCita(cita: Cita, hoyMillis: Long): RegistroHistorial {

    val (estado, estadoColor) = when {
        cita.fechaMillis > hoyMillis -> "Programada" to Blue500
        cita.fechaMillis == hoyMillis -> "Hoy" to Color(0xFFF59E0B)
        else -> "Completada" to SuccessGreen
    }

    val titulo = buildString {
        if (cita.nombreMedico.isNotBlank()) {
            append(cita.nombreMedico)
        } else {
            append(cita.tipo.titulo)
        }
        if (cita.especialidad.isNotBlank()) {
            append(" — ${cita.especialidad}")
        }
    }

    val paciente = etiquetaPacienteHistorial(cita.personaId)

    return RegistroHistorial(
        id = cita.id,
        tipo = TipoRegistroHistorial.CITA,
        fechaMillis = cita.fechaMillis,
        dia = formatearDiaHistorial(cita.fechaMillis),
        mes = formatearMesAbreviadoHistorial(cita.fechaMillis),
        icono = if (cita.tipo == TipoCita.EXAMENES) Icons.Outlined.Science else Icons.Outlined.MedicalServices,
        colorIcono = if (cita.tipo == TipoCita.EXAMENES) ErrorRed else Color(0xFFF59E0B),
        titulo = titulo,
        colorPaciente = colorPacienteHistorial(cita.personaId),
        paciente = paciente,
        estado = estado,
        estadoColor = estadoColor,
        textoBusqueda = listOf(
            cita.tipo.titulo,
            cita.especialidad,
            cita.motivo,
            cita.institucion,
            cita.nombreMedico,
            paciente
        ).joinToString(" ").lowercase()
    )
}

private fun construirRegistroMedicamento(medicamento: Medicamento, hoyMillis: Long): RegistroHistorial {

    val (estado, estadoColor) = if (medicamento.tratamientoPermanente) {
        "En tratamiento" to SuccessGreen
    } else {
        val fin = medicamento.fechaFinMillis
        if (fin != null && fin < hoyMillis) {
            "Finalizado" to TextSecondary
        } else {
            "En tratamiento" to SuccessGreen
        }
    }

    val paciente = etiquetaPacienteHistorial(medicamento.personaId)

    return RegistroHistorial(
        id = medicamento.id,
        tipo = TipoRegistroHistorial.MEDICAMENTO,
        fechaMillis = medicamento.fechaInicioMillis,
        dia = formatearDiaHistorial(medicamento.fechaInicioMillis),
        mes = formatearMesAbreviadoHistorial(medicamento.fechaInicioMillis),
        icono = Icons.Outlined.Medication,
        colorIcono = Blue500,
        titulo = medicamento.nombre,
        colorPaciente = colorPacienteHistorial(medicamento.personaId),
        paciente = paciente,
        estado = estado,
        estadoColor = estadoColor,
        textoBusqueda = listOf(
            medicamento.nombre,
            medicamento.indicaciones,
            paciente
        ).joinToString(" ").lowercase()
    )
}

private fun etiquetaPacienteHistorial(personaId: String?): String {

    if (personaId == null) {
        return "Yo"
    }

    val persona = PersonaRepository.obtenerPorId(personaId)

    return if (persona != null) {
        "Para ${persona.nombre}"
    } else {
        "Familiar"
    }
}


private fun colorPacienteHistorial(personaId: String?): Color {

    if (personaId == null) {
        return Color(0xFF10B981)
    }

    val paleta = listOf(
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFFF59E0B),
        Color(0xFF06B6D4)
    )

    val indice = kotlin.math.abs(personaId.hashCode()) % paleta.size

    return paleta[indice]
}




private fun agruparPorMes(items: List<RegistroHistorial>): List<GrupoHistorial> {

    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

    return items
        .groupBy { item ->
            calendar.timeInMillis = item.fechaMillis
            calendar.get(Calendar.YEAR) * 100 + calendar.get(Calendar.MONTH)
        }
        .entries
        .sortedByDescending { it.key }
        .map { (clave, itemsDelMes) ->

            val mes = clave % 100
            val anio = clave / 100

            GrupoHistorial(
                mesAnio = "${MESES[mes]} $anio",
                items = itemsDelMes
            )
        }
}



private fun obtenerHoyUtcMillisHistorial(): Long {

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

private fun formatearDiaHistorial(millis: Long): String {

    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = millis

    return String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.DAY_OF_MONTH))
}

private fun formatearMesAbreviadoHistorial(millis: Long): String {

    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = millis

    return MESES_ABREV[calendar.get(Calendar.MONTH)]
}

private fun formatearFechaCortaHistorial(millis: Long): String {

    val formatter = SimpleDateFormat("d MMM", Locale("es", "ES"))
    formatter.timeZone = TimeZone.getTimeZone("UTC")

    return formatter.format(Date(millis))
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistoryScreenPreview() {
    HealthControlTheme {
        HistoryScreen()
    }
}