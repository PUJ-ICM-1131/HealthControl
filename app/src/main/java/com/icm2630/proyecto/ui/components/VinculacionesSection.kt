package com.icm2630.proyecto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.Invitacion
import com.icm2630.proyecto.data.model.PersonaVinculada
import com.icm2630.proyecto.ui.viewmodel.PerfilUiState

@Composable
fun VinculacionesSection(
    miUid: String,
    personas: List<PersonaVinculada>,
    estado: PerfilUiState,
    onAgregar: () -> Unit,
    onVer: (PersonaVinculada) -> Unit,
    onResponder: (Invitacion, Boolean) -> Unit,
    onCancelar: (Invitacion) -> Unit,
    onDesvincular: (Invitacion) -> Unit
) {
    var porDesvincular by remember { mutableStateOf<Invitacion?>(null) }
    Text("Personas vinculadas", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (personas.isEmpty()) Text("Aún no tienes personas vinculadas.")
            personas.forEach { persona ->
                Column {
                    Text(persona.nombre, style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onVer(persona) }, enabled = !estado.guardando) { Text("Ver salud") }
                        val vinculo = estado.vinculos.find { it.otroUid(miUid) == persona.uid }
                        TextButton(onClick = { porDesvincular = vinculo },
                            enabled = vinculo != null && !estado.guardando) { Text("Desvincular") }
                    }
                }
            }
            OutlinedButton(onClick = onAgregar, enabled = !estado.guardando) { Text("Añadir mediante código") }
            Text("Al aceptar una solicitud, ambas personas podrán consultar sus datos de salud compartidos. El correo, los ajustes y el contacto de emergencia permanecen privados.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
    Spacer(Modifier.height(24.dp))
    Text("Solicitudes recibidas", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (estado.recibidas.isEmpty()) Text("No tienes solicitudes pendientes.")
            estado.recibidas.forEach { solicitud ->
                Column {
                    Text(solicitud.nombreEmisor, style = MaterialTheme.typography.titleSmall)
                    Text("Quiere vincularse contigo para compartir sus datos de salud.", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onResponder(solicitud, false) }, enabled = !estado.guardando) { Text("Rechazar") }
                        Button(onClick = { onResponder(solicitud, true) }, enabled = !estado.guardando) { Text("Aceptar") }
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    Text("Solicitudes enviadas", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (estado.enviadas.isEmpty()) Text("No tienes solicitudes pendientes de respuesta.")
            estado.enviadas.forEach { solicitud ->
                Column {
                    Text(solicitud.nombreDestinatario, style = MaterialTheme.typography.titleSmall)
                    Text("Pendiente de aceptación", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { onCancelar(solicitud) }, enabled = !estado.guardando) { Text("Cancelar solicitud") }
                }
            }
        }
    }
    porDesvincular?.let { solicitud ->
        AlertDialog(
            onDismissRequest = { porDesvincular = null },
            title = { Text("¿Eliminar vínculo?") },
            text = { Text("Ambas personas dejarán de consultar los datos compartidos de la otra. Para volver a compartir tendrán que aceptar una nueva solicitud.") },
            confirmButton = {
                TextButton(onClick = { onDesvincular(solicitud); porDesvincular = null },
                    enabled = !estado.guardando) { Text("Desvincular") }
            },
            dismissButton = { TextButton(onClick = { porDesvincular = null }) { Text("Cancelar") } }
        )
    }
}
