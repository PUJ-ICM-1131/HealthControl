package com.icm2630.proyecto.ui.screens

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
import androidx.compose.runtime.saveable.rememberSaveable
import com.icm2630.proyecto.data.model.FechasHistorial
import com.icm2630.proyecto.ui.util.rememberHoyUtcMillis
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
import androidx.compose.ui.text.style.TextAlign
import com.icm2630.proyecto.ui.components.FiltroListadoChip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.Cita
import com.icm2630.proyecto.data.model.Medicamento
import com.icm2630.proyecto.data.model.TipoCita
import com.icm2630.proyecto.data.repository.CitaRepository
import com.icm2630.proyecto.data.repository.MedicamentoRepository
import com.icm2630.proyecto.data.repository.VinculacionRepository
import com.icm2630.proyecto.data.repository.SesionRepository
import com.icm2630.proyecto.ui.components.BannerFamiliar
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
    val textoBusqueda: String,
    val cita: Cita? = null
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
    var busqueda by rememberSaveable { mutableStateOf("") }
    var filtroSeleccionado by rememberSaveable { mutableStateOf(FiltroHistorial.TODOS) }
    var filtroDesde by rememberSaveable { mutableStateOf<Long?>(null) }
    var filtroHasta by rememberSaveable { mutableStateOf<Long?>(null) }
    val rangoFechas = if (filtroDesde == null && filtroHasta == null) null
        else RangoFechas(filtroDesde, filtroHasta)
    var mostrarDialogoFiltros by remember { mutableStateOf(false) }


    val hoyMillis = rememberHoyUtcMillis()
    val historialCompleto = construirHistorial(hoyMillis)

    val historialFiltrado = historialCompleto.filter { item ->

        val coincideTipo = when (filtroSeleccionado) {
            FiltroHistorial.TODOS -> true
            FiltroHistorial.CITAS -> item.tipo == TipoRegistroHistorial.CITA
            FiltroHistorial.MEDICAMENTOS -> item.tipo == TipoRegistroHistorial.MEDICAMENTO
        }

        val coincideBusqueda = busqueda.isBlank() ||
                item.textoBusqueda.contains(busqueda.trim().lowercase())

        val coincideFecha = rangoFechas?.let { rango ->
            FechasHistorial.enRango(item.fechaMillis, rango.desdeMillis, rango.hastaMillis)
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
                    text = "Historial clínico",
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
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

            SesionRepository.familiarActivo?.let { familiar ->
                BannerFamiliar(persona = familiar)
                Spacer(Modifier.height(20.dp))
            }

            //Buscador:
            TextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = {
                    Text("Buscar en el historial", maxLines = 1, color = PlaceholderGris)
                },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, null, tint = Blue500)
                },
                trailingIcon = if (busqueda.isNotEmpty()) ({
                    IconButton(onClick = { busqueda = "" }) { Icon(Icons.Outlined.Close, "Limpiar búsqueda", tint = Blue500) }
                }) else null,
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

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DateRange, null, tint = Blue500, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        etiquetaRango(rangoFechas),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = Blue700
                    )
                    TextButton(onClick = { filtroDesde = null; filtroHasta = null }) {
                        Text("Quitar", color = ErrorRed)
                    }
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
                filtroDesde = nuevoRango?.desdeMillis
                filtroHasta = nuevoRango?.hastaMillis
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
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Intenta con otros filtros o términos de búsqueda",
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
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
                onVerMapa = { cita -> onVerDetalle(Routes.MapaCita(cita.id, SesionRepository.uidConsultado)) },
                onClick = {
                    val destino = if (item.tipo == TipoRegistroHistorial.CITA) {
                        Routes.DetalleCita(citaId = item.id, titularUid = SesionRepository.uidConsultado)
                    } else {
                        Routes.DetalleMedicamento(medicamentoId = item.id, titularUid = SesionRepository.uidConsultado)
                    }
                    onVerDetalle(destino)
                }
            )
        }
    }
}

