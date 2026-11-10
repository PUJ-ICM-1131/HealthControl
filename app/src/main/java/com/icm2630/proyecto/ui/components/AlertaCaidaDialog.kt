package com.icm2630.proyecto.ui.components

import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.ErrorRed
import com.icm2630.proyecto.ui.theme.ErrorRedBg
import com.icm2630.proyecto.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun AlertaCaidaDialog(
    telefonoCuidador: String,
    onCerrarAlerta: () -> Unit
) {
    val context = LocalContext.current
    var segundosRestantes by remember { mutableIntStateOf(20) }

    // Sonido de alarma/alerta
    val ringtone = remember(context) {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(context, uri)
        } catch (e: Exception) {
            null
        }
    }

    // Iniciar sonido y cuenta regresiva de 20 segundos
    LaunchedEffect(Unit) {
        ringtone?.play()
        while (segundosRestantes > 0) {
            delay(1000L)
            segundosRestantes--
        }
        // Se abre el marcador; Android requiere que el usuario confirme la llamada.
        ringtone?.stop()
        llamarCuidador(context, telefonoCuidador)
        onCerrarAlerta()
    }

    DisposableEffect(Unit) {
        onDispose {
            ringtone?.stop()
        }
    }

    AlertDialog(
        onDismissRequest = { /* Bloquear cierre accidental */ },
        icon = {
            Surface(
                color = ErrorRedBg,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Alerta de caída",
                        tint = ErrorRed,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "¡Posible caída detectada!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Blue700,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (telefonoCuidador.any { it.isDigit() })
                        "Detectamos un movimiento o impacto brusco. Confirma que estás bien; al finalizar se abrirá el marcador con tu contacto de emergencia."
                    else "Detectamos un movimiento o impacto brusco. No tienes un teléfono de emergencia configurado; puedes añadirlo en tu perfil.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = TextSecondary
                )

                Spacer(Modifier.height(20.dp))

                // Círculo con temporizador de 20 segundos
                Surface(
                    color = ErrorRedBg,
                    shape = CircleShape,
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${segundosRestantes}s",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    enabled = telefonoCuidador.any { it.isDigit() },
                    onClick = {
                        ringtone?.stop()
                        llamarCuidador(context, telefonoCuidador)
                        onCerrarAlerta()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Icon(Icons.Outlined.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Llamar a cuidador", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        ringtone?.stop()
                        onCerrarAlerta()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Estoy bien (Cancelar alarma)", color = Blue700, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {}
    )
}

private fun llamarCuidador(context: Context, numero: String) {
    try {
        val telefonoValido = numero.filter { it.isDigit() || it == '+' }.take(30)
        if (telefonoValido.none { it.isDigit() }) return
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.fromParts("tel", telefonoValido, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback si no se puede abrir el marcador
    }
}
