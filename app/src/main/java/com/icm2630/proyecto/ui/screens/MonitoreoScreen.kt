package com.icm2630.proyecto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icm2630.proyecto.R
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.PersonaVinculada
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.ui.theme.*

/**
 * HU-25: home del rol Acompañante. En vez de la propia salud, muestra la
 * de la persona (el titular) a la que este acompañante da seguimiento.
 */
@Composable
fun MonitoreoScreen(
    perfil: PerfilUsuario = PerfilUsuario(),
    onNavigate: (Routes) -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    onOpenMap: () -> Unit = {}
) {
    val vinculo = perfil.personaVinculada

    Scaffold(
        bottomBar = {
            HealthBottomNavigation(
                currentRoute = Routes.Monitoreo,
                onNavigate = onNavigate,
                rutaInicio = Routes.Monitoreo
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoMonitoreo()
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Health Control",
                        style = MaterialTheme.typography.titleLarge,
                        color = Blue700,
                        fontWeight = FontWeight.Bold
                    )
                }
                MenuAcompanante(onCerrarSesion = onCerrarSesion)
            }

            Spacer(Modifier.height(24.dp))

            if (vinculo == null) {
                EstadoSinVinculo(onIrAPerfil = { onNavigate(Routes.Perfil) })
            } else {
                ContenidoMonitoreo(vinculo = vinculo, onOpenMap = onOpenMap)
            }
        }
    }
}

@Composable
private fun ContenidoMonitoreo(vinculo: PersonaVinculada, onOpenMap: () -> Unit) {
    Surface(
        color = Blue100.copy(alpha = 0.3f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Groups, null, modifier = Modifier.size(16.dp), tint = Blue500)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Estás acompañando como ${vinculo.relacion.ifBlank { "acompañante" }}",
                style = MaterialTheme.typography.labelSmall,
                color = Blue700
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    Text(
        text = "Cuidando a ${vinculo.nombre}",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Blue700
    )

    Spacer(Modifier.height(32.dp))

    Text(
        text = "Acciones Rápidas",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Blue700
    )

    Spacer(Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth()) {
        AccionMonitoreo(
            icon = Icons.Outlined.Medication,
            title = "Registrar dosis",
            subtitle = "Para ${vinculo.nombre}",
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(16.dp))
        AccionMonitoreo(
            icon = Icons.Outlined.AddBox,
            title = "Nueva cita",
            subtitle = "Agendar visita",
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(16.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        AccionMonitoreo(
            icon = Icons.Outlined.Timeline,
            title = "Medir signos",
            subtitle = "Presión / Pulso",
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(16.dp))
        AccionMonitoreo(
            icon = Icons.Outlined.Schedule,
            title = "Ver historial",
            subtitle = "Citas y dosis",
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
        Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(50)) {
            Text(
                text = "2 pendientes",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Red
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    RecordatorioDe(
        icon = Icons.Outlined.Medication,
        titulo = "Losartán 50mg",
        detalle = "08:00 AM · ${vinculo.nombre}"
    )

    Spacer(Modifier.height(16.dp))

    RecordatorioDe(
        icon = Icons.Outlined.MedicalServices,
        titulo = "Cita con Dr. Andrés Valenzuela",
        detalle = "Hoy a las 04:30 PM · ${vinculo.nombre}"
    )

    Spacer(Modifier.height(24.dp))

    TarjetaUbicacion(nombre = vinculo.nombre, onClick = onOpenMap)
}

@Composable
private fun TarjetaUbicacion(nombre: String, onClick: () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Blue100.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.LocationOn, null, tint = Blue500, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Ubicación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Blue700)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Consulta la última ubicación registrada de $nombre",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = "Ver ubicación", tint = Blue700)
        }
    }
}

@Composable
private fun EstadoSinVinculo(onIrAPerfil: () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Groups,
                contentDescription = null,
                tint = Blue500,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Aún no estás vinculado a nadie",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Blue700,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Ingresa el código que te compartió la persona a la que darás seguimiento desde tu perfil.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onIrAPerfil,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Blue700)
            ) {
                Text("Ir a mi perfil")
            }
        }
    }
}

@Composable
private fun AccionMonitoreo(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
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
private fun RecordatorioDe(icon: ImageVector, titulo: String, detalle: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Blue100.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Blue500, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Blue700)
                Spacer(Modifier.height(4.dp))
                Text(detalle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun MenuAcompanante(onCerrarSesion: () -> Unit) {
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

        DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
            DropdownMenuItem(
                text = { Text("Cerrar sesión", color = Color(0xFFEF4444)) },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = Color(0xFFEF4444))
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
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    onCerrarSesion()
                }) { Text("Cerrar sesión", color = Color(0xFFEF4444)) }
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
private fun LogoMonitoreo() {
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

@Preview(showBackground = true)
@Composable
private fun MonitoreoPreview() {
    HealthControlTheme {
        MonitoreoScreen(
            perfil = PerfilUsuario(
                personaVinculada = PersonaVinculada(
                    nombre = "Elena Ramírez",
                    relacion = "Hija",
                    codigo = "7K3PQ9"
                )
            )
        )
    }
}