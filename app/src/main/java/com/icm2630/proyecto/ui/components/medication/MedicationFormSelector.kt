package com.icm2630.proyecto.ui.components.medication

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
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.icm2630.proyecto.ui.theme.Blue500
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextSecondary


data class MedicationFormOption(
    val name: String,
    val description: String,
    val icon: ImageVector
)


@Composable
fun MedicationFormSelector(
    selectedForm: String,
    onFormSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val forms = listOf(
        MedicationFormOption(
            name = "Pastilla",
            description = "Tableta o cápsula",
            icon = Icons.Outlined.Medication
        ),
        MedicationFormOption(
            name = "Jarabe",
            description = "Medicamento líquido",
            icon = Icons.Outlined.LocalDrink
        ),
        MedicationFormOption(
            name = "Gotas",
            description = "Aplicación por gotas",
            icon = Icons.Outlined.WaterDrop
        ),
        MedicationFormOption(
            name = "Inyección",
            description = "Dosis inyectable",
            icon = Icons.Outlined.Vaccines
        )
    )


    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        forms
            .chunked(2)
            .forEach { rowForms ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    rowForms.forEach { form ->

                        val selected =
                            selectedForm == form.name


                        MedicationFormCard(
                            option = form,
                            selected = selected,
                            onClick = {
                                onFormSelected(form.name)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }


                    if (rowForms.size == 1) {

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
    }
}


@Composable
private fun MedicationFormCard(
    option: MedicationFormOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier
            .height(125.dp)
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
            width = 1.5.dp,

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

            Icon(
                imageVector = option.icon,
                contentDescription = null,

                tint =
                    if (selected) {
                        Color.White
                    } else {
                        Blue500
                    }
            )


            Spacer(
                modifier = Modifier.height(10.dp)
            )


            Text(
                text = option.name,

                color =
                    if (selected) {
                        Color.White
                    } else {
                        Blue700
                    },

                style = MaterialTheme.typography.titleSmall,

                fontWeight = FontWeight.Bold
            )


            Spacer(
                modifier = Modifier.height(3.dp)
            )


            Text(
                text = option.description,

                color =
                    if (selected) {
                        Color.White.copy(alpha = 0.85f)
                    } else {
                        TextSecondary
                    },

                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Preview(
    showBackground = true,
    name = "Forma medicamento"
)
@Composable
private fun MedicationFormSelectorPreview() {

    HealthControlTheme {

        Surface(
            color = Blue50
        ) {

            MedicationFormSelector(
                selectedForm = "Pastilla",
                onFormSelected = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}