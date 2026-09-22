package com.icm2630.proyecto.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.PersonaVinculada
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.TextSecondary

/** "tu madre Elena Ramírez" / "tu familiar Lucía Torres". */
fun PersonaVinculada.descripcionConParentesco(): String =
    "tu ${relacion.trim().lowercase().ifBlank { "familiar" }} $nombre"

/**
 * Aviso de que se está consultando el HealthControl de un familiar
 * en solo lectura. Si llega [onVolverAMiPerfil] se muestra el botón
 * para regresar al perfil propio.
 */
@Composable
fun BannerFamiliar(
    persona: PersonaVinculada,
    modifier: Modifier = Modifier,
    onVolverAMiPerfil: (() -> Unit)? = null
) {
    Surface(
        color = Blue100.copy(alpha = 0.35f),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = Blue700,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Estás viendo el perfil de ${persona.descripcionConParentesco()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Blue700
                    )
                    Text(
                        text = "Solo lectura: no puedes modificar su información",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            if (onVolverAMiPerfil != null) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onVolverAMiPerfil,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue700)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Volver a mi perfil")
                    }
                }
            }
        }
    }
}
