package com.icm2630.proyecto.ui.components.medication

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
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
import com.icm2630.proyecto.ui.theme.Blue500
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextSecondary


@Composable
fun MedicationSummaryCard(
    medicationName: String,
    presentation: String,
    dose: String,
    quantityPerDose: String,
    personName: String,
    schedules: List<String>,
    startDate: String,
    endDate: String,
    permanentTreatment: Boolean,
    modifier: Modifier = Modifier
) {

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),

        shape = RoundedCornerShape(22.dp),

        colors = CardDefaults.elevatedCardColors(
            containerColor = Blue100.copy(
                alpha = 0.40f
            )
        ),

        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            // -------------------------------------------------
            // ENCABEZADO
            // -------------------------------------------------

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = Blue700
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }


                Spacer(
                    modifier = Modifier.width(12.dp)
                )


                Column {

                    Text(
                        text = "Revisa antes de guardar",
                        color = Blue700,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )


                    Text(
                        text = "Confirma que la información sea correcta.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }


            Spacer(
                modifier = Modifier.size(16.dp)
            )


            HorizontalDivider(
                color = Blue100
            )


            Spacer(
                modifier = Modifier.size(14.dp)
            )


            // -------------------------------------------------
            // MEDICAMENTO
            // -------------------------------------------------

            SummaryItem(
                icon = Icons.Outlined.Medication,
                title = "Medicamento",
                value = buildMedicationDescription(
                    medicationName = medicationName,
                    presentation = presentation,
                    dose = dose,
                    quantityPerDose = quantityPerDose
                )
            )


            SummarySeparator()


            // -------------------------------------------------
            // PERSONA
            // -------------------------------------------------

            SummaryItem(
                icon = Icons.Outlined.Person,
                title = "Para",
                value =
                    personName.ifBlank {
                        "Mi perfil"
                    }
            )


            SummarySeparator()


            // -------------------------------------------------
            // HORARIOS
            // -------------------------------------------------

            SummaryItem(
                icon = Icons.Outlined.Schedule,
                title = "Horarios",
                value =
                    if (schedules.isEmpty()) {
                        "Sin horarios"
                    } else {
                        schedules.joinToString(
                            separator = " · "
                        )
                    }
            )


            SummarySeparator()


            // -------------------------------------------------
            // DURACIÓN
            // -------------------------------------------------

            SummaryItem(
                icon = Icons.Outlined.CalendarMonth,
                title = "Duración",
                value = buildDurationDescription(
                    startDate = startDate,
                    endDate = endDate,
                    permanentTreatment = permanentTreatment
                )
            )
        }
    }
}


// =============================================================
// ELEMENTO DEL RESUMEN
// =============================================================

@Composable
private fun SummaryItem(
    icon: ImageVector,
    title: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {

        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Blue50
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Blue500,
                    modifier = Modifier.size(21.dp)
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
                text = title,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )


            Text(
                text = value,
                color = Blue700,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


// =============================================================
// SEPARADOR
// =============================================================

@Composable
private fun SummarySeparator() {

    Spacer(
        modifier = Modifier.size(11.dp)
    )


    HorizontalDivider(
        color = Blue100.copy(
            alpha = 0.8f
        )
    )


    Spacer(
        modifier = Modifier.size(11.dp)
    )
}


// =============================================================
// CONSTRUIR DESCRIPCIÓN DEL MEDICAMENTO
// =============================================================

private fun buildMedicationDescription(
    medicationName: String,
    presentation: String,
    dose: String,
    quantityPerDose: String
): String {

    if (medicationName.isBlank()) {
        return "Sin completar"
    }


    val parts =
        mutableListOf<String>()


    parts.add(
        medicationName
    )


    if (dose.isNotBlank()) {
        parts.add(
            dose
        )
    }


    if (
        presentation == "Pastilla" &&
        quantityPerDose.isNotBlank()
    ) {

        parts.add(
            "$quantityPerDose por toma"
        )
    }


    return parts.joinToString(
        separator = " · "
    )
}


// =============================================================
// CONSTRUIR DURACIÓN
// =============================================================

private fun buildDurationDescription(
    startDate: String,
    endDate: String,
    permanentTreatment: Boolean
): String {

    if (startDate.isBlank()) {
        return "Sin completar"
    }


    if (permanentTreatment) {

        return "$startDate · Tratamiento permanente"
    }


    if (endDate.isBlank()) {

        return "Desde $startDate"
    }


    return "$startDate - $endDate"
}


// =============================================================
// PREVIEW
// =============================================================

@Preview(
    showBackground = true,
    name = "Resumen medicamento"
)
@Composable
private fun MedicationSummaryCardPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            MedicationSummaryCard(
                medicationName = "Losartán",
                presentation = "Pastilla",
                dose = "50 mg",
                quantityPerDose = "1",
                personName = "Carlos Rodríguez",
                schedules = listOf(
                    "8:00 AM",
                    "8:00 PM"
                ),
                startDate = "17/09/2026",
                endDate = "30/09/2026",
                permanentTreatment = false,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}