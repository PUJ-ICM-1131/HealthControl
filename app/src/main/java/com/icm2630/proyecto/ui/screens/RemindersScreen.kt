package com.icm2630.proyecto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icm2630.proyecto.data.model.ModalidadCita
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

//Filtro por tipo de recordatorio:
private enum class FiltroTipo(val etiqueta: String) {
    TODOS("Todos"),
    MEDICAMENTOS("Medicamentos"),
    CITAS("Citas"),
    EXAMENES("Exámenes")
}



private data class PacienteFiltro(
    val personaId: String?,
    val nombre: String,
    val icon: ImageVector? = null,
    val inicial: String? = null,
    val pendientes: Int = 0
)




private enum class TipoRecordatorio {
    CITA,
    MEDICAMENTO
}

private data class Recordatorio(
    val tipo: TipoRecordatorio,
    val id: String,
    val tipoCita: TipoCita?,
    val horaOrden: Int,
    val minutoOrden: Int,
    val horaTexto: String,
    val icono: ImageVector,
    val titulo: String,
    val subtitulo: String
)

private data class GrupoRecordatorios(
    val fechaMillis: Long,
    val etiqueta: String,
    val esHoy: Boolean,
    val items: List<Recordatorio>
)


@Composable
fun RemindersScreen(
    onNavigate: (Routes) -> Unit = {},
    onVerDetalle: (Routes) -> Unit = {}
) {
    var filtroSeleccionado by remember { mutableStateOf(FiltroTipo.TODOS) }


    var pacienteSeleccionado by remember { mutableStateOf<String?>(null) }


    val pacientes = construirPacientes()
    val grupos = construirRecordatorios(
        filtro = filtroSeleccionado,
        personaId = pacienteSeleccionado
    )

    Scaffold(
        bottomBar = {
            HealthBottomNavigation(
                currentRoute = Routes.Recordatorios,
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
            //encabezado de pagina:
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pendientes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Blue700
                )

                BotonAgregar(onClick = { onNavigate(Routes.Registrar) })
            }

            Spacer(Modifier.height(20.dp))

            //Diseño: Filtro por tipo de recordatorio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FiltroTipo.entries.forEach { filtro ->
                    FiltroChip(
                        texto = filtro.etiqueta,
                        seleccionado = filtro == filtroSeleccionado,
                        onClick = { filtroSeleccionado = filtro }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            //Filtro por paciente
            Text(
                text = "Filtrar por paciente",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                pacientes.forEach { paciente ->
                    PacienteChip(
                        paciente = paciente,
                        seleccionado = paciente.personaId == pacienteSeleccionado,
                        onClick = { pacienteSeleccionado = paciente.personaId }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (grupos.isEmpty()) {
                EstadoVacio()
            } else {
                grupos.forEachIndexed { index, grupo ->
                    GrupoRecordatoriosSeccion(
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
}

@Composable
private fun EstadoVacio() {
    TarjetaBlanca {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.StickyNote2,
                contentDescription = null,
                tint = Blue100,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "No tienes pendientes por ahora",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tus próximas citas y medicamentos aparecerán aquí",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun GrupoRecordatoriosSeccion(
    grupo: GrupoRecordatorios,
    onVerDetalle: (Routes) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = grupo.etiqueta,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Blue700
        )
        if (grupo.esHoy) {
            Text(
                text = "Hoy",
                style = MaterialTheme.typography.labelLarge,
                color = Blue500
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    TarjetaBlanca {
        grupo.items.forEachIndexed { index, item ->
            RecordatorioFila(
                item = item,
                onClick = {
                    val destino = if (item.tipo == TipoRecordatorio.CITA) {
                        Routes.DetalleCita(citaId = item.id)
                    } else {
                        Routes.DetalleMedicamento(medicamentoId = item.id)
                    }
                    onVerDetalle(destino)
                }
            )
            if (index != grupo.items.lastIndex) {
                DivisorFila()
            }
        }
    }
}

@Composable
private fun TarjetaBlanca(contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        content = contenido
    )
}

@Composable
private fun DivisorFila() {
    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
}

@Composable
private fun RecordatorioFila(
    item: Recordatorio,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.horaTexto,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Blue700,
            modifier = Modifier.widthIn(min = 52.dp)
        )

        Surface(
            color = Blue100.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(item.icono, null, tint = Blue500, modifier = Modifier.size(20.dp))
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
            Text(
                text = item.subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Outlined.KeyboardArrowRight,
            contentDescription = "Ver detalle",
            tint = TextSecondary
        )
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
private fun PacienteChip(
    paciente: PacienteFiltro,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        BadgedBox(
            badge = {
                if (paciente.pendientes > 0) {
                    Badge(containerColor = Color(0xFFEF4444)) {
                        Text(paciente.pendientes.toString(), color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = if (seleccionado) Color.White else Blue100.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .then(
                        if (seleccionado)
                            Modifier.border(2.dp, Blue700, CircleShape)
                        else Modifier
                    )
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (paciente.icon != null) {
                    Icon(paciente.icon, contentDescription = null, tint = Blue700, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = paciente.inicial ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Blue700
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = paciente.nombre,
            style = MaterialTheme.typography.labelSmall,
            color = if (seleccionado) Blue700 else TextSecondary,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun BotonAgregar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = Blue100, spotColor = Blue100)
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Registrar medicamento o cita",
                tint = Blue700
            )
        }
    }
}



private fun construirPacientes(): List<PacienteFiltro> {

    val propio = PacienteFiltro(
        personaId = null,
        nombre = "Yo",
        icon = Icons.Outlined.Person,
        pendientes = contarPendientes(null)
    )

    val asociados = PersonaRepository.obtenerPersonasAsociadas().map { persona ->
        PacienteFiltro(
            personaId = persona.id,
            nombre = persona.nombre,
            inicial = persona.nombre.firstOrNull()?.uppercase() ?: "?",
            pendientes = contarPendientes(persona.id)
        )
    }

    return listOf(propio) + asociados
}

private fun contarPendientes(personaId: String?): Int {

    val citas = if (personaId == null) {
        CitaRepository.obtenerPropias()
    } else {
        CitaRepository.obtenerPorPersona(personaId)
    }

    val medicamentos = if (personaId == null) {
        MedicamentoRepository.obtenerPropios()
    } else {
        MedicamentoRepository.obtenerPorPersona(personaId)
    }

    val tomasDeMedicamento = medicamentos.sumOf { medicamento ->
        medicamento.horarios.ifEmpty { listOf("") }.size
    }

    return citas.size + tomasDeMedicamento
}




private fun construirRecordatorios(
    filtro: FiltroTipo,
    personaId: String?
): List<GrupoRecordatorios> {

    val hoyMillis = obtenerHoyUtcMillis()

    val citasBase = if (personaId == null) {
        CitaRepository.obtenerPropias()
    } else {
        CitaRepository.obtenerPorPersona(personaId)
    }

    val medicamentosBase = if (personaId == null) {
        MedicamentoRepository.obtenerPropios()
    } else {
        MedicamentoRepository.obtenerPorPersona(personaId)
    }

    val itemsCitas = citasBase
        .filter { cita ->
            when (filtro) {
                FiltroTipo.TODOS -> true
                FiltroTipo.CITAS -> cita.tipo != TipoCita.EXAMENES
                FiltroTipo.EXAMENES -> cita.tipo == TipoCita.EXAMENES
                FiltroTipo.MEDICAMENTOS -> false
            }
        }
        .map { cita ->
            val subtitulo = when {
                cita.modalidad == ModalidadCita.VIRTUAL && cita.nombreMedico.isNotBlank() ->
                    "Virtual · ${cita.nombreMedico}"

                cita.modalidad == ModalidadCita.VIRTUAL ->
                    "Cita virtual"

                cita.institucion.isNotBlank() ->
                    cita.institucion

                else ->
                    cita.motivo
            }

            RecordatorioConFecha(
                fechaMillis = cita.fechaMillis,
                recordatorio = Recordatorio(
                    tipo = TipoRecordatorio.CITA,
                    id = cita.id,
                    tipoCita = cita.tipo,
                    horaOrden = cita.hora,
                    minutoOrden = cita.minuto,
                    horaTexto = formatearHoraRecordatorio(cita.hora, cita.minuto),
                    icono = if (cita.tipo == TipoCita.EXAMENES) {
                        Icons.Outlined.Science
                    } else {
                        Icons.Outlined.MedicalServices
                    },
                    titulo = if (cita.especialidad.isNotBlank()) {
                        cita.especialidad
                    } else {
                        cita.tipo.titulo
                    },
                    subtitulo = subtitulo
                )
            )
        }

    val itemsMedicamentos = if (filtro == FiltroTipo.TODOS || filtro == FiltroTipo.MEDICAMENTOS) {

        medicamentosBase.flatMap { medicamento ->

            val horarios = medicamento.horarios.ifEmpty { listOf("") }

            horarios.map { horario ->
                val (horaOrden, minutoOrden) = parsearHorario(horario)

                val subtitulo = if (medicamento.cantidadPorToma.isNotBlank()) {
                    "${medicamento.cantidadPorToma} · ${medicamento.dosis}${medicamento.unidad}"
                } else {
                    "${medicamento.dosis}${medicamento.unidad}"
                }

                RecordatorioConFecha(
                    // Los medicamentos son recurrentes (no tienen una fecha
                    // puntual como las citas), así que se agrupan bajo "Hoy".
                    fechaMillis = hoyMillis,
                    recordatorio = Recordatorio(
                        tipo = TipoRecordatorio.MEDICAMENTO,
                        id = medicamento.id,
                        tipoCita = null,
                        horaOrden = horaOrden,
                        minutoOrden = minutoOrden,
                        horaTexto = horario.ifBlank { "--:--" },
                        icono = Icons.Outlined.Medication,
                        titulo = medicamento.nombre,
                        subtitulo = subtitulo
                    )
                )
            }
        }

    } else {
        emptyList()
    }

    return (itemsCitas + itemsMedicamentos)
        .groupBy { it.fechaMillis }
        .toSortedMap()
        .map { (fechaMillis, envolturas) ->
            GrupoRecordatorios(
                fechaMillis = fechaMillis,
                etiqueta = etiquetaParaFecha(fechaMillis, hoyMillis),
                esHoy = fechaMillis == hoyMillis,
                items = envolturas
                    .map { it.recordatorio }
                    .sortedWith(compareBy({ it.horaOrden }, { it.minutoOrden }))
            )
        }
}

private data class RecordatorioConFecha(
    val fechaMillis: Long,
    val recordatorio: Recordatorio
)



private fun obtenerHoyUtcMillis(): Long {

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

private fun etiquetaParaFecha(
    fechaMillis: Long,
    hoyMillis: Long
): String {

    val unDiaMillis = 24L * 60L * 60L * 1000L

    return when (fechaMillis) {

        hoyMillis ->
            "Hoy, ${formatearFechaLarga(fechaMillis)}"

        hoyMillis + unDiaMillis ->
            "Mañana, ${formatearFechaLarga(fechaMillis)}"

        else ->
            formatearFechaLarga(fechaMillis).replaceFirstChar { primero -> primero.uppercase() }
    }
}

private fun formatearFechaLarga(millis: Long): String {

    val formatter = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))
    formatter.timeZone = TimeZone.getTimeZone("UTC")

    return formatter.format(Date(millis))
}

private fun formatearHoraRecordatorio(hora: Int, minuto: Int): String {

    val amPm = if (hora < 12) "AM" else "PM"

    val hora12 = when {
        hora == 0 -> 12
        hora > 12 -> hora - 12
        else -> hora
    }

    return String.format(Locale.getDefault(), "%d:%02d %s", hora12, minuto, amPm)
}


private fun parsearHorario(horario: String): Pair<Int, Int> {

    if (horario.isBlank()) {
        return 0 to 0
    }

    return try {
        val formato = SimpleDateFormat("h:mm a", Locale.US)
        val fecha = formato.parse(horario) ?: return 0 to 0

        val calendar = Calendar.getInstance()
        calendar.time = fecha

        calendar.get(Calendar.HOUR_OF_DAY) to calendar.get(Calendar.MINUTE)

    } catch (e: Exception) {
        0 to 0
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RemindersScreenPreview() {
    HealthControlTheme {
        RemindersScreen()
    }
}