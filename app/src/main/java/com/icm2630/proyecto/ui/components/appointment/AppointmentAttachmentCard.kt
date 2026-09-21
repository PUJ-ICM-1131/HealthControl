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
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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


@Composable
fun AppointmentAttachmentCard(
    fileName: String?,
    onAttachClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val hasFile =
        !fileName.isNullOrBlank()


    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onAttachClick()
            },

        shape = RoundedCornerShape(18.dp),

        color =
            if (hasFile) {
                Blue100.copy(alpha = 0.35f)
            } else {
                Blue50
            },

        border = BorderStroke(
            width =
                if (hasFile) {
                    1.5.dp
                } else {
                    1.dp
                },

            color =
                if (hasFile) {
                    Blue500
                } else {
                    Blue100
                }
        )
    ) {

        Row(
            modifier = Modifier.padding(
                start = 14.dp,
                top = 14.dp,
                bottom = 14.dp,
                end = 6.dp
            ),

            verticalAlignment = Alignment.CenterVertically
        ) {
            // ICONO
            Surface(
                modifier = Modifier.size(46.dp),

                shape = CircleShape,

                color =
                    if (hasFile) {
                        Blue700
                    } else {
                        Blue100
                    }
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            if (hasFile) {
                                Icons.Outlined.CheckCircle
                            } else {
                                Icons.Outlined.AttachFile
                            },

                        contentDescription = null,

                        tint =
                            if (hasFile) {
                                Color.White
                            } else {
                                Blue700
                            }
                    )
                }
            }


            Spacer(
                modifier = Modifier.width(12.dp)
            )

            // INFORMACIÓN DEL ARCHIVO

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text =
                        if (hasFile) {
                            fileName!!
                        } else {
                            "Adjuntar soporte"
                        },

                    color = Blue700,

                    style =
                        MaterialTheme.typography.bodyLarge,

                    fontWeight =
                        FontWeight.Bold
                )


                Text(
                    text =
                        if (hasFile) {
                            "Archivo seleccionado"
                        } else {
                            "Orden médica, autorización o PDF"
                        },

                    color = TextSecondary,

                    style =
                        MaterialTheme.typography.bodySmall
                )
            }

            // ELIMINAR ARCHIVO

            if (hasFile) {

                IconButton(
                    onClick = onRemoveClick
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.Close,

                        contentDescription =
                            "Eliminar archivo",

                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

// PREVIEW SIN ARCHIVO
@Preview(
    showBackground = true,
    name = "Soporte de cita vacío"
)
@Composable
private fun AppointmentAttachmentEmptyPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            AppointmentAttachmentCard(
                fileName = null,
                onAttachClick = {},
                onRemoveClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// PREVIEW CON ARCHIVO
@Preview(
    showBackground = true,
    name = "Soporte de cita seleccionado"
)
@Composable
private fun AppointmentAttachmentSelectedPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            AppointmentAttachmentCard(
                fileName = "orden_cardiologia.pdf",
                onAttachClick = {},
                onRemoveClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}