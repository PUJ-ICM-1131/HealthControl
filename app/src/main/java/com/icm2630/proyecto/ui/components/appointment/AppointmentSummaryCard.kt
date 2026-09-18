package com.icm2630.proyecto.ui.components.appointment

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
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
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
fun AppointmentSummaryCard(
    personName: String,
    appointmentType: String,
    specialty: String,
    date: String,
    time: String,
    modality: String,
    institution: String,
    address: String,
    virtualLink: String,
    doctorName: String,
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

            // =================================================
            // ENCABEZADO
            // =================================================

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
                        text = "Confirma que los datos de la cita sean correctos.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }


            SummarySeparator()


            // =================================================
            // PERSONA
            // =================================================

            AppointmentSummaryItem(
                icon = Icons.Outlined.Person,
                title = "Cita para",
                value =
                    personName.ifBlank {
                        "Sin completar"
                    }
            )


            SummarySeparator()


            // =================================================
            // TIPO DE CITA
            // =================================================

            AppointmentSummaryItem(
                icon = Icons.Outlined.LocalHospital,
                title = "Tipo de cita",
                value = buildAppointmentTypeDescription(
                    appointmentType = appointmentType,
                    specialty = specialty,
                    doctorName = doctorName
                )
            )


            SummarySeparator()


            // =================================================
            // FECHA
            // =================================================

            AppointmentSummaryItem(
                icon = Icons.Outlined.CalendarMonth,
                title = "Fecha",
                value =
                    date.ifBlank {
                        "Sin completar"
                    }
            )


            SummarySeparator()


            // =================================================
            // HORA
            // =================================================

            AppointmentSummaryItem(
                icon = Icons.Outlined.Schedule,
                title = "Hora",
                value =
                    time.ifBlank {
                        "Sin completar"
                    }
            )


            SummarySeparator()


            // =================================================
            // LUGAR / MODALIDAD
            // =================================================

            AppointmentSummaryItem(
                icon = Icons.Outlined.Place,
                title = "Lugar",
                value = buildLocationDescription(
                    modality = modality,
                    institution = institution,
                    address = address,
                    virtualLink = virtualLink
                )
            )
        }
    }
}


// =============================================================
// ITEM DEL RESUMEN
// =============================================================

@Composable
private fun AppointmentSummaryItem(
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
// DESCRIPCIÓN DEL TIPO DE CITA
// =============================================================

private fun buildAppointmentTypeDescription(
    appointmentType: String,
    specialty: String,
    doctorName: String
): String {

    if (appointmentType.isBlank()) {
        return "Sin completar"
    }


    val parts =
        mutableListOf<String>()


    parts.add(
        appointmentType
    )


    if (specialty.isNotBlank()) {

        parts.add(
            specialty
        )
    }


    if (doctorName.isNotBlank()) {

        parts.add(
            doctorName
        )
    }


    return parts.joinToString(
        separator = " · "
    )
}


// =============================================================
// DESCRIPCIÓN DEL LUGAR
// =============================================================

private fun buildLocationDescription(
    modality: String,
    institution: String,
    address: String,
    virtualLink: String
): String {

    if (modality.isBlank()) {
        return "Sin completar"
    }


    return when (modality) {

        "Virtual" -> {

            if (virtualLink.isBlank()) {

                "Virtual"

            } else {

                "Virtual · $virtualLink"
            }
        }


        else -> {

            val parts =
                mutableListOf<String>()

            parts.add(
                "Presencial"
            )


            if (institution.isNotBlank()) {

                parts.add(
                    institution
                )
            }


            if (address.isNotBlank()) {

                parts.add(
                    address
                )
            }


            parts.joinToString(
                separator = " · "
            )
        }
    }
}


// =============================================================
// PREVIEW
// =============================================================

@Preview(
    showBackground = true,
    name = "Resumen cita"
)
@Composable
private fun AppointmentSummaryCardPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            AppointmentSummaryCard(
                personName = "Carlos Rodríguez",
                appointmentType = "Especialista",
                specialty = "Cardiología",
                date = "22/09/2026",
                time = "10:30 AM",
                modality = "Presencial",
                institution = "Clínica del Country",
                address = "Carrera 16 # 82-57",
                virtualLink = "",
                doctorName = "Dra. Laura Gómez",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}