package com.icm2630.proyecto.ui.components.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextSecondary


@Composable
fun MapQuickActions(
    onCall: () -> Unit,
    onDirections: () -> Unit,
    onSafeZone: () -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        // =====================================================
        // TÍTULO
        // =====================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Acciones rápidas",
                color = Blue700,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )


            Text(
                text = "Acceso directo",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // =====================================================
        // FILA 1
        // =====================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            QuickActionCard(
                title = "Llamar",
                subtitle = "Contacto directo",
                primary = true,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Call,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                },
                onClick = onCall,
                modifier = Modifier.weight(1f)
            )


            QuickActionCard(
                title = "Cómo llegar",
                subtitle = "Ruta hacia él",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsWalk,
                        contentDescription = null,
                        tint = Blue700,
                        modifier = Modifier.size(27.dp)
                    )
                },
                onClick = onDirections,
                modifier = Modifier.weight(1f)
            )
        }


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // =====================================================
        // FILA 2
        // =====================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            QuickActionCard(
                title = "Zona segura",
                subtitle = "Radio y límites",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = Blue700,
                        modifier = Modifier.size(27.dp)
                    )
                },
                onClick = onSafeZone,
                modifier = Modifier.weight(1f)
            )


            QuickActionCard(
                title = "Historial",
                subtitle = "Rutas del día",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = Blue700,
                        modifier = Modifier.size(27.dp)
                    )
                },
                onClick = onHistory,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


// =============================================================
// TARJETA DE ACCIÓN
// =============================================================

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false
) {

    ElevatedCard(
        modifier = modifier.height(108.dp),

        onClick = onClick,

        shape = RoundedCornerShape(22.dp),

        colors = CardDefaults.elevatedCardColors(
            containerColor =
                if (primary) {
                    Blue700
                } else {
                    Color.White
                }
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

            Surface(
                modifier = Modifier.size(48.dp),

                shape = CircleShape,

                color =
                    if (primary) {
                        Color.White.copy(alpha = 0.18f)
                    } else {
                        Blue50
                    }
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }


            Spacer(
                modifier = Modifier.width(11.dp)
            )


            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,

                    color =
                        if (primary) {
                            Color.White
                        } else {
                            Blue700
                        },

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )


                Text(
                    text = subtitle,

                    color =
                        if (primary) {
                            Color.White.copy(alpha = 0.82f)
                        } else {
                            TextSecondary
                        },

                    style =
                        MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Preview(
    showBackground = true,
    name = "Acciones rápidas"
)
@Composable
private fun MapQuickActionsPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            MapQuickActions(
                onCall = {},
                onDirections = {},
                onSafeZone = {},
                onHistory = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}