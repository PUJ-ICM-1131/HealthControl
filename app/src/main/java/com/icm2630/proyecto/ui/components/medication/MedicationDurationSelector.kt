package com.icm2630.proyecto.ui.components.medication

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
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
fun MedicationDurationSelector(
    startDate: String,
    endDate: String,
    permanentTreatment: Boolean,
    endDateError: Boolean = false,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onPermanentTreatmentChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        // FECHA DE INICIO
        Text(
            text = "Fecha de inicio",
            color = Blue700,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )


        Spacer(
            modifier = Modifier.size(8.dp)
        )


        MedicationDateCard(
            value = startDate,
            placeholder = "Seleccionar fecha de inicio",
            onClick = onStartDateClick
        )


        Spacer(
            modifier = Modifier.size(14.dp)
        )

        // TRATAMIENTO PERMANENTE

        PermanentTreatmentCard(
            checked = permanentTreatment,
            onCheckedChange = onPermanentTreatmentChange
        )

        // FECHA DE FINALIZACIÓN
        if (!permanentTreatment) {

            Spacer(
                modifier = Modifier.size(18.dp)
            )


            Text(
                text = "Fecha de finalización",
                color = Blue700,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )


            Spacer(
                modifier = Modifier.size(8.dp)
            )


            MedicationDateCard(
                value = endDate,
                placeholder = "Seleccionar fecha de finalización",
                isError = endDateError,
                onClick = onEndDateClick
            )


            if (endDateError) {

                Spacer(
                    modifier = Modifier.size(6.dp)
                )


                Text(
                    text = "La fecha de finalización debe ser posterior a la fecha de inicio.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// TARJETA DE FECHA
@Composable
private fun MedicationDateCard(
    value: String,
    placeholder: String,
    isError: Boolean = false,
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
            width = 1.2.dp,

            color =
                if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    Blue100
                }
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
                modifier = Modifier.size(44.dp),

                shape = CircleShape,

                color = Blue100
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
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


                if (value.isNotBlank()) {

                    Text(
                        text = "Toca para cambiar",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// TRATAMIENTO PERMANENTE
@Composable
private fun PermanentTreatmentCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(!checked)
            },

        shape = RoundedCornerShape(16.dp),

        color =
            if (checked) {
                Blue100.copy(alpha = 0.55f)
            } else {
                Blue50
            },

        border = BorderStroke(
            width =
                if (checked) {
                    1.5.dp
                } else {
                    1.dp
                },

            color =
                if (checked) {
                    Blue500
                } else {
                    Blue100
                }
        )
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 12.dp
            ),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(44.dp),

                shape = CircleShape,

                color =
                    if (checked) {
                        Blue700
                    } else {
                        Blue100
                    }
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Outlined.EventRepeat,
                        contentDescription = null,

                        tint =
                            if (checked) {
                                Color.White
                            } else {
                                Blue700
                            },

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
                    text = "Tratamiento permanente",
                    color = Blue700,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )


                Text(
                    text = "Actívalo si no tiene una fecha de finalización.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

// PREVIEW
@Preview(
    showBackground = true,
    name = "Duración medicamento"
)
@Composable
private fun MedicationDurationSelectorPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            MedicationDurationSelector(
                startDate = "17/09/2026",
                endDate = "30/09/2026",
                permanentTreatment = false,
                endDateError = false,
                onStartDateClick = {},
                onEndDateClick = {},
                onPermanentTreatmentChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}