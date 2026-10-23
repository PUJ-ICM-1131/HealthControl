package com.icm2630.proyecto.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Paleta propia de HealthControl: azules del proyecto y verde de salud.
// Se aplica a toda la pantalla del mapa, incluida su barra inferior.
private val MapLightColors = lightColorScheme(
    primary = Blue700, onPrimary = Color.White,
    primaryContainer = Blue100, onPrimaryContainer = Color(0xFF123D6B),
    secondary = Blue700, onSecondary = Color.White,
    secondaryContainer = Blue50, onSecondaryContainer = Blue700,
    tertiary = SuccessGreen, onTertiary = Color.White,
    tertiaryContainer = SuccessGreenBg, onTertiaryContainer = Color(0xFF115237),
    background = Color(0xFFF8FAFF), onBackground = TextPrimary,
    surface = Color.White, onSurface = TextPrimary,
    surfaceVariant = Blue50, onSurfaceVariant = Color(0xFF4F6176),
    surfaceContainer = Color(0xFFF0F6FC), surfaceContainerLow = Color(0xFFF6F9FE),
    surfaceContainerHigh = Color(0xFFE7F0FA),
    outline = Color(0xFF788BA0), outlineVariant = Color(0xFFCEDDEC),
    error = Color(0xFFB3261E), onError = Color.White,
    errorContainer = ErrorRedBg, onErrorContainer = Color(0xFF7A1914)
)

private val MapDarkColors = darkColorScheme(
    primary = Blue300, onPrimary = Color(0xFF082D4B),
    primaryContainer = Color(0xFF173F64), onPrimaryContainer = Color(0xFFD6ECFF),
    secondary = Blue100, onSecondary = Color(0xFF123D6B),
    secondaryContainer = Color(0xFF203A53), onSecondaryContainer = Color(0xFFD6ECFF),
    tertiary = Color(0xFF82DAB2), onTertiary = Color(0xFF083A27),
    tertiaryContainer = Color(0xFF164E38), onTertiaryContainer = Color(0xFFC4F5DD),
    background = Color(0xFF0C1624), onBackground = Color(0xFFE5EFFA),
    surface = Color(0xFF142337), onSurface = Color(0xFFE5EFFA),
    surfaceVariant = Color(0xFF22384E), onSurfaceVariant = Color(0xFFBBCBDC),
    surfaceContainer = Color(0xFF182B41), surfaceContainerLow = Color(0xFF102033),
    surfaceContainerHigh = Color(0xFF23394F),
    outline = Color(0xFF8A9FB5), outlineVariant = Color(0xFF3B536C),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF652525), onErrorContainer = Color(0xFFFFDAD5)
)

@Composable
fun HealthControlMapTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    // Los slots que faltaban en Type.kt también usan Poppins en el mapa.
    val typography = HealthControlTypography.let {
        it.copy(
            headlineSmall = it.headlineSmall.copy(fontFamily = PoppinsFamily),
            titleMedium = it.titleMedium.copy(fontFamily = PoppinsFamily),
            titleSmall = it.titleSmall.copy(fontFamily = PoppinsFamily),
            bodyMedium = it.bodyMedium.copy(fontFamily = PoppinsFamily),
            bodySmall = it.bodySmall.copy(fontFamily = PoppinsFamily),
            labelMedium = it.labelMedium.copy(fontFamily = PoppinsFamily),
            labelSmall = it.labelSmall.copy(fontFamily = PoppinsFamily)
        )
    }
    MaterialTheme(
        colorScheme = if (darkTheme) MapDarkColors else MapLightColors,
        typography = typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(22.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}