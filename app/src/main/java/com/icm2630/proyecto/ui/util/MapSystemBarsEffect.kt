package com.icm2630.proyecto.ui.util

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Mantiene legibles los iconos del sistema cuando la poca luz oscurece la pantalla. */
@Composable
fun MapSystemBarsEffect(oscuro: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, oscuro) {
        val activity = generateSequence(view.context) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<Activity>().firstOrNull()
        val controller = activity?.window?.let { WindowCompat.getInsetsController(it, view) }
        val statusAnterior = controller?.isAppearanceLightStatusBars
        val navigationAnterior = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = !oscuro
        controller?.isAppearanceLightNavigationBars = !oscuro
        onDispose {
            if (statusAnterior != null) controller.isAppearanceLightStatusBars = statusAnterior
            if (navigationAnterior != null) controller.isAppearanceLightNavigationBars = navigationAnterior
        }
    }
}