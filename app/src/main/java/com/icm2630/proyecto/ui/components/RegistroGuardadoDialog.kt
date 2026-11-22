package com.icm2630.proyecto.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.ui.theme.Blue700

@Composable
fun RegistroGuardadoDialog(
    titulo: String,
    mensaje: String,
    onVolver: () -> Unit,
    onRegistrarOtro: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onVolver,
        icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Blue700) },
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                onRegistrarOtro?.let { registrarOtro ->
                    Button(
                        onClick = registrarOtro,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue700)
                    ) {
                        Text("Registrar otro")
                    }
                }
                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text("Volver")
                }
            }
        }
    )
}
