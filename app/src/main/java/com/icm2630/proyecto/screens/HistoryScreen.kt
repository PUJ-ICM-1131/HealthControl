package com.icm2630.proyecto.screens

import androidx.compose.foundation.background
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
import com.icm2630.proyecto.components.HealthBottomNavigation
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.theme.*

private val Fondo = Color(0xFFF8FAFF)
private val CampoBg = Color(0xFFEFF5FC)
private val PlaceholderGris = Color(0xFF9CA9BC)

//Tipo de registro por el que se puede filtrar el historial:
private enum class FiltroHistorial(val etiqueta: String) {
    TODOS("Todo"),
    CITAS("Citas"),
    MEDICAMENTOS("Medicamentos"),
    EXAMENES("Exámenes")
}


private data class RegistroHistorial(
    val dia: String,
    val mes: String,
    val icono: ImageVector,
    val colorIcono: Color,
    val titulo: String,
    val colorPaciente: Color,
    val paciente: String,
    val estado: String,
    val estadoColor: Color
)

private data class GrupoHistorial(
    val mesAnio: String,
    val items: List<RegistroHistorial>
)

//Datos de ejemplo (luego se implementara en el backend los datos a extraer)
private val historialDeEjemplo = listOf(
    GrupoHistorial(
        mesAnio = "SEPTIEMBRE 2026",
        items = listOf(
            RegistroHistorial(
                dia = "15", mes = "SEP",
                icono = Icons.Outlined.MedicalServices, colorIcono = Color(0xFFF59E0B),
                titulo = "Dr. Alejandro Gómez — Cardiología",
                colorPaciente = Color(0xFFF59E0B),
                paciente = "Para Papá (Marcelo)",
                estado = "Completado", estadoColor = SuccessGreen
            ),
            RegistroHistorial(
                dia = "10", mes = "SEP",
                icono = Icons.Outlined.Medication, colorIcono = Blue500,
                titulo = "Metformina 850mg",
                colorPaciente = Color(0xFF10B981),
                paciente = "Yo (Santiago)",
                estado = "Completado a las 10:00 AM", estadoColor = SuccessGreen
            ),
            RegistroHistorial(
                dia = "02", mes = "SEP",
                icono = Icons.Outlined.Science, colorIcono = ErrorRed,
                titulo = "Análisis de Sangre Completo",
                colorPaciente = Color(0xFFEC4899),
                paciente = "Para Mamá (Andrea)",
                estado = "Cancelado", estadoColor = ErrorRed
            )
        )
    ),
    GrupoHistorial(
        mesAnio = "AGOSTO 2026",
        items = listOf(
            RegistroHistorial(
                dia = "28", mes = "AGO",
                icono = Icons.Outlined.MedicalServices, colorIcono = Color(0xFF8B5CF6),
                titulo = "Dra. Laura Soto — Pediatría",
                colorPaciente = Color(0xFF8B5CF6),
                paciente = "Kalel",
                estado = "Completado", estadoColor = SuccessGreen
            ),
            RegistroHistorial(
                dia = "15", mes = "AGO",
                icono = Icons.Outlined.Medication, colorIcono = Blue500,
                titulo = "Atorvastatina 20mg",
                colorPaciente = Color(0xFF10B981),
                paciente = "Yo (Santiago)",
                estado = "Pendiente de tomar", estadoColor = Color(0xFFF59E0B)
            )
        )
    )
)


@Composable
fun HistoryScreen(
    onNavigate: (Routes) -> Unit = {}
) {
    var busqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf(FiltroHistorial.TODOS) }

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

                BotonFiltros(onClick = { })
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

            Spacer(Modifier.height(24.dp))

            //Lista por mes
            historialDeEjemplo.forEachIndexed { index, grupo ->
                GrupoHistorialSeccion(grupo)
                if (index != historialDeEjemplo.lastIndex) {
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

//Titulo del mes con sus tarjetas
@Composable
private fun GrupoHistorialSeccion(grupo: GrupoHistorial) {
    Text(
        text = grupo.mesAnio,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = TextSecondary
    )

    Spacer(Modifier.height(12.dp))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        grupo.items.forEach { item -> RegistroHistorialTarjeta(item) }
    }
}

//Tarjeta de un registro
@Composable
private fun RegistroHistorialTarjeta(item: RegistroHistorial) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
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




//El boton filtros aunque no tiene funcionalidad actual ,sera una opcion para filtros avanzado pudiendo tener en cuenta rango de fechas o cosas asi.
@Composable
private fun BotonFiltros(onClick: () -> Unit) {
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
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistoryScreenPreview() {
    HealthControlTheme {
        HistoryScreen()
    }
}