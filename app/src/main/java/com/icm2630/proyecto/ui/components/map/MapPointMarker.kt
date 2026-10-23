package com.icm2630.proyecto.ui.components.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry

/** Etiquetas A/B iguales a las tarjetas del planificador; la punta marca la coordenada. */
@Composable
@MapboxMapComposable
fun MapPointMarker(point: Point, letra: String, titulo: String, color: Color, onColor: Color) {
    val borde = MaterialTheme.colorScheme.surface
    // Recrear las dos vistas al cambiar punto/tema evita contenido nativo obsoleto.
    key(point, letra, titulo, color, onColor, borde) {
        val options = remember(point) {
            viewAnnotationOptions {
                geometry(point)
                allowOverlap(true)
                annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
            }
        }
        ViewAnnotation(options = options, content = @Composable @UiComposable {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "Punto $letra, $titulo de la ruta"
                }
            ) {
                Surface(
                    color = color, contentColor = onColor,
                    shape = RoundedCornerShape(18.dp), border = BorderStroke(2.dp, borde),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MapPointBadge(letra, onColor, color)
                        Text(titulo, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
                Canvas(Modifier.size(14.dp, 7.dp)) {
                    drawPath(Path().apply {
                        moveTo(0f, 0f); lineTo(size.width, 0f)
                        lineTo(size.width / 2f, size.height); close()
                    }, color)
                }
            }
        })
    }
}

@Composable
fun MapPointBadge(letra: String, color: Color, onColor: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.size(24.dp), shape = CircleShape, color = color, contentColor = onColor) {
        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
            Text(letra, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}
