package com.icm2630.proyecto.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.CONDICIONES_COMUNES
import com.icm2630.proyecto.data.model.TipoSangre
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.TextPrimary

/** Selectores de chips compartidos por ProfileSetup y la edición en ProfileScreen. */

@Composable
private fun ChipSalud(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto, style = MaterialTheme.typography.labelLarge) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFFEFF5FC),
            labelColor = TextPrimary,
            selectedContainerColor = Blue700,
            selectedLabelColor = Color.White
        ),
        border = null
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipsTipoSangre(
    seleccionado: TipoSangre?,
    onSeleccionar: (TipoSangre) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TipoSangre.entries.forEach { tipo ->
            ChipSalud(tipo.etiqueta, tipo == seleccionado) { onSeleccionar(tipo) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipsCondiciones(
    seleccionadas: List<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CONDICIONES_COMUNES.forEach { condicion ->
            ChipSalud(condicion, condicion in seleccionadas) { onToggle(condicion) }
        }
    }
}
