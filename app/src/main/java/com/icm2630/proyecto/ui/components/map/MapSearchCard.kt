package com.icm2630.proyecto.ui.components.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.PuntoMapa
import com.icm2630.proyecto.ui.viewmodel.MapUiState
import com.icm2630.proyecto.ui.viewmodel.SeleccionMapa

@Composable
fun MapSearchCard(
    state: MapUiState,
    onConsulta: (String) -> Unit,
    onBuscar: () -> Unit,
    onElegir: (PuntoMapa) -> Unit
) {
    val origen = state.seleccion == SeleccionMapa.ORIGEN
    val acento = if (origen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    val sobreAcento = if (origen) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiary
    Column {
        OutlinedTextField(
            value = state.consulta,
            onValueChange = onConsulta,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text("Dirección o lugar y ciudad") },
            label = {
                Text(
                    if (origen) "Buscar origen" else "Buscar destino",
                    // La etiqueta flotante queda fuera del relleno del campo: cubre el mapa de fondo.
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 4.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = acento,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor = acento
            ),
            leadingIcon = { MapPointBadge(if (origen) "A" else "B", acento, sobreAcento) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onBuscar() }),
            trailingIcon = {
                if (state.buscandoDireccion) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onBuscar) {
                        Icon(Icons.Outlined.Search, contentDescription = "Buscar dirección", tint = acento)
                    }
                }
            }
        )
        if (state.resultadosBusqueda.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(16.dp), shadowElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.heightIn(max = 180.dp).verticalScroll(rememberScrollState())) {
                    state.resultadosBusqueda.forEach { punto ->
                        Column(Modifier.fillMaxWidth().clickable { onElegir(punto) }.padding(12.dp)) {
                            Text(punto.nombre, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(coordenadasMapa(punto), style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}