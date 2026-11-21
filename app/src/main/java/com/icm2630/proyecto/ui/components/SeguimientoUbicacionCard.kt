package com.icm2630.proyecto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.service.SeguimientoUbicacion
import com.icm2630.proyecto.ui.util.rememberLocationPermissionRequest
import com.icm2630.proyecto.ui.util.rememberNotificationPermissionRequest

/** La interacción visible inicia el servicio; no se reinicia silenciosamente al abrir la app. */
@Composable
fun SeguimientoUbicacionCard(
    perfil: PerfilUsuario,
    guardando: Boolean,
    errorGuardado: String?,
    onGuardar: (PerfilUsuario) -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val estado by SeguimientoUbicacion.estado.collectAsStateWithLifecycle()
    var pendiente by rememberSaveable { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val activo = estado.uid != null
    val notificaciones = rememberNotificationPermissionRequest { concedido ->
        if (concedido) {
            pendiente = true
            if (!perfil.compartirUbicacion) onGuardar(perfil.copy(compartirUbicacion = true))
        } else error = "Permite las notificaciones para mantener visible el aviso de ubicación compartida."
    }
    val ubicacion = rememberLocationPermissionRequest { concedido ->
        if (concedido) notificaciones()
        else error = "Necesitas permitir la ubicación para compartirla."
    }
    LaunchedEffect(pendiente, perfil.compartirUbicacion, guardando, errorGuardado) {
        if (pendiente && errorGuardado != null) pendiente = false
        if (pendiente && perfil.compartirUbicacion && !guardando) {
            pendiente = false
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                SeguimientoUbicacion.iniciar(context)
            } else error = "Vuelve a pulsar Iniciar con la app abierta."
        }
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Compartir mi ubicación", style = MaterialTheme.typography.titleSmall)
        Text(
            if (activo) "Seguimiento activo. Continúa al minimizar la app o bloquear la pantalla."
            else "Comparte tu posición con tus personas vinculadas, incluso al salir del mapa. Se mostrará una notificación permanente.",
            style = MaterialTheme.typography.bodySmall
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando && !pendiente,
            onClick = {
                error = null
                if (activo) {
                    SeguimientoUbicacion.detener(context)
                    onGuardar(perfil.copy(compartirUbicacion = false))
                } else ubicacion()
            }
        ) {
            Text(if (guardando || pendiente) "Preparando…" else if (activo) "Dejar de compartir" else "Iniciar seguimiento")
        }
        if (!activo && perfil.compartirUbicacion) {
            TextButton(
                enabled = !guardando,
                onClick = { pendiente = false; onGuardar(perfil.copy(compartirUbicacion = false)) }
            ) { Text("Retirar permiso de compartir") }
        }
        (error ?: estado.error)?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
