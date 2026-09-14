package com.icm2630.proyecto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Nota: Material3 no tiene un slot nativo para "éxito", así que
// SuccessGreen / SuccessGreenBg se usan directamente donde se
// necesiten (badges de "Tomado", "Seguro", "Normal"), no van en
// el ColorScheme.
private val HealthControlColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue700,

    secondary = Blue300,
    onSecondary = Blue700,

    background = Blue50,
    onBackground = TextPrimary,

    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = Blue100,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedBg,
    onErrorContainer = ErrorRed
)

@Composable
fun HealthControlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HealthControlColorScheme,
        typography = HealthControlTypography,
        content = content
    )
}
