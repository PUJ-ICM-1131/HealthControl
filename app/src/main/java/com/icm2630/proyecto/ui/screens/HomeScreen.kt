package com.icm2630.proyecto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icm2630.proyecto.R
import com.icm2630.proyecto.data.model.Cita
import com.icm2630.proyecto.data.model.Medicamento
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.PersonaVinculada
import com.icm2630.proyecto.data.repository.CitaRepository
import com.icm2630.proyecto.data.repository.MedicamentoRepository
import com.icm2630.proyecto.data.repository.PersonaRepository
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.components.BannerFamiliar
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private data class QuickAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: Routes?
)

/**
 * Home del Titular (HU-02). Si llega [familiar], se está consultando el
 * HealthControl de esa persona: se muestra su información en solo lectura.
 */
@Composable
fun HomeScreen(
    perfil: PerfilUsuario = PerfilUsuario(),
    familiar: PersonaVinculada? = null,
    onNavigate: (Routes) -> Unit = {},
    onVolverAMiPerfil: () -> Unit = {}
) {
    val soloLectura = familiar != null

    val allActions = remember {
        listOf(
            QuickAction("dosis", "Registrar Dosis", "Toma rápida", Icons.Outlined.Medication, Routes.RegistrarMedicamento()),
            QuickAction("cita", "Nueva Cita", "Agendar visita", Icons.Outlined.AddBox, Routes.Registrar),
            QuickAction("signos", "Medir Signos", "Presión / Pulso", Icons.Outlined.Timeline, null),
            QuickAction("cuidador", "Compartir", "Añadir cuidador", Icons.Outlined.Group, Routes.Perfil),
            // Opciones extra para personalizar
            QuickAction("historial", "Ver Historial", "Tus registros", Icons.Outlined.History, Routes.Historial),
            QuickAction("mapa", "Ver Mapa", "Ubicaciones", Icons.Outlined.Map, Routes.Mapa)
        )
    }

    // En solo lectura solo se ofrecen acciones de consulta.
    val accionesFamiliar = remember {
        listOf(
            QuickAction("pendientes", "Pendientes", "Sus recordatorios", Icons.Outlined.StickyNote2, Routes.Recordatorios),
            QuickAction("historial", "Historial", "Sus registros", Icons.Outlined.History, Routes.Historial),
            QuickAction("perfil", "Su Perfil", "Datos de salud", Icons.Outlined.Person, Routes.Perfil),
            QuickAction("mapa", "Ver Mapa", "Ubicaciones", Icons.Outlined.Map, Routes.Mapa)
        )
    }

    var selectedActionIds by remember { mutableStateOf(setOf("dosis", "cita", "signos", "cuidador")) }
    var showCustomizer by remember { mutableStateOf(false) }
    var dosisTomada by remember { mutableStateOf(false) }

    val nombrePropio = familiar?.nombre?.substringBefore(" ") ?: "Yo"
    val resumen = construirResumenHome(nombrePropio)

    Scaffold(
        bottomBar = {
            HealthBottomNavigation(
                currentRoute = Routes.Home,
                onNavigate = onNavigate
            )
        },
        containerColor = Color(0xFFF8FAFF)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoSmall()
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Health Control",
                        style = MaterialTheme.typography.titleLarge,
                        color = Blue700,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón de Perfil que navega directamente
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(Blue100)
                        .clickable { onNavigate(Routes.Perfil) }
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "Ver perfil",
                        modifier = Modifier.align(Alignment.Center),
                        tint = Blue700
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Hola, ${perfil.nombreCompleto.substringBefore(" ").ifBlank { "de nuevo" }}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Blue700
            )

            if (familiar != null) {
                Spacer(Modifier.height(16.dp))
                BannerFamiliar(
                    persona = familiar,
                    onVolverAMiPerfil = onVolverAMiPerfil
                )
            }

            perfil.acompanantes.takeIf { it.isNotEmpty() && !soloLectura }?.let { acompanantes ->
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = Blue100.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Group, null, modifier = Modifier.size(16.dp), tint = Blue500)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (acompanantes.size == 1)
                                "${acompanantes.first().nombre} también da seguimiento a tu salud"
                            else
                                "${acompanantes.size} acompañantes dan seguimiento a tu salud",
                            style = MaterialTheme.typography.labelSmall,
                            color = Blue700
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Blue700
                )
                if (!soloLectura) {
                    IconButton(onClick = { showCustomizer = true }) {
                        Icon(Icons.Outlined.Settings, "Personalizar", tint = Blue500)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Grid Dinámico de Acciones Rápidas
            val currentActions = if (soloLectura) {
                accionesFamiliar
            } else {
                allActions.filter { it.id in selectedActionIds }
            }
            currentActions.chunked(2).forEach { rowActions ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowActions.forEachIndexed { index, action ->
                        QuickActionCard(
                            icon = action.icon,
                            title = action.title,
                            subtitle = action.subtitle,
                            onClick = { action.route?.let { onNavigate(it) } },
                            modifier = Modifier.weight(1f)
                        )
                        if (index == 0 && rowActions.size > 1) {
                            Spacer(Modifier.width(16.dp))
                        }
                    }
                    if (rowActions.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Próximos Recordatorios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Blue700
                )
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable { onNavigate(Routes.Recordatorios) }
                ) {
                    Text(
                        text = "${resumen.pendientes} pendientes",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Card Recordatorio Medicamento
            resumen.medicamento?.let { toma ->
                MedicationReminderCard(
                    titulo = toma.titulo,
                    hora = toma.hora,
                    personName = toma.paciente,
                    isTaken = dosisTomada,
                    soloLectura = soloLectura,
                    onTakenChange = { dosisTomada = it },
                    onClick = { onNavigate(Routes.Recordatorios) }
                )
                Spacer(Modifier.height(16.dp))
            }

            // Card Cita Médica
            resumen.cita?.let { cita ->
                AppointmentReminderCard(
                    medico = cita.medico,
                    cuando = cita.cuando,
                    personName = cita.paciente,
                    onClick = { onNavigate(Routes.Recordatorios) }
                )
            }

            if (resumen.medicamento == null && resumen.cita == null) {
                Text(
                    text = "No hay recordatorios próximos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }

    if (showCustomizer) {
        AlertDialog(
            onDismissRequest = { showCustomizer = false },
            title = { Text("Personalizar Home") },
            text = {
                Column {
                    Text("Selecciona las acciones que deseas ver en tu inicio (máximo 4):", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    allActions.forEach { action ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newSet = selectedActionIds.toMutableSet()
                                    if (action.id in newSet) {
                                        newSet.remove(action.id)
                                    } else if (newSet.size < 4) {
                                        newSet.add(action.id)
                                    }
                                    selectedActionIds = newSet
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = action.id in selectedActionIds,
                                onCheckedChange = null // Manejado por la fila
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(action.icon, null, tint = Blue500, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(action.title)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomizer = false }) {
                    Text("Listo", color = Blue700)
                }
            }
        )
    }
}

@Composable
private fun LogoSmall() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Blue500,
                spotColor = Blue500
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Blue100, RoundedCornerShape(12.dp))
    ) {
        Image(
            painter = painterResource(R.drawable.loguitouwu),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.height(90.dp),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Blue100.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Blue500, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Blue700, maxLines = 1)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
            }
        }
    }
}

@Composable
private fun MedicationReminderCard(
    titulo: String,
    hora: String,
    personName: String = "Yo",
    isTaken: Boolean = false,
    soloLectura: Boolean = false,
    onTakenChange: (Boolean) -> Unit = {},
    onClick: () -> Unit = {}
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (isTaken) SuccessGreenBg else Blue100.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isTaken) Icons.Outlined.CheckCircle else Icons.Outlined.Medication,
                            null,
                            tint = if (isTaken) SuccessGreen else Blue500,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Blue700)
                        
                        // Etiqueta de la persona
                        Surface(
                            color = Blue50,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Blue100)
                        ) {
                            Text(
                                text = personName,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Blue700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    if (!isTaken) {
                        Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(4.dp)) {
                            Text("Hace 10 min", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color.Red)
                        }
                    } else {
                        Surface(color = SuccessGreenBg, shape = RoundedCornerShape(4.dp)) {
                            Text("Completado", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = SuccessGreen)
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(hora, fontSize = 14.sp, color = Blue700, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (!soloLectura) {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { 
                        onTakenChange(!isTaken)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTaken) SuccessGreen else Blue700
                    )
                ) {
                    Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isTaken) "Tomada" else "Marcar como tomada")
                }
            }
        }
    }
}

