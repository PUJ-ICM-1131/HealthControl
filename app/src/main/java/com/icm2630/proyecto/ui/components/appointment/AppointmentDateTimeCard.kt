package com.icm2630.proyecto.ui.components.appointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue50
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextSecondary


@Composable
fun AppointmentDateTimeCard(
    date: String,
    time: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Text(
            text = "Fecha de la cita",
            color = Blue700,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.size(8.dp)
        )

        AppointmentDateTimeOption(
            icon = Icons.Outlined.CalendarMonth,
            value = date,
            placeholder = "Seleccionar fecha",
            helperText = "Toca para elegir el día",
            onClick = onDateClick
        )


        Spacer(
            modifier = Modifier.size(18.dp)
        )


        Text(
            text = "Hora de la cita",
            color = Blue700,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.size(8.dp)
        )

        AppointmentDateTimeOption(
            icon = Icons.Outlined.Schedule,
            value = time,
            placeholder = "Seleccionar hora",
            helperText = "Toca para elegir la hora",
            onClick = onTimeClick
        )
    }
}


@Composable
private fun AppointmentDateTimeOption(
    icon: ImageVector,
    value: String,
    placeholder: String,
    helperText: String,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(16.dp),

        color = Color.White,

        border = BorderStroke(
            width = 1.dp,
            color = Blue100
        )
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 13.dp
            ),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = Blue100
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Blue700,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }


            Spacer(
                modifier = Modifier.width(12.dp)
            )


            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text =
                        if (value.isBlank()) {
                            placeholder
                        } else {
                            value
                        },

                    color =
                        if (value.isBlank()) {
                            TextSecondary
                        } else {
                            Blue700
                        },

                    style = MaterialTheme.typography.bodyLarge,

                    fontWeight =
                        if (value.isBlank()) {
                            FontWeight.Normal
                        } else {
                            FontWeight.SemiBold
                        }
                )


                Text(
                    text = helperText,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Preview(
    showBackground = true,
    name = "Fecha y hora de cita"
)
@Composable
private fun AppointmentDateTimeCardPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            AppointmentDateTimeCard(
                date = "22/09/2026",
                time = "10:30 AM",
                onDateClick = {},
                onTimeClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}