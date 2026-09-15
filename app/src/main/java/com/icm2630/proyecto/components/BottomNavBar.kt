package com.icm2630.proyecto.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.TextSecondary

@Composable
fun HealthBottomNavigation(
    currentRoute: Routes,
    onNavigate: (Routes) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem("Inicio", Icons.Outlined.Home, Routes.Home),
            BottomNavItem("Recordatorios", Icons.Outlined.Notifications, Routes.Recordatorios),
            BottomNavItem("Registrar", Icons.Outlined.Add, Routes.Registrar),
            BottomNavItem("Historial", Icons.Outlined.Schedule, Routes.Historial),
            BottomNavItem("Perfil", Icons.Outlined.Person, Routes.Perfil)
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
