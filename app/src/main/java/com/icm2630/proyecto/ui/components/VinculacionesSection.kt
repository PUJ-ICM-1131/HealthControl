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
    onDesvincular: (Invitacion) -> Unit,
    onPermitirGestion: (Invitacion, Boolean) -> Unit
) {
    var porDesvincular by remember { mutableStateOf<Invitacion?>(null) }
    var cambioPermiso by remember { mutableStateOf<Pair<Invitacion, Boolean>?>(null) }
    Text("Personas vinculadas", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (personas.isEmpty()) Text("Aún no tienes personas vinculadas.")
            personas.forEach { persona ->
                val vinculo = estado.vinculos.find { it.otroUid(miUid) == persona.uid }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(persona.nombre, style = MaterialTheme.typography.titleSmall)
                    Text(when {
                        persona.puedeGestionar -> "Puedes gestionar sus citas y medicamentos. Sus tomas las confirma esta persona."
                        persona.gestionaMiSalud -> "Puede ayudarte a gestionar tus citas y medicamentos. Tus tomas las confirmas tú."
                        else -> "Vínculo de consulta."
                    }, style = MaterialTheme.typography.bodySmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onVer(persona) }, enabled = !estado.guardando) { Text("Ver salud") }
                        if (vinculo != null && (vinculo.titularGestionUid.isEmpty() || persona.gestionaMiSalud)) {
                            TextButton(onClick = { cambioPermiso = vinculo to !persona.gestionaMiSalud },
                                enabled = !estado.guardando) {
                                Text(if (persona.gestionaMiSalud) "Retirar permiso" else "Permitir gestión")
                            }
                        }
                        TextButton(onClick = { porDesvincular = vinculo },
                            enabled = vinculo != null && !estado.guardando) { Text("Desvincular") }
                    }
                }
            }
            OutlinedButton(onClick = onAgregar, enabled = !estado.guardando) { Text("Ayudar a alguien mediante código") }
            Text("Envía una solicitud para ayudar a otra persona. Al aceptarla, autoriza que gestiones sus citas y medicamentos; no obtiene permiso para modificar los tuyos. Los perfiles privados y las confirmaciones de tomas siguen bajo el control de su titular.",
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(solicitud.nombreEmisor, style = MaterialTheme.typography.titleSmall)
                    Text(if (solicitud.solicitaGestion)
                        "Quiere ayudarte a gestionar tu salud. Si aceptas, podrá crear y editar tus citas y medicamentos y consultar tus tomas. Solo tú podrás confirmar las tomas. No tendrás permiso para modificar sus registros."
                        else "Quiere compartir la consulta de sus datos de salud contigo. Esta solicitud anterior no concede gestión.",
                        style = MaterialTheme.typography.bodySmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onResponder(solicitud, false) }, enabled = !estado.guardando) { Text("Rechazar") }
                        Button(onClick = { onResponder(solicitud, true) }, enabled = !estado.guardando) {
                            Text(if (solicitud.solicitaGestion) "Aceptar y autorizar" else "Aceptar consulta")
                        }
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
                    Text(if (solicitud.solicitaGestion) "Esperando su autorización para ayudarle."
                        else "Pendiente de aceptación para consulta.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { onCancelar(solicitud) }, enabled = !estado.guardando) { Text("Cancelar solicitud") }
                }
            }
        }
    }
    cambioPermiso?.let { (solicitud, permitir) ->
        AlertDialog(
            onDismissRequest = { cambioPermiso = null },
            title = { Text(if (permitir) "¿Autorizar a ${solicitud.nombreDelOtro(miUid)}?" else "¿Retirar permiso de gestión?") },
            text = { Text(if (permitir)
                "Esta persona podrá crear y editar tus citas y medicamentos. Tú seguirás confirmando tus tomas. Puedes retirar el permiso cuando quieras."
                else "Esta persona dejará de modificar tus citas y medicamentos. El vínculo conservará la consulta de datos compartidos.") },
            confirmButton = { TextButton(enabled = !estado.guardando, onClick = {
                onPermitirGestion(solicitud, permitir); cambioPermiso = null
            }) { Text(if (permitir) "Autorizar" else "Retirar permiso") } },
            dismissButton = { TextButton(onClick = { cambioPermiso = null }) { Text("Cancelar") } }
        )
    }
    porDesvincular?.let { solicitud ->
        AlertDialog(
            onDismissRequest = { porDesvincular = null },
            title = { Text("¿Eliminar vínculo?") },
            text = { Text("Ambas personas dejarán de consultar los datos compartidos de la otra y se retirará el permiso de gestión. Para volver a compartir tendrán que aceptar una nueva solicitud.") },
            confirmButton = {
                TextButton(onClick = { onDesvincular(solicitud); porDesvincular = null },
                    enabled = !estado.guardando) { Text("Desvincular") }
            },
            dismissButton = { TextButton(onClick = { porDesvincular = null }) { Text("Cancelar") } }
        )
    }
}