@Composable
private fun AppointmentReminderCard(
    medico: String,
    cuando: String,
    personName: String = "Yo",
    onClick: () -> Unit = {}
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Blue100.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.MedicalServices, null, tint = Blue500, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CITA MÉDICA", style = MaterialTheme.typography.labelSmall, color = Blue500, fontWeight = FontWeight.Bold)
                            Text(medico, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Blue700)
                        }
                        
                        // Etiqueta de la persona
                        Surface(
                            color = Blue50,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Blue100)
                        ) {
                            Text(
                                text = personName,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Blue700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(18.dp), tint = Blue500)
                    Spacer(Modifier.width(8.dp))
                    Text(cuando, style = MaterialTheme.typography.bodyMedium, color = Blue700)
                }
            }
        }
    }
}

// RESUMEN DE RECORDATORIOS DEL PERFIL ACTIVO (propio o del familiar)

private data class ProximaToma(val titulo: String, val hora: String, val paciente: String)

private data class ProximaCita(val medico: String, val cuando: String, val paciente: String)

private data class ResumenHome(
    val pendientes: Int,
    val medicamento: ProximaToma?,
    val cita: ProximaCita?
)

/** [nombrePropio] es la etiqueta de lo que no es de una persona asociada. */
private fun construirResumenHome(nombrePropio: String): ResumenHome {

    val hoy = hoyUtcMillisHome()

    val medicamentosActivos = MedicamentoRepository.obtenerTodos().filter { medicamento ->
        medicamento.tratamientoPermanente ||
                medicamento.fechaFinMillis == null ||
                medicamento.fechaFinMillis >= hoy
    }

    val citasProximas = CitaRepository.obtenerTodas()
        .filter { it.fechaMillis >= hoy }
        .sortedWith(compareBy({ it.fechaMillis }, { it.hora }, { it.minuto }))

    val toma = medicamentosActivos.firstOrNull()?.let { medicamento: Medicamento ->
        ProximaToma(
            titulo = "${medicamento.nombre} ${medicamento.dosis}${medicamento.unidad}",
            hora = medicamento.horarios.firstOrNull() ?: "--:--",
            paciente = etiquetaPacienteHome(medicamento.personaId, nombrePropio)
        )
    }

    val cita = citasProximas.firstOrNull()?.let { proxima: Cita ->
        ProximaCita(
            medico = proxima.nombreMedico.ifBlank { proxima.especialidad.ifBlank { proxima.tipo.titulo } },
            cuando = cuandoCita(proxima, hoy),
            paciente = etiquetaPacienteHome(proxima.personaId, nombrePropio)
        )
    }

    val tomas = medicamentosActivos.sumOf { it.horarios.ifEmpty { listOf("") }.size }

    return ResumenHome(
        pendientes = citasProximas.size + tomas,
        medicamento = toma,
        cita = cita
    )
}

private fun etiquetaPacienteHome(personaId: String?, nombrePropio: String): String =
    if (personaId == null) {
        nombrePropio
    } else {
        PersonaRepository.obtenerPorId(personaId)?.nombre ?: "Familiar"
    }

private fun cuandoCita(cita: Cita, hoy: Long): String {

    val unDia = 24L * 60L * 60L * 1000L
    val hora12 = when {
        cita.hora == 0 -> 12
        cita.hora > 12 -> cita.hora - 12
        else -> cita.hora
    }
    val hora = String.format(Locale.getDefault(), "%02d:%02d %s", hora12, cita.minuto, if (cita.hora < 12) "AM" else "PM")

    val dia = when (cita.fechaMillis) {
        hoy -> "Hoy"
        hoy + unDia -> "Mañana"
        else -> SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(cita.fechaMillis))
    }

    return "$dia a las $hora"
}

private fun hoyUtcMillisHome(): Long {
    val local = Calendar.getInstance()
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    HealthControlTheme {
        HomeScreen()
    }
}
