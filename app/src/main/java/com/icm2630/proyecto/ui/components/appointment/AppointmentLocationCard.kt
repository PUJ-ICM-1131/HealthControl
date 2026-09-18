package com.icm2630.proyecto.ui.components.appointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun AppointmentLocationCard(
    modality: String,
    institution: String,
    address: String,
    virtualLink: String,
    onModalityChange: (String) -> Unit,
    onInstitutionChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onVirtualLinkChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        // =====================================================
        // MODALIDAD
        // =====================================================

        Text(
            text = "Modalidad",
            color = Blue700,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )


        Spacer(
            modifier = Modifier.height(4.dp)
        )


        Text(
            text = "Selecciona cómo se realizará la cita.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            AppointmentModalityOption(
                title = "Presencial",
                icon = Icons.Outlined.Place,
                selected = modality == "Presencial",
                onClick = {
                    onModalityChange("Presencial")
                },
                modifier = Modifier.weight(1f)
            )


            AppointmentModalityOption(
                title = "Virtual",
                icon = Icons.Outlined.Videocam,
                selected = modality == "Virtual",
                onClick = {
                    onModalityChange("Virtual")
                },
                modifier = Modifier.weight(1f)
            )
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // =====================================================
        // PRESENCIAL
        // =====================================================

        if (modality == "Presencial") {

            OutlinedTextField(
                value = institution,
                onValueChange = onInstitutionChange,
                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Clínica, hospital o institución")
                },

                placeholder = {
                    Text("Ej. Clínica del Country")
                },

                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Business,
                        contentDescription = null
                    )
                },

                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Dirección")
                },

                placeholder = {
                    Text("Ej. Carrera 16 # 82-57")
                },

                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null
                    )
                },

                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }


        // =====================================================
        // VIRTUAL
        // =====================================================

        if (modality == "Virtual") {

            OutlinedTextField(
                value = virtualLink,
                onValueChange = onVirtualLinkChange,
                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Enlace o plataforma")
                },

                placeholder = {
                    Text("Ej. Google Meet, Teams o enlace de acceso")
                },

                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Link,
                        contentDescription = null
                    )
                },

                shape = RoundedCornerShape(16.dp)
            )


            Spacer(
                modifier = Modifier.height(10.dp)
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

                Text(
                    text = "Puedes ingresar el enlace ahora o completarlo posteriormente cuando la entidad lo envíe.",

                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 12.dp
                    ),

                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


// =============================================================
// OPCIÓN DE MODALIDAD
// =============================================================

@Composable
private fun AppointmentModalityOption(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(16.dp),

        color =
            if (selected) {
                Blue700
            } else {
                Color.White
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
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 14.dp
            )
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,

                tint =
                    if (selected) {
                        Color.White
                    } else {
                        Blue700
                    }
            )


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            Text(
                text = title,

                color =
                    if (selected) {
                        Color.White
                    } else {
                        Blue700
                    },

                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// =============================================================
// PREVIEW
// =============================================================

@Preview(
    showBackground = true,
    name = "Lugar cita presencial"
)
@Composable
private fun AppointmentLocationCardPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            AppointmentLocationCard(
                modality = "Presencial",
                institution = "Clínica del Country",
                address = "Carrera 16 # 82-57",
                virtualLink = "",
                onModalityChange = {},
                onInstitutionChange = {},
                onAddressChange = {},
                onVirtualLinkChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}