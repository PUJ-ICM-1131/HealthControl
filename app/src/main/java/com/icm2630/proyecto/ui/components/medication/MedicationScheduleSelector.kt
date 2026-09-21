package com.icm2630.proyecto.ui.components.medication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.icm2630.proyecto.ui.theme.Blue500
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextSecondary


@Composable
fun MedicationScheduleSelector(
    schedules: List<String>,
    onAddSchedule: () -> Unit,
    onDeleteSchedule: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Text(
            text = "Horarios de toma",
            color = Blue700,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )


        Spacer(
            modifier = Modifier.size(4.dp)
        )


        Text(
            text = "Agrega las horas en las que debe tomarse el medicamento.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )


        Spacer(
            modifier = Modifier.size(14.dp)
        )

        // SIN HORARIOS
        if (schedules.isEmpty()) {

            EmptyScheduleCard()

        } else {

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                schedules.forEach { schedule ->

                    ScheduleCard(
                        time = schedule,

                        onDelete = {
                            onDeleteSchedule(schedule)
                        }
                    )
                }
            }
        }


        Spacer(
            modifier = Modifier.size(12.dp)
        )

        // AGREGAR HORARIO
        OutlinedButton(
            onClick = onAddSchedule,

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(16.dp),

            border = BorderStroke(
                width = 1.3.dp,
                color = Blue500
            )
        ) {

            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = Blue700
            )


            Spacer(
                modifier = Modifier.width(8.dp)
            )


            Text(
                text = "Agregar horario",
                color = Blue700,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ESTADO VACÍO
@Composable
private fun EmptyScheduleCard() {

    Surface(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(16.dp),

        color = Blue50,

        border = BorderStroke(
            width = 1.dp,
            color = Blue100
        )
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 15.dp
            ),

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
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Blue700,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }


            Spacer(
                modifier = Modifier.width(12.dp)
            )


            Column {

                Text(
                    text = "Sin horarios registrados",
                    color = Blue700,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )


                Text(
                    text = "Agrega al menos una hora de toma.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// TARJETA DE HORARIO
@Composable
private fun ScheduleCard(
    time: String,
    onDelete: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(16.dp),

        color = Color.White,

        border = BorderStroke(
            width = 1.dp,
            color = Blue100
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    end = 4.dp,
                    top = 7.dp,
                    bottom = 7.dp
                ),

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
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Blue700,
                        modifier = Modifier.size(22.dp)
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
                    text = time,
                    color = Blue700,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )


                Text(
                    text = getMomentOfDay(time),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            IconButton(
                onClick = onDelete
            ) {

                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Eliminar horario",
                    tint = TextSecondary
                )
            }
        }
    }
}

// MOMENTO DEL DÍA
private fun getMomentOfDay(
    time: String
): String {

    val upperTime =
        time.uppercase()

    val hour =
        time
            .substringBefore(":")
            .trim()
            .toIntOrNull()
            ?: return "Horario programado"


    return when {

        "AM" in upperTime &&
                hour in 5..11 -> {
            "En la mañana"
        }

        "PM" in upperTime &&
                (
                        hour == 12 ||
                                hour in 1..5
                        ) -> {
            "En la tarde"
        }

        "PM" in upperTime &&
                hour in 6..11 -> {
            "En la noche"
        }

        else -> {
            "Horario programado"
        }
    }
}

// PREVIEW
@Preview(
    showBackground = true,
    name = "Horarios medicamento"
)
@Composable
private fun MedicationScheduleSelectorPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            MedicationScheduleSelector(

                schedules = listOf(
                    "8:00 AM",
                    "2:00 PM",
                    "8:00 PM"
                ),

                onAddSchedule = {},

                onDeleteSchedule = {},

                modifier = Modifier.padding(16.dp)
            )
        }
    }
}