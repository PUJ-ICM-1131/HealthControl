package com.icm2630.proyecto.ui.util

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.*

/** Sesión 6: estado Accompanist, autorización, rationale y solicitud desde un evento. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val result by rememberUpdatedState(onResult)
    var rationale by remember { mutableStateOf(false) }
    var ajustes by remember { mutableStateOf(false) }
    val permisos = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    ) { respuesta ->
        val concedido = respuesta.values.any { it }
        result(concedido)
        if (!concedido && context.activity()?.let {
                !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_COARSE_LOCATION)
            } == true) ajustes = true
    }
    PermissionDialog(
        visible = rationale || ajustes,
        titulo = "Acceso a tu ubicación",
        mensaje = "Permite la ubicación para mostrar tu posición en el mapa y calcular rutas. Puedes continuar eligiendo los puntos manualmente.",
        ajustes = ajustes,
        onSolicitar = { permisos.launchMultiplePermissionRequest() },
        onCerrar = { rationale = false; ajustes = false }
    )
    return {
        when {
            permisos.permissions.any { it.status.isGranted } -> result(true)
            permisos.shouldShowRationale -> rationale = true
            else -> permisos.launchMultiplePermissionRequest()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberNotificationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return { onResult(true) }
    val context = LocalContext.current
    val result by rememberUpdatedState(onResult)
    var rationale by remember { mutableStateOf(false) }
    var ajustes by remember { mutableStateOf(false) }
    val permiso = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) { concedido ->
        result(concedido)
        if (!concedido && context.activity()?.let {
                !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.POST_NOTIFICATIONS)
            } == true) ajustes = true
    }
    PermissionDialog(
        visible = rationale || ajustes,
        titulo = "Permiso de notificaciones",
        mensaje = "Permite las notificaciones para recibir avisos de HealthControl. Puedes seguir usando la aplicación si no las autorizas.",
        ajustes = ajustes,
        onSolicitar = { permiso.launchPermissionRequest() },
        onCerrar = { rationale = false; ajustes = false }
    )
    return {
        when {
            permiso.status.isGranted -> result(true)
            permiso.status.shouldShowRationale -> rationale = true
            else -> permiso.launchPermissionRequest()
        }
    }
}

@Composable
private fun PermissionDialog(
    visible: Boolean,
    titulo: String,
    mensaje: String,
    ajustes: Boolean,
    onSolicitar: () -> Unit,
    onCerrar: () -> Unit
) {
    if (!visible) return
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = {
                if (ajustes) context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${context.packageName}")))
                else onSolicitar()
                onCerrar()
            }) { Text(if (ajustes) "Abrir ajustes" else "Continuar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}
