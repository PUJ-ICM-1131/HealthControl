package com.icm2630.proyecto.ui.util

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.icm2630.proyecto.data.model.FechasPendientes
import kotlinx.coroutines.delay

/** Actualiza al volver a la pantalla y al cambiar de día sin volver a abrir la app. */
@Composable
fun rememberHoyUtcMillis(): Long {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var hoy by remember { mutableLongStateOf(FechasPendientes.hoy()) }
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                hoy = FechasPendientes.hoy()
                delay(30_000L)
            }
        }
    }
    return hoy
}
