package com.icm2630.proyecto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.AuditoriaRegistro
import com.icm2630.proyecto.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AuditoriaRegistroSection(auditoria: AuditoriaRegistro?) {
    if (auditoria == null) return
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (auditoria.creadoPorUid != null) {
            Text("Registrado por ${auditoria.creadoPorNombre.orEmpty()}" +
                (auditoria.creadoEn?.let { " · ${formato.format(it.toDate())}" } ?: ""),
                style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        if (auditoria.creadoPorUid == null || auditoria.creadoEn != auditoria.actualizadoEn) {
            Text("Última modificación: ${auditoria.actualizadoPorNombre}" +
                (auditoria.actualizadoEn?.let { " · ${formato.format(it.toDate())}" } ?: ""),
                style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
