package com.icm2630.proyecto.ui.components.medication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue50
import com.icm2630.proyecto.ui.theme.Blue500
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextSecondary


@Composable
fun MedicationDoseSelector(
    selectedForm: String,
    dose: String,
    unit: String,
    quantityPerDose: String,
    onDoseChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onQuantityPerDoseChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    // SABER SI ESTÁ USANDO "OTRO"
    var otroSeleccionado by remember {

        mutableStateOf(
            quantityPerDose.isNotBlank() &&
                    quantityPerDose != "1" &&
                    quantityPerDose != "2"
        )
    }


    /*
     * Mantiene sincronizado el selector cuando el valor
     * cambia desde el ViewModel, por ejemplo al editar.
     */
    LaunchedEffect(quantityPerDose) {

        when {

            quantityPerDose == "1" ||
                    quantityPerDose == "2" -> {

                otroSeleccionado = false
            }


            quantityPerDose.isNotBlank() -> {

                otroSeleccionado = true
            }
        }
    }


    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Text(
            text = "Dosis por toma",
            color = Blue700,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )


        Spacer(
            modifier = Modifier.height(4.dp)
        )


        Text(
            text = getDoseDescription(selectedForm),
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )


        Spacer(
            modifier = Modifier.height(14.dp)
        )

        // CANTIDAD + UNIDAD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            OutlinedTextField(
                value = dose,

                onValueChange = { newValue ->

                    /*
                     * Únicamente números
                     * y un separador decimal.
                     */
                    val valid =
                        newValue.matches(
                            Regex("""^\d*[.,]?\d*$""")
                        )


                    if (valid) {
                        onDoseChange(newValue)
                    }
                },

                modifier = Modifier.weight(1f),

                label = {
                    Text("Cantidad")
                },

                placeholder = {
                    Text(
                        getDoseExample(selectedForm)
                    )
                },

                singleLine = true,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),

                shape = RoundedCornerShape(16.dp),

                textStyle =
                    MaterialTheme.typography.bodyLarge
            )


            MedicationUnitSelector(
                selectedForm = selectedForm,
                selectedUnit = unit,
                onUnitSelected = onUnitChange,
                modifier = Modifier.weight(0.75f)
            )
        }

        // PASTILLAS POR TOMA
        if (selectedForm == "Pastilla") {

            Spacer(
                modifier = Modifier.height(20.dp)
            )


            Text(
                text = "Cantidad por toma",
                color = Blue700,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )


            Spacer(
                modifier = Modifier.height(4.dp)
            )


            Text(
                text = "¿Cuántas pastillas debe tomar en cada horario?",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )


            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // 1 / 2 / OTRO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                QuantityOption(
                    value = "1",
                    label = "pastilla",

                    selected =
                        !otroSeleccionado &&
                                quantityPerDose == "1",

                    modifier =
                        Modifier.weight(1f),

                    onClick = {

                        otroSeleccionado = false

                        onQuantityPerDoseChange(
                            "1"
                        )
                    }
                )


                QuantityOption(
                    value = "2",
                    label = "pastillas",

                    selected =
                        !otroSeleccionado &&
                                quantityPerDose == "2",

                    modifier =
                        Modifier.weight(1f),

                    onClick = {

                        otroSeleccionado = false

                        onQuantityPerDoseChange(
                            "2"
                        )
                    }
                )


                QuantityOption(
                    value = "Otro",
                    label = "cantidad",

                    selected =
                        otroSeleccionado,

                    modifier =
                        Modifier.weight(1f),

                    onClick = {

                        otroSeleccionado = true


                        /*
                         * Si antes tenía seleccionada
                         * la opción 1 o 2, limpiamos el
                         * valor para que escriba otro.
                         */
                        if (
                            quantityPerDose == "1" ||
                            quantityPerDose == "2"
                        ) {

                            onQuantityPerDoseChange(
                                ""
                            )
                        }
                    }
                )
            }

            // CANTIDAD PERSONALIZADA
            if (otroSeleccionado) {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )


                OutlinedTextField(
                    value =
                        if (
                            quantityPerDose == "1" ||
                            quantityPerDose == "2"
                        ) {
                            ""
                        } else {
                            quantityPerDose
                        },

                    onValueChange = { newValue ->

                        val valid =
                            newValue.matches(
                                Regex("""^\d*[.,]?\d*$""")
                            )


                        if (valid) {

                            onQuantityPerDoseChange(
                                newValue
                            )
                        }
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "Otra cantidad"
                        )
                    },

                    placeholder = {
                        Text(
                            "Ej. 3"
                        )
                    },

                    supportingText = {

                        Text(
                            "Ingresa cuántas pastillas debe tomar."
                        )
                    },

                    singleLine = true,

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Decimal
                        ),

                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // AYUDA SEGÚN PRESENTACIÓN
        if (selectedForm != "Pastilla") {

            Spacer(
                modifier = Modifier.height(16.dp)
            )


            Surface(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

                color = Blue50,

                border = BorderStroke(
                    width = 1.dp,
                    color = Blue100
                )
            ) {

                Text(
                    text = getDoseHelpExample(
                            selectedForm
                        ),

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

// SELECTOR DE UNIDAD
@Composable
private fun MedicationUnitSelector(selectedForm: String,
    selectedUnit: String, onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    val units = getUnitsForForm(selectedForm)

    Box(
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable {
                    expanded = true
                },
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border =
                BorderStroke(
                    width = 1.dp,
                    color = Blue500
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 13.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text = "Unidad",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Text(
                        text = selectedUnit,
                        color = Blue700,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Seleccionar unidad",
                    tint = Blue500
                )
            }
        }

        DropdownMenu(
            expanded = expanded,

            onDismissRequest = {
                expanded = false
            }
        ) {

            units.forEach { unit ->

                DropdownMenuItem(

                    text = {

                        Text(
                            text = unit,
                            style =
                                MaterialTheme.typography.bodyLarge
                        )
                    },

                    onClick = {

                        onUnitSelected(
                            unit
                        )

                        expanded = false
                    }
                )
            }
        }
    }
}

// OPCIÓN DE CANTIDAD
@Composable
private fun QuantityOption(value: String, label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(16.dp),

        color =
            if (selected) {
                Blue100
            } else {
                Color.White
            },

        border =
            BorderStroke(

                width =
                    if (selected) {
                        1.5.dp
                    } else {
                        1.dp
                    },

                color =
                    if (selected) {
                        Blue500
                    } else {
                        Blue100
                    }
            )
    ) {

        Column(
            modifier = Modifier.padding(
                vertical = 12.dp
            ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = value,
                color = Blue700,
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight =
                    FontWeight.Bold
            )


            Text(
                text = label,
                color = TextSecondary,
                style =
                    MaterialTheme.typography.bodySmall
            )
        }
    }
}

// DATOS VISUALES
private fun getUnitsForForm(selectedForm: String): List<String> {

    return when (selectedForm) {
        "Jarabe" -> listOf("mL", "mg")

        "Gotas" -> listOf("gotas", "mL")

        "Inyección" ->
            listOf("mL", "mg", "UI")

        else ->
            listOf("mg", "g", "mcg")
    }
}


private fun getDoseDescription(selectedForm: String): String {
    return when (selectedForm) {
        "Jarabe" ->
            "Indica la cantidad de líquido que debe tomar."

        "Gotas" ->
            "Indica la cantidad de gotas de cada aplicación."

        "Inyección" ->
            "Indica la dosis correspondiente a cada aplicación."

        else -> "Indica la concentración del medicamento."
    }
}


private fun getDoseExample(selectedForm: String): String {
    return when (selectedForm) {
        "Jarabe" -> "Ej. 10"
        "Gotas" -> "Ej. 5"
        "Inyección" -> "Ej. 2"
        else -> "Ej. 50"
    }
}


private fun getDoseHelpExample(selectedForm: String): String {
    return when (selectedForm) {
        "Jarabe" -> "Ejemplo: 10 mL en cada toma."

        "Gotas" -> "Ejemplo: 5 gotas en cada aplicación."

        "Inyección" -> "Ejemplo: 2 mL en cada aplicación."

        else -> ""
    }
}

// PREVIEW
@Preview(showBackground = true, name = "Dosis medicamento")
@Composable
private fun MedicationDoseSelectorPreview() {

    HealthControlTheme {
        Surface(
            color = Blue50
        ) {
            MedicationDoseSelector(
                selectedForm = "Pastilla",
                dose = "50",
                unit = "mg",
                quantityPerDose = "1",
                onDoseChange = {},
                onUnitChange = {},
                onQuantityPerDoseChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}