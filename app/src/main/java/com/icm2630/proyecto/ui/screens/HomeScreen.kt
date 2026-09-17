package com.icm2630.proyecto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icm2630.proyecto.R
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.theme.*
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
@Composable
fun HomeScreen(
    onNavigate: (Routes) -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            HealthBottomNavigation(
                currentRoute = Routes.Home,
                onNavigate = onNavigate
            )
        },
        containerColor = Color(0xFFF8FAFF)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoSmall()
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Health Control",
                        style = MaterialTheme.typography.titleLarge,
                        color = Blue700,
                        fontWeight = FontWeight.Bold
                    )
                }

                PerfilMenu(onCerrarSesion = onCerrarSesion)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Hola, María",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Blue700
            )

            Spacer(Modifier.height(16.dp))

            // Chips de perfiles
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileChip(label = "Mi Salud", icon = Icons.Outlined.Person, isSelected = true)
                ProfileChip(label = "Mamá Elena", icon = null, isSelected = false, letter = "E")
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Blue700
            )

            Spacer(Modifier.height(16.dp))

            // Grid de Acciones Rápidas
            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionCard(
                    icon = Icons.Outlined.Medication,
                    title = "Registrar Dosis",
                    subtitle = "Toma rápida",
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                QuickActionCard(
                    icon = Icons.Outlined.AddBox,
                    title = "Nueva Cita",
                    subtitle = "Agendar visita",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionCard(
                    icon = Icons.Outlined.Timeline,
                    title = "Medir Signos",
                    subtitle = "Presión / Pulso",
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                QuickActionCard(
                    icon = Icons.Outlined.Group,
                    title = "Asociar Pers...",
                    subtitle = "Familia / Tutor",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Próximos Recordatorios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Blue700
                )
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "3 pendientes",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Card Recordatorio Medicamento
            MedicationReminderCard()

            Spacer(Modifier.height(16.dp))

            // Card Cita Médica
            AppointmentReminderCard()
        }
    }
}

/**
 * Ícono de perfil con menú desplegable. "Cerrar sesión" no ejecuta
 * directamente: primero pide confirmación con un AlertDialog, porque
 * es una acción destructiva de un solo toque y fácil de disparar
 * sin querer.
 */
@Composable
private fun PerfilMenu(onCerrarSesion: () -> Unit) {
    var menuAbierto by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(Blue100)
                .clickable { menuAbierto = true }
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = "Menú de perfil",
                modifier = Modifier.align(Alignment.Center),
                tint = Blue700
            )
        }

        DropdownMenu(
            expanded = menuAbierto,
            onDismissRequest = { menuAbierto = false }
        ) {
            DropdownMenuItem(
                text = { Text("Cerrar sesión", color = Color(0xFFEF4444)) },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Logout,
                        contentDescription = null,
                        tint = Color(0xFFEF4444)
                    )
                },
                onClick = {
                    menuAbierto = false
                    mostrarConfirmacion = true
                }
            )
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tendrás que volver a iniciar sesión para acceder a tu cuenta.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        onCerrarSesion()
                    }
                ) {
                    Text("Cerrar sesión", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun LogoSmall() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Blue500,
                spotColor = Blue500
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, Blue100, RoundedCornerShape(18.dp))
    ) {
        Image(
            painter = painterResource(R.drawable.loguitouwu),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ProfileChip(label: String, icon: ImageVector?, isSelected: Boolean, letter: String? = null) {
    Surface(
        color = if (isSelected) Color.White else Blue100.copy(alpha = 0.5f),
        shape = RoundedCornerShape(50),
        border = if (isSelected) BorderStroke(1.dp, Blue100) else null,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(18.dp), tint = Blue500)
            } else if (letter != null) {
                Surface(
                    color = Blue500,
                    shape = CircleShape,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(letter, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (isSelected) Blue700 else TextSecondary)
            if (!isSelected) {
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Filled.Circle, null, modifier = Modifier.size(8.dp), tint = Blue500)
            }
        }
    }
}

@Composable
private fun QuickActionCard(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(90.dp),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Blue100.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Blue500, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Blue700)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun MedicationReminderCard() {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Blue100.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Medication, null, tint = Blue500, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Losartán 50mg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Blue700)
                        Spacer(Modifier.width(8.dp))
                        Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(4.dp)) {
                            Text("Hace 10 min", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color.Red)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Blue100.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Group, null, modifier = Modifier.size(14.dp), tint = Blue500)
                                Spacer(Modifier.width(4.dp))
                                Text("Para: Mamá Elena", fontSize = 12.sp, color = Blue700)
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text("08:00 AM", fontSize = 14.sp, color = Blue700, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue700)
            ) {
                Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Marcar como tomada")
            }
        }
    }
}

@Composable
private fun AppointmentReminderCard() {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Blue100.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.MedicalServices, null, tint = Blue500, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("CITA MÉDICA", style = MaterialTheme.typography.labelSmall, color = Blue500, fontWeight = FontWeight.Bold)
                    Text("Dr. Andrés Valenzuela", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Blue700)
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(18.dp), tint = Blue500)
                    Spacer(Modifier.width(8.dp))
                    Text("Hoy a las 04:30 PM", style = MaterialTheme.typography.bodyMedium, color = Blue700)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    HealthControlTheme {
        HomeScreen()
    }
}