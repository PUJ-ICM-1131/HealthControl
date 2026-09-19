package com.icm2630.proyecto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue50
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextSecondary


@Composable
fun RegisterOptionsScreen(
    onNavigate: (Routes) -> Unit,
    onRegisterAppointment: () -> Unit,
    onRegisterMedication: () -> Unit
) {

    Scaffold(

        containerColor = Blue50,

        bottomBar = {

            HealthBottomNavigation(
                currentRoute = Routes.Registrar,
                onNavigate = onNavigate
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 18.dp
                )
        ) {

            Spacer(
                modifier = Modifier.height(28.dp)
            )


            // =================================================
            // ENCABEZADO
            // =================================================

            Text(
                text = "Registrar",
                color = Blue700,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )


            Spacer(
                modifier = Modifier.height(6.dp)
            )


            Text(
                text = "¿Qué deseas registrar?",
                color = Blue700,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )


            Spacer(
                modifier = Modifier.height(4.dp)
            )


            Text(
                text = "Selecciona una opción para agregar información a tu seguimiento de salud.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge
            )


            Spacer(
                modifier = Modifier.height(28.dp)
            )


            // =================================================
            // CITA MÉDICA
            // =================================================

            RegisterOptionCard(
                icon = Icons.Outlined.CalendarMonth,
                title = "Cita médica",
                description = "Registra una consulta, examen o cita con un especialista.",
                actionText = "Registrar cita",
                onClick = onRegisterAppointment
            )


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            // =================================================
            // MEDICAMENTO
            // =================================================

            RegisterOptionCard(
                icon = Icons.Outlined.Medication,
                title = "Medicamento",
                description = "Agrega un medicamento, dosis, horarios y duración del tratamiento.",
                actionText = "Registrar medicamento",
                onClick = onRegisterMedication
            )


            Spacer(
                modifier = Modifier.height(24.dp)
            )


            // =================================================
            // MENSAJE DE AYUDA
            // =================================================

            Surface(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(18.dp),

                color = Blue100.copy(
                    alpha = 0.45f
                ),

                border = BorderStroke(
                    width = 1.dp,
                    color = Blue100
                )
            ) {

                Text(
                    text = "La información que registres te ayudará a mantener organizadas tus citas y tratamientos.",

                    modifier = Modifier.padding(16.dp),

                    color = Blue700,

                    style = MaterialTheme.typography.bodyMedium
                )
            }


            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}


// =============================================================
// TARJETA DE OPCIÓN
// =============================================================

@Composable
private fun RegisterOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String,
    onClick: () -> Unit
) {

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),

        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.size(58.dp),
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
                            modifier = Modifier.size(29.dp)
                        )
                    }
                }


                Spacer(
                    modifier = Modifier.width(14.dp)
                )


                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = title,
                        color = Blue700,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )


                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )


                    Text(
                        text = description,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            Surface(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

                color = Blue50
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 12.dp
                    ),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = actionText,
                        color = Blue700,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )


                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Blue700,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}


// =============================================================
// PREVIEW
// =============================================================

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Opciones de registro"
)
@Composable
private fun RegisterOptionsScreenPreview() {

    HealthControlTheme {

        RegisterOptionsScreen(
            onNavigate = {},
            onRegisterAppointment = {},
            onRegisterMedication = {}
        )
    }
}