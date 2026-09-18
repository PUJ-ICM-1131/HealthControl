package com.icm2630.proyecto.ui.components.medication

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
fun MedicationSectionCard(
    step: Int,
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(22.dp),

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
                verticalAlignment = Alignment.Top
            ) {

                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Blue100
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = step.toString(),
                            color = Blue700,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
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
                        color = Blue700,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )


                    if (!subtitle.isNullOrBlank()) {

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )


                        Text(
                            text = subtitle,
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            content()
        }
    }
}


@Preview(
    showBackground = true,
    name = "Sección medicamento"
)
@Composable
private fun MedicationSectionCardPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            MedicationSectionCard(
                step = 1,
                title = "Medicamento",
                subtitle = "Indica cuál medicamento deseas registrar",
            ) {

                Text(
                    text = "Aquí irán los campos del medicamento."
                )
            }
        }
    }
}