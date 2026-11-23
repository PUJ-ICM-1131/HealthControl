package com.icm2630.proyecto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.PersonaVinculada

@Composable
fun SelectorPersonaSalud(
    personas: List<PersonaVinculada>,
    seleccionada: PersonaVinculada?,
    onSeleccionar: (PersonaVinculada?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        OutlinedCard(onClick = { abierto = true }, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.People, null)
                Column(Modifier.weight(1f)) {
                    Text("Salud que estás consultando", style = MaterialTheme.typography.labelMedium)
                    Text(seleccionada?.nombre ?: "Mi salud", fontWeight = FontWeight.SemiBold)
                }
                Icon(Icons.Outlined.ExpandMore, "Elegir persona")
            }
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            Column(Modifier.heightIn(max = 360.dp).widthIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                DropdownMenuItem(text = { Text("Mi salud") },
                    onClick = { abierto = false; onSeleccionar(null) })
                personas.forEach { persona ->
                    DropdownMenuItem(
                        text = { Column {
                            Text(persona.nombre, fontWeight = FontWeight.SemiBold)
                            Text(if (persona.puedeGestionar) "Gestión autorizada" else "Consulta",
                                style = MaterialTheme.typography.bodySmall)
                        } },
                        onClick = { abierto = false; onSeleccionar(persona) }
                    )
                }
            }
        }
    }
}
