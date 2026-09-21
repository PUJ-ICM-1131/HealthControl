package com.icm2630.proyecto.ui.components.appointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import com.icm2630.proyecto.ui.theme.Blue500
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextSecondary


data class AppointmentTypeOption(
    val title: String,
    val description: String,
    val symbol: String
)


@Composable
fun AppointmentTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val appointmentTypes = listOf(

        AppointmentTypeOption(
            title = "Medicina general",
            description = "Consulta médica general",
            symbol = "MG"
        ),

        AppointmentTypeOption(
            title = "Especialista",
            description = "Consulta especializada",
            symbol = "ESP"
        ),

        AppointmentTypeOption(
            title = "Odontología",
            description = "Consulta odontológica",
            symbol = "OD"
        ),

        AppointmentTypeOption(
            title = "Exámenes",
            description = "Laboratorio o diagnóstico",
            symbol = "EX"
        )
    )


    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        appointmentTypes
            .chunked(2)
            .forEach { rowTypes ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    rowTypes.forEach { appointmentType ->

                        AppointmentTypeCard(
                            option = appointmentType,

                            selected =
                                selectedType ==
                                        appointmentType.title,

                            onClick = {
                                onTypeSelected(
                                    appointmentType.title
                                )
                            },

                            modifier = Modifier.weight(1f)
                        )
                    }


                    if (rowTypes.size == 1) {

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
    }
}


@Composable
private fun AppointmentTypeCard(
    option: AppointmentTypeOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(18.dp),

        color =
            if (selected) {
                Blue700
            } else {
                Blue50
            },

        border = BorderStroke(
            width =
                if (selected) {
                    1.5.dp
                } else {
                    1.dp
                },

            color =
                if (selected) {
                    Blue700
                } else {
                    Blue100
                }
        )
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            // IDENTIFICADOR VISUAL
            Surface(
                modifier = Modifier.size(42.dp),

                shape = CircleShape,

                color =
                    if (selected) {
                        Color.White.copy(
                            alpha = 0.18f
                        )
                    } else {
                        Blue100
                    }
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = option.symbol,

                        color =
                            if (selected) {
                                Color.White
                            } else {
                                Blue700
                            },

                        style =
                            MaterialTheme.typography.labelMedium,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }


            Spacer(
                modifier = Modifier.size(10.dp)
            )


            Text(
                text = option.title,

                color =
                    if (selected) {
                        Color.White
                    } else {
                        Blue700
                    },

                style =
                    MaterialTheme.typography.titleSmall,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier = Modifier.size(3.dp)
            )


            Text(
                text = option.description,

                color =
                    if (selected) {
                        Color.White.copy(
                            alpha = 0.85f
                        )
                    } else {
                        TextSecondary
                    },

                style =
                    MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Preview(
    showBackground = true,
    name = "Tipos de cita"
)
@Composable
private fun AppointmentTypeSelectorPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            AppointmentTypeSelector(
                selectedType = "Especialista",
                onTypeSelected = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}