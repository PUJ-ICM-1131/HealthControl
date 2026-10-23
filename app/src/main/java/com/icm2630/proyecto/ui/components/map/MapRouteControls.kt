package com.icm2630.proyecto.ui.components.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.ModoRuta
import com.icm2630.proyecto.data.model.PuntoMapa
import com.icm2630.proyecto.ui.viewmodel.MapUiState
import com.icm2630.proyecto.ui.viewmodel.SeleccionMapa
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun MapRouteControls(
    state: MapUiState,
    expandido: Boolean,
    onExpandir: () -> Unit,
    onSeleccionar: (SeleccionMapa) -> Unit,
    onUsarUbicacion: () -> Unit,
    onModoRuta: (ModoRuta) -> Unit,
    onCalcular: () -> Unit,
    onVerRuta: () -> Unit,
    onLimpiar: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onExpandir),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Planear ruta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = state.ruta?.let {
                        "${distanciaRuta(it.distanciaMetros)} · ${tiempoRuta(it.duracionSegundos)}"
                    } ?: "Elige un origen y un destino",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onLimpiar) {
                Icon(Icons.Outlined.Close, contentDescription = "Limpiar los puntos y la ruta")
            }
            IconButton(onClick = onExpandir) {
                Icon(
                    if (expandido) Icons.Outlined.ExpandMore else Icons.Outlined.ExpandLess,
                    contentDescription = if (expandido) "Contraer controles" else "Mostrar controles"
                )
            }
        }
        if (expandido) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PuntoRutaCard(
                    "A", "Origen", state.origen, state.seleccion == SeleccionMapa.ORIGEN,
                    MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, Modifier.weight(1f)
                ) { onSeleccionar(SeleccionMapa.ORIGEN) }
                PuntoRutaCard(
                    "B", "Destino", state.destino, state.seleccion == SeleccionMapa.DESTINO,
                    MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary, Modifier.weight(1f)
                ) { onSeleccionar(SeleccionMapa.DESTINO) }
            }
            TextButton(onClick = onUsarUbicacion) {
                Icon(Icons.Outlined.MyLocation, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Usar mi ubicación como origen")
            }
            Text(
                text = "Mantén pulsado el mapa o busca una dirección para elegir el " +
                        if (state.seleccion == SeleccionMapa.ORIGEN) "origen." else "destino.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModoRuta.entries.forEach { modo ->
                    FilterChip(
                        selected = state.modoRuta == modo,
                        onClick = { onModoRuta(modo) },
                        label = { Text(modo.titulo) },
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                if (modo == ModoRuta.CAMINANDO) Icons.AutoMirrored.Outlined.DirectionsWalk else Icons.Outlined.DirectionsCar,
                                null, Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                enabled = state.origen != null && state.destino != null && !state.calculandoRuta,
                onClick = onCalcular
            ) {
                if (state.calculandoRuta) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("  Calculando ruta…")
                } else {
                    Icon(Icons.Outlined.Directions, contentDescription = null)
                    Text("  Trazar mi ruta")
                }
            }
            if (state.ruta != null) {
                TextButton(onClick = onVerRuta, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver recorrido completo")
                }
                Text(
                    "Tiempo estimado. El origen se conserva al calcular; puedes actualizarlo con tu ubicación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PuntoRutaCard(
    letra: String,
    titulo: String,
    punto: PuntoMapa?,
    seleccionado: Boolean,
    color: Color,
    onColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (seleccionado) color.copy(alpha = 0.10f).compositeOver(MaterialTheme.colorScheme.surface) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, if (seleccionado) color else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MapPointBadge(letra, color, onColor)
                Text(titulo, fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.labelLarge)
            }
            Text(
                punto?.nombre ?: "Sin seleccionar", maxLines = 2,
                overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall
            )
            punto?.let {
                Text(
                    coordenadasMapa(it), style = MaterialTheme.typography.labelSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun coordenadasMapa(punto: PuntoMapa): String =
    String.format(Locale.US, "%.5f, %.5f", punto.latitud, punto.longitud)

fun distanciaRuta(metros: Double): String = if (metros < 1_000) {
    "${metros.roundToInt()} m"
} else {
    String.format(Locale.getDefault(), "%.1f km", metros / 1_000)
}

fun tiempoRuta(segundos: Double): String {
    val minutos = ceil(segundos / 60).toInt().coerceAtLeast(1)
    return if (minutos < 60) "$minutos min" else "${minutos / 60} h ${minutos % 60} min"
}