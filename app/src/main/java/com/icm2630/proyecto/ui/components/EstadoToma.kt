package com.icm2630.proyecto.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.*
import com.icm2630.proyecto.data.repository.*
import com.icm2630.proyecto.ui.theme.*
import com.icm2630.proyecto.ui.viewmodel.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** El estado procede del snapshot confirmado por Firestore, nunca de un remember local. */
@Composable
fun EstadoToma(toma: TomaProgramada, hoy: Long, viewModel: TomasViewModel, estado: TomasUiState, uid: String? = TomaRepository.cuentaConsultada()) {
    val registro = TomaRepository.obtener(uid).find { it.clave == toma.clave }
    val cargado = TomaRepository.cargado(uid)
    val guardando = "${uid}/${toma.clave}" in estado.guardando
    val propia = uid != null && SesionRepository.haySesionGuardada &&
        uid == SesionRepository.uidActual && SesionRepository.familiarActivo == null
    val editable = propia && cargado &&
        TomasProgramadas.esDeHoy(toma.medicamento, toma.fechaMillis, toma.horario, hoy)

    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        when {
            TomaRepository.falloCarga(uid) -> Text(
                "No se pudo consultar el estado de las tomas.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            !cargado -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Blue700)
                Text("Consultando tomas…", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            registro != null -> Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                Text(
                    "Tomada · " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(registro.registradaEnMillis)),
                    modifier = Modifier.weight(1f),
                    color = SuccessGreen,
                    style = MaterialTheme.typography.labelLarge
                )
                if (editable) {
                    TextButton(onClick = { viewModel.cambiar(toma, false) }, enabled = !guardando) {
                        Text(if (guardando) "Guardando…" else "Deshacer")
                    }
                }
            }
            editable -> OutlinedButton(
                onClick = { viewModel.cambiar(toma, true) },
                enabled = !guardando,
                modifier = Modifier.align(Alignment.CenterEnd).heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Blue100),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Blue50, contentColor = Blue700),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Blue700)
                } else {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(if (guardando) "Guardando…" else "Marcar tomada")
            }
            else -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Text(
                    if (toma.fechaMillis > hoy) "Programada" else "Pendiente de confirmar",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
