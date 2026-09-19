package com.icm2630.proyecto.ui.components.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue50
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextSecondary


@Composable
fun MapPreviewCard(
    personName: String,
    address: String,
    lastUpdate: String,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(410.dp),

        shape = RoundedCornerShape(28.dp),

        color = Color(0xFFDCEEFF),

        border = BorderStroke(
            width = 1.dp,
            color = Blue100
        ),

        shadowElevation = 3.dp
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // =================================================
            // MAPA SIMULADO
            // =================================================

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {

                // Fondo
                drawRect(
                    color = Color(0xFFDDEEFF)
                )


                // Calle diagonal
                drawLine(
                    color = Color.White.copy(alpha = 0.78f),

                    start = Offset(
                        x = -50f,
                        y = size.height * 0.72f
                    ),

                    end = Offset(
                        x = size.width + 80f,
                        y = size.height * 0.35f
                    ),

                    strokeWidth = 42f
                )


                // Calle vertical
                drawLine(
                    color = Color.White.copy(alpha = 0.72f),

                    start = Offset(
                        x = size.width * 0.60f,
                        y = -40f
                    ),

                    end = Offset(
                        x = size.width * 0.56f,
                        y = size.height + 40f
                    ),

                    strokeWidth = 34f
                )


                // Calle secundaria
                drawLine(
                    color = Color(0xFFF5FAFF),

                    start = Offset(
                        x = -20f,
                        y = size.height * 0.35f
                    ),

                    end = Offset(
                        x = size.width + 30f,
                        y = size.height * 0.28f
                    ),

                    strokeWidth = 20f
                )


                // =================================================
                // ZONA SEGURA
                // =================================================

                val center = Offset(
                    x = size.width * 0.50f,
                    y = size.height * 0.52f
                )


                drawCircle(
                    color = Color(0xFFB7D8FF).copy(
                        alpha = 0.28f
                    ),

                    radius = size.minDimension * 0.31f,

                    center = center
                )


                drawCircle(
                    color = Color(0xFF1B73C9),

                    radius = size.minDimension * 0.30f,

                    center = center,

                    style = Stroke(
                        width = 4f,

                        pathEffect =
                            PathEffect.dashPathEffect(
                                floatArrayOf(
                                    14f,
                                    10f
                                )
                            )
                    )
                )


                drawCircle(
                    color = Color(0xFF89BDF2),

                    radius = size.minDimension * 0.20f,

                    center = center,

                    style = Stroke(
                        width = 2f
                    )
                )
            }


            // =================================================
            // ZONA SEGURA
            // =================================================

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),

                shape = RoundedCornerShape(50),

                color = Color.White,

                shadowElevation = 2.dp
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 13.dp,
                        vertical = 8.dp
                    ),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = CircleShape,
                        color = Color(0xFF20C77A)
                    ) {}


                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )


                    Text(
                        text = "Zona segura · Radio 500 m",

                        color = Blue700,

                        style =
                            MaterialTheme.typography.bodyMedium,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }


            // =================================================
            // FARMACIA
            // =================================================

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = 68.dp,
                        end = 14.dp
                    ),

                shape = RoundedCornerShape(50),

                color = Color.White,

                shadowElevation = 2.dp
            ) {

                Text(
                    text = "Farmacia · 250 m",

                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 7.dp
                    ),

                    color = Blue700,

                    style =
                        MaterialTheme.typography.bodySmall,

                    fontWeight =
                        FontWeight.SemiBold
                )
            }


            // =================================================
            // CENTRO DE SALUD
            // =================================================

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 16.dp,
                        bottom = 96.dp
                    ),

                shape = RoundedCornerShape(50),

                color = Color.White,

                shadowElevation = 2.dp
            ) {

                Text(
                    text = "Centro de salud · 400 m",

                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 7.dp
                    ),

                    color = Blue700,

                    style =
                        MaterialTheme.typography.bodySmall,

                    fontWeight =
                        FontWeight.SemiBold
                )
            }


            // =================================================
            // MARCADOR DE LA PERSONA
            // =================================================

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        y = (-8).dp
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Surface(
                    modifier = Modifier.size(74.dp),

                    shape = CircleShape,

                    color = Color.White,

                    border = BorderStroke(
                        width = 5.dp,
                        color = Blue700
                    ),

                    shadowElevation = 7.dp
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Surface(
                            modifier = Modifier.size(58.dp),
                            shape = CircleShape,
                            color = Blue100
                        ) {

                            Box(
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Outlined.Person,

                                    contentDescription =
                                        personName,

                                    tint =
                                        Blue700,

                                    modifier =
                                        Modifier.size(34.dp)
                                )
                            }
                        }
                    }
                }


                // Punta del marcador
                Surface(
                    modifier = Modifier
                        .size(
                            width = 18.dp,
                            height = 12.dp
                        )
                        .offset(
                            y = (-3).dp
                        ),

                    shape =
                        RoundedCornerShape(
                            bottomStart = 12.dp,
                            bottomEnd = 12.dp
                        ),

                    color = Blue700
                ) {}
            }


            // =================================================
            // DIRECCIÓN
            // =================================================

            Surface(
                modifier = Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .padding(14.dp)
                    .fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp),

                color =
                    Color.White,

                shadowElevation =
                    4.dp
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 13.dp
                    ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Surface(
                        modifier =
                            Modifier.size(46.dp),

                        shape =
                            RoundedCornerShape(15.dp),

                        color =
                            Blue50
                    ) {

                        Box(
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Outlined.LocationOn,

                                contentDescription =
                                    null,

                                tint =
                                    Blue700,

                                modifier =
                                    Modifier.size(27.dp)
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.width(12.dp)
                    )


                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text =
                                if (address.isBlank()) {
                                    "Ubicación no disponible"
                                } else {
                                    address
                                },

                            color =
                                Blue700,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,

                            fontWeight =
                                FontWeight.Bold
                        )


                        Spacer(
                            modifier =
                                Modifier.height(2.dp)
                        )


                        Text(
                            text =
                                lastUpdate,

                            color =
                                TextSecondary,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }
                }
            }
        }
    }
}


@Preview(
    showBackground = true,
    name = "Mapa simulado"
)
@Composable
private fun MapPreviewCardPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            MapPreviewCard(
                personName = "Carlos Rodríguez",

                address =
                    "Av. Las Palmeras 450, Centro",

                lastUpdate =
                    "Actualizado hace 2 minutos",

                modifier =
                    Modifier.padding(16.dp)
            )
        }
    }
}