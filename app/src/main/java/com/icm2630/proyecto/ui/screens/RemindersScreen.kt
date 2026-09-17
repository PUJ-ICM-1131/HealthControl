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
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.theme.*

private val Fondo = Color(0xFFF8FAFF)

//Filtro por tipo de recordatorio:
private enum class FiltroTipo(val etiqueta: String) {
    TODOS("Todos"),
    MEDICAMENTOS("Medicamentos"),
    CITAS("Citas"),
    EXAMENES("Exámenes")
}


private data class PacienteFiltro(
    val nombre: String,
    val icon: ImageVector? = null,
    val inicial: String? = null,
    val pendientes: Int = 0
)

private val pacientesDeEjemplo = listOf(
    PacienteFiltro("Yo", icon = Icons.Outlined.Person),
    PacienteFiltro("Mamá", inicial = "M", pendientes = 3),
    PacienteFiltro("Papá", inicial = "P", pendientes = 1),
    PacienteFiltro("Kalel", inicial = "K")
)

//Para poder filtrar el recordatorio necesitamos tener un estado con variables que definen a un recordatorio.
private enum class EstadoRecordatorio {
    TOMADO, TOMAR, PENDIENTE, VER_DETALLES, NINGUNO
}

private data class Recordatorio(
    val hora: String,
    val icono: ImageVector,
    val titulo: String,
    val subtitulo: String,
    val estado: EstadoRecordatorio = EstadoRecordatorio.NINGUNO
)

private data class GrupoRecordatorios(
    val fecha: String,
    val esHoy: Boolean,
    val items: List<Recordatorio>
)


//A continuacion solo tendremos por el momento datos de prueba para la pantalla.(Luego estos datos se extraeran desde el backend)
private val recordatoriosDeEjemplo = listOf(
    GrupoRecordatorios(
        fecha = "Hoy, 5 de septiembre",
        esHoy = true,
        items = listOf(
            Recordatorio("08:00", Icons.Outlined.Medication, "Metformina 500mg", "1 comprimido", EstadoRecordatorio.TOMADO),
            Recordatorio("10:30", Icons.Outlined.Medication, "Vitamina D3", "2000 UI", EstadoRecordatorio.TOMAR),
            Recordatorio("14:00", Icons.Outlined.Medication, "Losartán 50mg", "1 comprimido", EstadoRecordatorio.PENDIENTE),
            Recordatorio("16:00", Icons.Outlined.MedicalServices, "Dr. García · Cardiología", "Clínica San Rafael", EstadoRecordatorio.VER_DETALLES)
        )
    ),
    GrupoRecordatorios(
        fecha = "Mañana, 6 de septiembre",
        esHoy = false,
        items = listOf(
            Recordatorio("08:00", Icons.Outlined.Medication, "Metformina 500mg", "1 comprimido"),
            Recordatorio("09:00", Icons.Outlined.Science, "Examen de sangre", "Laboratorio Central"),
            Recordatorio("10:30", Icons.Outlined.Medication, "Vitamina D3", "2000 UI")
        )
    )
)




@Composable
fun RemindersScreen(
    onNavigate: (Routes) -> Unit = {}
) {
    var filtroSeleccionado by remember { mutableStateOf(FiltroTipo.TODOS) }
    var pacienteSeleccionado by remember { mutableStateOf(pacientesDeEjemplo.first().nombre) }

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
                    text = "Recordatorios",
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
                pacientesDeEjemplo.forEach { paciente ->
                    PacienteChip(
                        paciente = paciente,
                        seleccionado = paciente.nombre == pacienteSeleccionado,
                        onClick = { pacienteSeleccionado = paciente.nombre }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            recordatoriosDeEjemplo.forEachIndexed { index, grupo ->
                GrupoRecordatoriosSeccion(grupo)
                if (index != recordatoriosDeEjemplo.lastIndex) {
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun GrupoRecordatoriosSeccion(grupo: GrupoRecordatorios) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = grupo.fecha,
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
            RecordatorioFila(item)
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
private fun RecordatorioFila(item: Recordatorio) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.hora,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Blue700,
            modifier = Modifier.width(52.dp)
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

        EstadoRecordatorioIndicador(item.estado)
    }
}

@Composable
private fun EstadoRecordatorioIndicador(estado: EstadoRecordatorio) {
    when (estado) {
        EstadoRecordatorio.TOMADO -> Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Tomado", style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
        }

        EstadoRecordatorio.TOMAR -> Button(
            onClick = { },
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Blue700)
        ) {
            Text("Tomar", style = MaterialTheme.typography.labelMedium, color = Color.White)
        }

        EstadoRecordatorio.PENDIENTE -> Text(
            text = "Pendiente",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )

        EstadoRecordatorio.VER_DETALLES -> Text(
            text = "Ver detalles",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Blue500,
            modifier = Modifier.clickable { }
        )

        EstadoRecordatorio.NINGUNO -> Unit
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RemindersScreenPreview() {
    HealthControlTheme {
        RemindersScreen()
    }
}