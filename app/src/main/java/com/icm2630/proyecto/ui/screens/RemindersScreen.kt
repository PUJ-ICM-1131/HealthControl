package com.icm2630.proyecto.ui.screens

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.icm2630.proyecto.ui.components.FiltroListadoChip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icm2630.proyecto.data.model.TomaProgramada
import com.icm2630.proyecto.data.model.TomasProgramadas
import com.icm2630.proyecto.data.repository.TomaRepository
import com.icm2630.proyecto.ui.components.EstadoToma
import com.icm2630.proyecto.ui.viewmodel.TomasViewModel
import com.icm2630.proyecto.ui.viewmodel.TomasUiState
import com.icm2630.proyecto.data.model.FechasPendientes
import com.icm2630.proyecto.ui.util.rememberHoyUtcMillis
import com.icm2630.proyecto.data.model.ModalidadCita
import com.icm2630.proyecto.data.model.TipoCita
import com.icm2630.proyecto.data.repository.CitaRepository
import com.icm2630.proyecto.data.repository.MedicamentoRepository
import com.icm2630.proyecto.data.repository.VinculacionRepository
import com.icm2630.proyecto.data.repository.SesionRepository
import com.icm2630.proyecto.ui.components.BannerFamiliar
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.data.model.Cita
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
    CITAS("Citas")
}



private data class PacienteFiltro(
    val personaId: String?,
    val nombre: String,
    val icon: ImageVector? = null,
    val inicial: String? = null,
    val pendientes: Int = 0
)

// MODELOS INTERNOS DE PANTALLA
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
    val subtitulo: String,
    val toma: TomaProgramada? = null,
    val cita: Cita? = null
)

private data class GrupoRecordatorios(
    val fechaMillis: Long,
    val etiqueta: String,
    val esHoy: Boolean,
    val urgencia: NivelUrgencia,
    val items: List<Recordatorio>
)

/**
 * Prioridad visual de un grupo de recordatorios según qué tan
 * cerca está su fecha. Se usa para la franja de color de cada
 * tarjeta y el color del encabezado del grupo.
 */
private enum class NivelUrgencia(val color: Color, val etiqueta: String?) {
    HOY(Color(0xFFEF4444), "Hoy"),
    PRONTO(Color(0xFFF59E0B), "Pronto"),
    FUTURO(Blue500, null)
}


private fun calcularUrgencia(fechaMillis: Long, hoyMillis: Long): NivelUrgencia {

    val unDiaMillis = 24L * 60L * 60L * 1000L
    val diasRestantes = (fechaMillis - hoyMillis) / unDiaMillis

    return when {
        diasRestantes == 0L -> NivelUrgencia.HOY
        diasRestantes <= 3 -> NivelUrgencia.PRONTO
        else -> NivelUrgencia.FUTURO
    }
}

