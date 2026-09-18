package com.icm2630.proyecto.ui.components.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun MapPersonCard(
    personName: String,
    canChangePerson: Boolean = false,
    onChangePerson: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),

        shape = RoundedCornerShape(26.dp),

        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),

        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            // =================================================
            // PERSONA + ESTADO
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Avatar temporal
                Box {

                    Surface(
                        modifier = Modifier.size(66.dp),
                        shape = CircleShape,
                        color = Blue100
                    ) {

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Blue700,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }


                    // Indicador verde de conexión
                    Surface(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.BottomEnd),

                        shape = CircleShape,
                        color = Color.White
                    ) {

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            Surface(
                                modifier = Modifier.size(13.dp),
                                shape = CircleShape,
                                color = Color(0xFF20C77A)
                            ) {}
                        }
                    }
                }


                Spacer(
                    modifier = Modifier.width(14.dp)
                )


                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = personName,
                        color = Blue700,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )


                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )


                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFE7F8EF),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFFB7E8CD)
                        )
                    ) {

                        Row(
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            ),

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Surface(
                                modifier = Modifier.size(9.dp),
                                shape = CircleShape,
                                color = Color(0xFF20C77A)
                            ) {}


                            Spacer(
                                modifier = Modifier.width(6.dp)
                            )


                            Text(
                                text = "En movimiento · Seguro",
                                color = Color(0xFF15945C),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }


                if (canChangePerson) {

                    TextButton(
                        onClick = onChangePerson
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Cambiar persona",
                            tint = Blue700
                        )
                    }
                }
            }


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            // =================================================
            // INFORMACIÓN DEL DISPOSITIVO
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                DeviceStatusItem(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Watch,
                            contentDescription = null,
                            tint = Blue700
                        )
                    },
                    title = "Reloj",
                    value = "84%"
                )


                DeviceStatusItem(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.HealthAndSafety,
                            contentDescription = null,
                            tint = Color(0xFF20B970)
                        )
                    },
                    title = "Sensor caídas",
                    value = "Activo"
                )
            }
        }
    }
}


@Composable
private fun DeviceStatusItem(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Blue50,
        border = BorderStroke(
            width = 1.dp,
            color = Blue100
        )
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 13.dp,
                vertical = 11.dp
            ),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier.size(25.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }


            Spacer(
                modifier = Modifier.width(9.dp)
            )


            Column {

                Text(
                    text = title,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )


                Text(
                    text = value,
                    color = Blue700,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Preview(
    showBackground = true,
    name = "Persona monitoreada"
)
@Composable
private fun MapPersonCardPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            MapPersonCard(
                personName = "Carlos Rodríguez",
                canChangePerson = true,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}