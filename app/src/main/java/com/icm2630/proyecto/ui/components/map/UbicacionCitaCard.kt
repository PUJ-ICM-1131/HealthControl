package com.icm2630.proyecto.ui.components.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.ModalidadCita
import com.icm2630.proyecto.data.repository.SesionRepository
import com.icm2630.proyecto.ui.viewmodel.MapUiState

@Composable
fun UbicacionCitaCard(state: MapUiState, onBuscar: () -> Unit, onGuardar: () -> Unit, onRuta: () -> Unit) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val cita = state.cita
        when {
            state.cargandoCita -> {
                Text("Consultando cita…")
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            cita == null -> Text("La cita no está disponible. Vuelve a abrirla cuando se restablezca el acceso.")
            cita.modalidad == ModalidadCita.VIRTUAL -> Text("Esta cita es virtual. Abre su enlace desde el detalle.")
            else -> {
                Text(cita.institucion.ifBlank { "Lugar de la cita" }, style = MaterialTheme.typography.titleMedium)
                Text("Para: ${state.titularCitaUid?.let(SesionRepository::nombreDe).orEmpty()}",
                    style = MaterialTheme.typography.bodySmall)
                Text(cita.direccion.ifBlank { "La cita no tiene dirección escrita." },
                    style = MaterialTheme.typography.bodySmall)
                val punto = state.destino
                if (punto == null) {
                    Text("Busca el lugar e incluye la ciudad, o mantén pulsado el mapa para elegir un punto.",
                        style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(if (state.destinoCitaConfirmado) "Lugar confirmado en la cita" else "Revisa este punto antes de usarlo",
                        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(punto.nombre, style = MaterialTheme.typography.bodySmall)
                    if (punto.nombre == "Punto seleccionado") {
                        Text(coordenadasMapa(punto), style = MaterialTheme.typography.labelSmall)
                    }
                }
                state.mensajeLugar?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                val puedeGuardar = state.titularCitaUid?.let(SesionRepository::puedeGestionar) == true
                if (punto != null && !state.destinoCitaConfirmado && !puedeGuardar) {
                    Text("Puedes usar este punto para tu recorrido. Solo el titular o un cuidador autorizado puede guardarlo en la cita.",
                        style = MaterialTheme.typography.bodySmall)
                }
                if (punto != null && !state.destinoCitaConfirmado && puedeGuardar) {
                    Button(onClick = onGuardar, enabled = !state.guardandoLugar,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text(if (state.guardandoLugar) "Guardando…" else "Confirmar y guardar lugar")
                    }
                }
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onBuscar, enabled = !state.guardandoLugar,
                        modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 48.dp)) {
                        Text(if (punto == null) "Buscar lugar" else "Buscar otro")
                    }
                    if (punto != null) {
                        OutlinedButton(onClick = onRuta, enabled = !state.guardandoLugar && !state.calculandoRuta,
                            modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 48.dp)) {
                            Text(if (state.calculandoRuta) "Calculando…" else if (state.destinoCitaConfirmado)
                                "Cómo llegar" else "Ruta a este punto")
                        }
                    }
                }
            }
        }
    }
}
