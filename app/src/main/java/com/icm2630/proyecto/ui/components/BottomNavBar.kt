package com.icm2630.proyecto.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.TextSecondary

/**
 * [rutaInicio] es a qué destino lleva y qué resalta la pestaña "Inicio".
 * Un titular usa [Routes.Home]; un acompañante usa [Routes.Monitoreo], que
 * es su propia home (HU-25) pero comparte esta misma barra inferior.
 */
@Composable
fun HealthBottomNavigation(
    currentRoute: Routes,
    onNavigate: (Routes) -> Unit,
    rutaInicio: Routes = Routes.Home
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem("Inicio", Icons.Outlined.Home, rutaInicio),
            BottomNavItem("Pendientes", Icons.Outlined.StickyNote2, Routes.Recordatorios),
            BottomNavItem("Registrar", Icons.Outlined.Add, Routes.Registrar),
            BottomNavItem("Historial", Icons.Outlined.Schedule, Routes.Historial),
            BottomNavItem("Mapa", Icons.Outlined.Map, Routes.Mapa)
        )

        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) Blue700 else TextSecondary
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) Blue700 else TextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            )
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Routes
)