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
import androidx.compose.material.icons.outlined.Person
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
fun MedicationPersonCard(
    personName: String,
    personType: String,
    canChangePerson: Boolean = false,
    onChangePerson: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),

        shape = RoundedCornerShape(22.dp),

        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),

        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            // ÍCONO DE PERSONA
            Surface(
                modifier = Modifier.size(52.dp),
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
                        modifier = Modifier.size(27.dp)
                    )
                }
            }


            Spacer(
                modifier = Modifier.width(14.dp)
            )

            // INFORMACIÓN
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Medicamento para",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )


                Text(
                    text = personName,
                    color = Blue700,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )


                Spacer(
                    modifier = Modifier.size(3.dp)
                )


                Surface(
                    shape = RoundedCornerShape(50),
                    color = Blue100
                ) {

                    Text(
                        text = personType,

                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        ),

                        color = Blue700,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // CAMBIAR PERSONA
            if (canChangePerson) {

                TextButton(
                    onClick = onChangePerson
                ) {

                    Text(
                        text = "Cambiar",
                        color = Blue700,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// PREVIEW - PERFIL PERSONAL
@Preview(
    showBackground = true,
    name = "Medicamento - Mi perfil"
)
@Composable
private fun MedicationPersonCardPersonalPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            MedicationPersonCard(
                personName = "Mi perfil",
                personType = "Perfil personal",
                canChangePerson = false,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// PREVIEW - PERSONA ASOCIADA
@Preview(
    showBackground = true,
    name = "Medicamento - Persona asociada"
)
@Composable
private fun MedicationPersonCardAssociatedPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            MedicationPersonCard(
                personName = "Carlos Rodríguez",
                personType = "Persona asociada",
                canChangePerson = true,
                onChangePerson = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}