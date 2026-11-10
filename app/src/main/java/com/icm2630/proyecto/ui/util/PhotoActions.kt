package com.icm2630.proyecto.ui.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.*
import java.io.File

data class PhotoActions(val camara: () -> Unit, val galeria: () -> Unit)

internal tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

/** Sesión 8: foto completa mediante FileProvider y selección sin permisos de almacenamiento. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberPhotoActions(onSelected: (Uri) -> Unit): PhotoActions {
    val context = LocalContext.current
    val selected by rememberUpdatedState(onSelected)
    var pendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var mostrarRationale by remember { mutableStateOf(false) }
    var mostrarAjustes by remember { mutableStateOf(false) }

    fun fallo(mensaje: String) = Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()

    val captura = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = pendiente?.let(Uri::parse)
        pendiente = null
        if (ok && uri != null) selected(uri)
    }
    val tomarFoto: () -> Unit = {
        try {
            val carpeta = File(context.filesDir, "imagenes").apply { mkdirs() }
            val archivo = File.createTempFile("captura_", ".jpg", carpeta)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
            pendiente = uri.toString()
            captura.launch(uri)
        } catch (e: android.content.ActivityNotFoundException) {
            pendiente = null
            fallo("No hay una aplicación de cámara disponible.")
        } catch (e: SecurityException) {
            pendiente = null
            fallo("No se pudo acceder a la cámara. Revisa el permiso en Ajustes.")
        } catch (e: java.io.IOException) {
            pendiente = null
            fallo("No se pudo crear el archivo de la foto.")
        }
    }
    val permiso = rememberPermissionState(Manifest.permission.CAMERA) { concedido ->
        if (concedido) tomarFoto()
        else if (context.activity()?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
            } == false) mostrarAjustes = true
        else fallo("Puedes continuar sin tomar una foto.")
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // Algunos proveedores solo conceden acceso temporal.
            }
            selected(it)
        }
    }
    if (mostrarRationale || mostrarAjustes) {
        AlertDialog(
            onDismissRequest = { mostrarRationale = false; mostrarAjustes = false },
            title = { Text("Acceso a la cámara") },
            text = { Text("HealthControl necesita la cámara para tomar la foto que quieres adjuntar. También puedes elegir una imagen de la galería.") },
            confirmButton = {
                TextButton(onClick = {
                    if (mostrarAjustes) {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")))
                    } else permiso.launchPermissionRequest()
                    mostrarRationale = false
                    mostrarAjustes = false
                }) { Text(if (mostrarAjustes) "Abrir ajustes" else "Continuar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarRationale = false; mostrarAjustes = false }) { Text("Cancelar") }
            }
        )
    }
    return PhotoActions(
        camara = {
            when {
                permiso.status.isGranted -> tomarFoto()
                permiso.status.shouldShowRationale -> mostrarRationale = true
                else -> permiso.launchPermissionRequest()
            }
        },
        galeria = { galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    )
}
