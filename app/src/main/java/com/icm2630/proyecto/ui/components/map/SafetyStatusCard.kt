package com.icm2630.proyecto.ui.components.map

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.CheckCircle
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
import com.icm2630.proyecto.ui.theme.Blue50
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextSecondary


@Composable
fun SafetyStatusCard(
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier.fillMaxWidth(),

        shape = RoundedCornerShape(24.dp),

        color = Color.White,

        shadowElevation = 2.dp
    ) {

        Row(
            modifier = Modifier.padding(18.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            // =================================================
            // ICONO
            // =================================================

            Surface(
                modifier = Modifier.size(52.dp),

                shape = CircleShape,

                color = Color(0xFFE5F8EF),

                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFB9E9D0)
                )
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF20B970),
                        modifier = Modifier.size(29.dp)
                    )
                }
            }


            Spacer(
                modifier = Modifier.width(14.dp)
            )


            // =================================================
            // TEXTO
            // =================================================

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Dentro de la zona segura",

                    color = Blue700,

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold
                )


                Text(
                    text = "A 180 m de casa · Sin alertas activas",

                    color = TextSecondary,

                    style = MaterialTheme.typography.bodyMedium
                )
            }


            // =================================================
            // ESTADO
            // =================================================

            Surface(
                shape = RoundedCornerShape(12.dp),

                color = Color(0xFFE5F8EF),

                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFB9E9D0)
                )
            ) {

                Text(
                    text = "Normal",

                    modifier = Modifier.padding(
                        horizontal = 13.dp,
                        vertical = 8.dp
                    ),

                    color = Color(0xFF15945C),

                    style = MaterialTheme.typography.bodyMedium,

                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Preview(
    showBackground = true,
    name = "Estado zona segura"
)
@Composable
private fun SafetyStatusCardPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            SafetyStatusCard(
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}