//Tarjeta de un registro (ahora clickable: navega al detalle de la cita o del medicamento)
@Composable
private fun RegistroHistorialTarjeta(item: RegistroHistorial, onClick: () -> Unit, onVerMapa: (Cita) -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(48.dp)
                    .background(Blue50, RoundedCornerShape(12.dp)).padding(vertical = 8.dp)
            ) {
                Text(item.dia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Blue700)
                Text(item.mes, style = MaterialTheme.typography.labelSmall, color = Blue500)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(item.icono, null, tint = item.colorIcono, modifier = Modifier.size(20.dp))
                    Text(
                        item.titulo,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.heightIn(min = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Outlined.Person, null, tint = item.colorPaciente, modifier = Modifier.size(14.dp))
                        Text(item.paciente, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Surface(color = item.estadoColor.copy(alpha = 0.10f), shape = RoundedCornerShape(6.dp)) {
                        Text(item.estado, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = item.estadoColor)
                    }
                }
                item.cita?.let { cita ->
                    com.icm2630.proyecto.ui.components.VerLugarCitaButton(cita,
                        onClick = { onVerMapa(cita) }, modifier = Modifier.align(Alignment.End))
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Outlined.KeyboardArrowRight, "Ver detalle", tint = TextSecondary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun FiltroChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FiltroListadoChip(texto, seleccionado, onClick)
}

@Composable
private fun BotonFiltros(activo: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
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
    var desdeMillis by rememberSaveable { mutableStateOf(rangoActual?.desdeMillis) }
    var hastaMillis by rememberSaveable { mutableStateOf(rangoActual?.hastaMillis) }
    var mostrarPickerDesde by remember { mutableStateOf(false) }
    var mostrarPickerHasta by remember { mutableStateOf(false) }

    val hoyMillis = rememberHoyUtcMillis()
    val rangoValido = FechasHistorial.rangoValido(desdeMillis, hastaMillis)

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Filtrar por fecha")
        },

        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                        desdeMillis = FechasHistorial.desdeUltimosDias(hoyMillis, 7)
                        hastaMillis = hoyMillis
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Últimos 7 días")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        desdeMillis = FechasHistorial.desdeUltimosDias(hoyMillis, 30)
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
                    text = "Fecha de la cita o inicio del tratamiento",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    OutlinedButton(
                        onClick = { mostrarPickerDesde = true },
                        modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 48.dp)
                    ) {
                        Text(desdeMillis?.let { formatearFechaCortaHistorial(it) } ?: "Desde")
                    }

                    OutlinedButton(
                        onClick = { mostrarPickerHasta = true },
                        modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 48.dp)
                    ) {
                        Text(hastaMillis?.let { formatearFechaCortaHistorial(it) } ?: "Hasta")
                    }
                }
                if (!rangoValido) {
                    Text("La fecha Desde debe ser anterior o igual a Hasta.",
                        color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
                },
                enabled = rangoValido
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



private fun construirHistorial(hoyMillis: Long): List<RegistroHistorial> {

    val citas = CitaRepository.obtenerTodas().map { cita ->
        construirRegistroCita(cita, hoyMillis)
    }

    val medicamentos = MedicamentoRepository.obtenerTodos().map { medicamento ->
        construirRegistroMedicamento(medicamento, hoyMillis)
    }

    return (citas + medicamentos).sortedByDescending { it.fechaMillis }
}

private fun construirRegistroCita(cita: Cita, hoyMillis: Long): RegistroHistorial {

    val (estado, estadoColor) = when (FechasHistorial.estadoCita(cita.fechaMillis, hoyMillis)) {
        FechasHistorial.EstadoCita.PROGRAMADA -> "Programada" to Blue500
        FechasHistorial.EstadoCita.HOY -> "Hoy" to Color(0xFFF59E0B)
        FechasHistorial.EstadoCita.PASADA -> "Fecha pasada" to TextSecondary
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
        cita = cita,
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

    val (estado, estadoColor) = when (FechasHistorial.estadoTratamiento(medicamento, hoyMillis)) {
        FechasHistorial.EstadoTratamiento.PROGRAMADO -> "Programado" to Blue500
        FechasHistorial.EstadoTratamiento.VIGENTE -> "En tratamiento" to SuccessGreen
        FechasHistorial.EstadoTratamiento.FINALIZADO -> "Finalizado" to TextSecondary
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
        // En el perfil de un familiar, lo "propio" es de esa persona.
        return SesionRepository.familiarActivo?.nombre?.substringBefore(" ") ?: "Yo"
    }

    val persona = VinculacionRepository.obtenerPorId(personaId)

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

    val indice = Math.floorMod(personaId.hashCode(), paleta.size)

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