@Composable
fun RemindersScreen(
    filtroInicialMedicamentos: Boolean = false,
    onNavigate: (Routes) -> Unit = {},
    onVerDetalle: (Routes) -> Unit = {},
    tomasViewModel: TomasViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    estadoTomas: TomasUiState = TomasUiState()
) {
    var filtroSeleccionado by remember { mutableStateOf(if (filtroInicialMedicamentos) FiltroTipo.MEDICAMENTOS else FiltroTipo.TODOS) }


    var pacienteSeleccionado by remember(SesionRepository.familiarActivo?.uid) { mutableStateOf<String?>(null) }

    // El permiso del titular determina si además de consultar se pueden registrar datos.
    val familiar = SesionRepository.familiarActivo

    val hoyMillis = rememberHoyUtcMillis()
    val pacientes = construirPacientes(hoyMillis)
    val grupos = construirRecordatorios(
        filtro = filtroSeleccionado,
        personaId = pacienteSeleccionado,
        hoyMillis = hoyMillis
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
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Blue700
                )

                if (SesionRepository.puedeGestionar(pacienteSeleccionado ?: SesionRepository.uidConsultado)) {
                    BotonAgregar(onClick = {
                        pacienteSeleccionado?.let { id ->
                            VinculacionRepository.obtenerPorId(id)?.let(SesionRepository::verFamiliar)
                        }
                        onNavigate(Routes.Registrar)
                    })
                }
            }

            Spacer(Modifier.height(20.dp))

            if (familiar != null) {
                BannerFamiliar(persona = familiar)
                Spacer(Modifier.height(20.dp))
            }

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

            //Filtro por paciente (solo en el perfil propio)
            if (familiar == null) {
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
            }

            if (grupos.isEmpty()) {
                EstadoVacio()
            } else {
                grupos.forEachIndexed { index, grupo ->
                    GrupoRecordatoriosSeccion(
                        grupo = grupo,
                        onVerDetalle = { destino ->
                            onVerDetalle(destino)
                        },
                        hoy = hoyMillis,
                        tomasViewModel = tomasViewModel,
                        estadoTomas = estadoTomas,
                        uid = pacienteSeleccionado ?: TomaRepository.cuentaConsultada()
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
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tus próximas citas y medicamentos aparecerán aquí",
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun GrupoRecordatoriosSeccion(
    grupo: GrupoRecordatorios,
    onVerDetalle: (Routes) -> Unit,
    hoy: Long,
    tomasViewModel: TomasViewModel,
    estadoTomas: TomasUiState,
    uid: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f).padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {

            // Punto de color: prioridad visual del grupo según su fecha
            // (rojo = hoy o vencido, ámbar = próximos 3 días, azul = más adelante).
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(grupo.urgencia.color, CircleShape)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = grupo.etiqueta,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = grupo.urgencia.color
            )
        }

        grupo.urgencia.etiqueta?.let { etiquetaUrgencia ->
            Surface(
                color = grupo.urgencia.color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = etiquetaUrgencia,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = grupo.urgencia.color,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    TarjetaBlanca {
        grupo.items.forEachIndexed { index, item ->
            RecordatorioFila(
                item = item,
                colorPrioridad = grupo.urgencia.color,
                onClick = {
                    val destino = if (item.tipo == TipoRecordatorio.CITA) {
                        Routes.DetalleCita(citaId = item.id, titularUid = uid)
                    } else {
                        Routes.DetalleMedicamento(medicamentoId = item.id, titularUid = uid)
                    }
                    onVerDetalle(destino)
                }
            )
            item.cita?.let { cita ->
                com.icm2630.proyecto.ui.components.VerLugarCitaButton(cita,
                    onClick = { onVerDetalle(Routes.MapaCita(cita.id, uid ?: SesionRepository.uidConsultado)) },
                    modifier = Modifier.align(Alignment.End))
            }
            item.toma?.let { toma ->
                EstadoToma(toma, hoy, tomasViewModel, estadoTomas, uid)
            }
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
    colorPrioridad: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).height(40.dp).background(colorPrioridad, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(10.dp))
        Surface(color = Blue50, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(item.icono, null, tint = Blue500, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Blue700, fontWeight = FontWeight.SemiBold)) { append(item.horaTexto) }
                    if (item.subtitulo.isNotBlank()) append(" · " + item.subtitulo)
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Outlined.KeyboardArrowRight, "Ver detalle", tint = TextSecondary, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun FiltroChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FiltroListadoChip(texto, seleccionado, onClick)
}

@Composable
private fun PacienteChip(
    paciente: PacienteFiltro,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
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
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
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
            .size(48.dp)
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

// CONSTRUCCIÓN DE LA LISTA DE PACIENTES (monitoreo familiar)


private fun construirPacientes(hoyMillis: Long): List<PacienteFiltro> {

    val propio = PacienteFiltro(
        personaId = null,
        nombre = "Yo",
        icon = Icons.Outlined.Person,
        pendientes = contarPendientes(null, hoyMillis)
    )

    val asociados = VinculacionRepository.obtenerPersonasAsociadas().map { persona ->
        PacienteFiltro(
            personaId = persona.id,
            nombre = persona.nombre,
            inicial = persona.nombre.firstOrNull()?.uppercase() ?: "?",
            pendientes = contarPendientes(persona.id, hoyMillis)
        )
    }

    return listOf(propio) + asociados
}

private fun contarPendientes(personaId: String?, hoyMillis: Long): Int {

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

    val uid = personaId ?: TomaRepository.cuentaConsultada()
    val tomasDeMedicamento = TomasProgramadas.pendientes(medicamentos, hoyMillis, TomaRepository.claves(uid)).size
    return citas.count { FechasPendientes.citaPendiente(it.fechaMillis, hoyMillis) } + tomasDeMedicamento
}

// CONSTRUCCIÓN DE LOS RECORDATORIOS A PARTIR DE LOS REPOSITORIOS
private fun construirRecordatorios(
    filtro: FiltroTipo,
    personaId: String?,
    hoyMillis: Long
): List<GrupoRecordatorios> {

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
        .filter { FechasPendientes.citaPendiente(it.fechaMillis, hoyMillis) }
        .filter { cita ->
            when (filtro) {
                FiltroTipo.TODOS -> true
                FiltroTipo.CITAS -> true
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
                fechaMillis = FechasPendientes.dia(cita.fechaMillis),
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
                    subtitulo = subtitulo,
                    cita = cita
                )
            )
        }

    val itemsMedicamentos = if (filtro == FiltroTipo.TODOS || filtro == FiltroTipo.MEDICAMENTOS) {

        medicamentosBase.flatMap { medicamento ->

            val fechaPendiente = FechasPendientes.proximoDia(medicamento, hoyMillis)
                ?: return@flatMap emptyList()
            val horarios = medicamento.horarios.distinct().filter { TomasProgramadas.minutos(it) != null }

            horarios.map { horario ->
                val (horaOrden, minutoOrden) = parsearHorario(horario)

                val subtitulo = if (medicamento.cantidadPorToma.isNotBlank()) {
                    "${medicamento.cantidadPorToma} · ${medicamento.dosis}${medicamento.unidad}"
                } else {
                    "${medicamento.dosis}${medicamento.unidad}"
                }

                RecordatorioConFecha(
                    // Una toma vigente pertenece a hoy; una futura, al inicio del tratamiento.
                    fechaMillis = fechaPendiente,
                    recordatorio = Recordatorio(
                        tipo = TipoRecordatorio.MEDICAMENTO,
                        id = medicamento.id,
                        tipoCita = null,
                        horaOrden = horaOrden,
                        minutoOrden = minutoOrden,
                        horaTexto = horario.ifBlank { "--:--" },
                        icono = Icons.Outlined.Medication,
                        titulo = medicamento.nombre,
                        subtitulo = subtitulo,
                        toma = TomaProgramada(medicamento, fechaPendiente, horario)
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
                urgencia = calcularUrgencia(fechaMillis, hoyMillis),
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

// UTILIDADES DE FECHA / HORA

/**
 * Medianoche de "hoy" expresada en UTC, para que sea comparable
 * con los `fechaMillis` guardados desde el DatePicker (Material3
 * trabaja siempre en UTC).
 */

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