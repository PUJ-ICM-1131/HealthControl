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