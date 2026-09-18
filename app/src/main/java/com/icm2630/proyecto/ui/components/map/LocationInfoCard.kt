package com.icm2630.proyecto.ui.components.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue50
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextSecondary


@Composable
fun LocationInfoCard(
    address: String,
    city: String,
    lastUpdate: String,
    isMock: Boolean,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier.fillMaxWidth(),

        shape = RoundedCornerShape(22.dp),

        color = Color.White,

        border = BorderStroke(
            width = 1.dp,
            color = Blue100
        ),

        shadowElevation = 2.dp
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            // =================================================
            // TÍTULO
            // =================================================

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = Blue100
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Blue700,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }


                Spacer(
                    modifier = Modifier.width(12.dp)
                )


                Column {

                    Text(
                        text = "Última ubicación registrada",
                        color = Blue700,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )


                    Text(
                        text = "Información disponible más reciente",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }


            Spacer(
                modifier = Modifier.size(18.dp)
            )


            // =================================================
            // DIRECCIÓN
            // =================================================

            Text(
                text =
                    if (address.isBlank()) {
                        "Ubicación no disponible"
                    } else {
                        address
                    },

                color = Blue700,

                style = MaterialTheme.typography.bodyLarge,

                fontWeight = FontWeight.SemiBold
            )


            if (city.isNotBlank()) {

                Text(
                    text = city,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }


            Spacer(
                modifier = Modifier.size(16.dp)
            )


            // =================================================
            // ÚLTIMA ACTUALIZACIÓN
            // =================================================

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = Blue700,
                    modifier = Modifier.size(20.dp)
                )


                Spacer(
                    modifier = Modifier.width(8.dp)
                )


                Text(
                    text =
                        if (lastUpdate.isBlank()) {
                            "Sin información de actualización"
                        } else {
                            "Actualizado $lastUpdate"
                        },

                    color = TextSecondary,

                    style = MaterialTheme.typography.bodyMedium
                )
            }


            // =================================================
            // INDICADOR MOCK
            // =================================================

            if (isMock) {

                Spacer(
                    modifier = Modifier.size(14.dp)
                )


                Surface(
                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(14.dp),

                    color = Blue50,

                    border = BorderStroke(
                        width = 1.dp,
                        color = Blue100
                    )
                ) {

                    Row(
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 10.dp
                        ),

                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Blue700,
                            modifier = Modifier.size(20.dp)
                        )


                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )


                        Text(
                            text = "Ubicación simulada para esta versión del proyecto.",

                            color = Blue700,

                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}


@Preview(
    showBackground = true,
    name = "Información de ubicación"
)
@Composable
private fun LocationInfoCardPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            LocationInfoCard(
                address = "Carrera 7 con Calle 72",
                city = "Bogotá",
                lastUpdate = "hace 5 minutos",
                isMock = true,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}