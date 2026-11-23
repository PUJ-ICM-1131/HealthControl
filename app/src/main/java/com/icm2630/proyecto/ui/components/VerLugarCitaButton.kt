package com.icm2630.proyecto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.Cita
import com.icm2630.proyecto.data.model.ModalidadCita

@Composable
fun VerLugarCitaButton(cita: Cita, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (cita.modalidad != ModalidadCita.PRESENCIAL) return
    TextButton(onClick = onClick, modifier = modifier.heightIn(min = 48.dp)) {
        Icon(Icons.Outlined.Map, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Ver en mapa")
    }
}
