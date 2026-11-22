package com.icm2630.proyecto.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.TextSecondary

@Composable
fun FiltroListadoChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto, maxLines = 1) },
        modifier = Modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            labelColor = TextSecondary,
            selectedContainerColor = Blue700,
            selectedLabelColor = Color.White
        ),
        border = BorderStroke(1.dp, if (seleccionado) Blue700 else Color(0xFFE5E7EB))
    )
}
