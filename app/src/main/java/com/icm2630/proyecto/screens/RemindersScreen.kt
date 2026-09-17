package com.icm2630.proyecto.screens

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Person
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
import com.icm2630.proyecto.components.HealthBottomNavigation
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
            //Cabeza de pantalla:
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

        }
    }
}

@Composable
private fun FiltroChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (seleccionado) Blue700 else Color.White,
        shape = RoundedCornerShape(50),
        border = if (!seleccionado) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
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