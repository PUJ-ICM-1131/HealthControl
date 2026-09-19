package com.icm2630.proyecto.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.KeyboardArrowRight
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue50
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.TextSecondary


@Composable
fun MonitoreoScreen(
    onNavigate: (Routes) -> Unit,
    onOpenMap: () -> Unit
) {

    Scaffold(

        containerColor = Blue50,

        bottomBar = {

            HealthBottomNavigation(
                currentRoute = Routes.Monitoreo,
                onNavigate = onNavigate
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            Spacer(
                modifier = Modifier.height(20.dp)
            )


            Text(
                text = "Monitoreo",
                color = Blue700,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )


            Text(
                text = "Supervisa la información de salud de las personas que tienes asociadas.",

                color = TextSecondary,

                style = MaterialTheme.typography.bodyLarge
            )


            Spacer(
                modifier = Modifier.height(6.dp)
            )


            // =================================================
            // PERSONAS ASOCIADAS
            // =================================================

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(22.dp),

                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color.White
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = Blue100
                    ) {

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector = Icons.Outlined.People,
                                contentDescription = null,
                                tint = Blue700
                            )
                        }
                    }


                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {

                        Text(
                            text = "Personas asociadas",
                            color = Blue700,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )


                        Text(
                            text = "Consulta su información y seguimiento.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }


            // =================================================
            // UBICACIÓN
            // =================================================

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(),

                onClick = onOpenMap,

                shape = RoundedCornerShape(22.dp),

                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color.White
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = Blue100
                    ) {

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = Blue700,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }


                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {

                        Text(
                            text = "Ubicación",
                            color = Blue700,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )


                        Spacer(
                            modifier = Modifier.height(3.dp)
                        )


                        Text(
                            text = "Consulta la última ubicación registrada de una persona asociada.",

                            color = TextSecondary,

                            style = MaterialTheme.typography.bodyMedium
                        )
                    }


                    Icon(
                        imageVector =
                            Icons.Outlined.KeyboardArrowRight,

                        contentDescription =
                            "Ver ubicación",

                        tint = Blue700
                    )
                }
            }
        }
    }
}