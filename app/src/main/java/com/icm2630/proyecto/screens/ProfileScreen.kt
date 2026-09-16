package com.icm2630.proyecto.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icm2630.proyecto.components.HealthBottomNavigation
import com.icm2630.proyecto.model.PerfilUsuario
import com.icm2630.proyecto.model.TipoPerfil
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue500
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.ErrorRed
import com.icm2630.proyecto.ui.theme.ErrorRedBg
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextPrimary
import com.icm2630.proyecto.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val Fondo = Color(0xFFF8FAFF)
private val CardShape = RoundedCornerShape(24.dp)
private val HairLine = Color(0xFFE5E7EB)

/**
 * Identifica qué campo del perfil se quiere editar. Por ahora ningún
 * valor dispara lógica real: sirve para que, cuando se conecte la
 * edición de verdad, cada fila ya sepa qué evento reportar sin tener
 * que tocar el layout de la pantalla.
 */
enum class CampoPerfilEditable {
    FOTO, NOMBRE, FECHA_NACIMIENTO, GENERO, CONTACTO, CONTACTO_EMERGENCIA, CONDICION_MEDICA
}

@Composable
fun ProfileScreen(
    perfil: PerfilUsuario = PerfilUsuario(),
    onNavigate: (Routes) -> Unit = {},
    onEditarCampo: (CampoPerfilEditable) -> Unit = {},
    onCambiarTipoPerfil: (TipoPerfil) -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    var mostrarConfirmacionCierre by remember { mutableStateOf(false) }

    // Estado visual local para que la selección responda al tacto ya
    // mismo; la fuente de verdad real se conectará más adelante.
    var tipoSeleccionado by remember(perfil.tipoPerfil) { mutableStateOf(perfil.tipoPerfil) }

    Scaffold(
        bottomBar = {
            HealthBottomNavigation(
                currentRoute = Routes.Perfil,
                onNavigate = onNavigate
            )
        },
        containerColor = Fondo
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Blue700
            )

            Spacer(Modifier.height(20.dp))

            // ---------- Encabezado con avatar ----------
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarPerfil(onEditarFoto = { onEditarCampo(CampoPerfilEditable.FOTO) })

                Spacer(Modifier.height(12.dp))

                Text(
                    text = perfil.nombreCompleto.ifBlank { "Sin especificar" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Blue700
                )
                Text(
                    text = tipoSeleccionado.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.height(28.dp))

            // ---------- Datos personales ----------
            SeccionTitulo("Datos personales")
            Spacer(Modifier.height(12.dp))
            TarjetaBlanca {
                FilaDato(
                    icon = Icons.Outlined.Person,
                    label = "Nombre completo",
                    valor = perfil.nombreCompleto.ifBlank { "Sin especificar" },
                    onEditar = { onEditarCampo(CampoPerfilEditable.NOMBRE) }
                )
                DivisorFila()
                FilaDato(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "Fecha de nacimiento",
                    valor = formatearFecha(perfil.fechaNacimientoMillis),
                    onEditar = { onEditarCampo(CampoPerfilEditable.FECHA_NACIMIENTO) }
                )
                DivisorFila()
                FilaDato(
                    icon = Icons.Outlined.Info,
                    label = "Género",
                    valor = perfil.genero?.etiqueta ?: "Sin especificar",
                    onEditar = { onEditarCampo(CampoPerfilEditable.GENERO) }
                )
                DivisorFila()
                FilaDato(
                    icon = Icons.Outlined.MailOutline,
                    label = "Teléfono o correo",
                    valor = perfil.contacto.ifBlank { "Sin especificar" },
                    onEditar = { onEditarCampo(CampoPerfilEditable.CONTACTO) }
                )
                DivisorFila()
                FilaDato(
                    icon = Icons.Outlined.Shield,
                    label = "Contacto de emergencia",
                    valor = perfil.contactoEmergencia.ifBlank { "Sin especificar" },
                    onEditar = { onEditarCampo(CampoPerfilEditable.CONTACTO_EMERGENCIA) }
                )
                DivisorFila()
                FilaDato(
                    icon = Icons.Outlined.MedicalServices,
                    label = "Condición médica relevante",
                    valor = perfil.condicionRelevante.ifBlank { "Sin especificar" },
                    onEditar = { onEditarCampo(CampoPerfilEditable.CONDICION_MEDICA) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // ---------- Tipo de perfil (HU-02) ----------
            SeccionTitulo("Tipo de perfil")
            Spacer(Modifier.height(12.dp))
            TarjetaBlanca {
                Text(
                    text = "Define si usas HealthControl para ti o para acompañar a alguien más.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SelectorTipoPerfil(
                        tipo = TipoPerfil.INDIVIDUAL,
                        icon = Icons.Outlined.FavoriteBorder,
                        seleccionado = tipoSeleccionado == TipoPerfil.INDIVIDUAL,
                        onClick = {
                            tipoSeleccionado = TipoPerfil.INDIVIDUAL
                            onCambiarTipoPerfil(TipoPerfil.INDIVIDUAL)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SelectorTipoPerfil(
                        tipo = TipoPerfil.ASOCIADO,
                        icon = Icons.Outlined.Groups,
                        seleccionado = tipoSeleccionado == TipoPerfil.ASOCIADO,
                        onClick = {
                            tipoSeleccionado = TipoPerfil.ASOCIADO
                            onCambiarTipoPerfil(TipoPerfil.ASOCIADO)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---------- Cerrar sesión ----------
            TarjetaBlanca {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarConfirmacionCierre = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = ErrorRedBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Cerrar sesión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Outlined.ChevronRight,
                        null,
                        tint = ErrorRed.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (mostrarConfirmacionCierre) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionCierre = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tendrás que volver a iniciar sesión para acceder a tu cuenta.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacionCierre = false
                        onCerrarSesion()
                    }
                ) {
                    Text("Cerrar sesión", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionCierre = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

/* ---------------------------------------------------------------- */
/*  Componentes                                                      */
/* ---------------------------------------------------------------- */

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Blue700,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun TarjetaBlanca(contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, CardShape)
            .padding(20.dp),
        content = contenido
    )
}

@Composable
private fun DivisorFila() {
    HorizontalDivider(
        color = HairLine,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun AvatarPerfil(onEditarFoto: () -> Unit) {
    Box(modifier = Modifier.size(96.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Blue100.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = "Foto de perfil",
                tint = Blue700,
                modifier = Modifier.size(46.dp)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp)
                .background(Blue700, CircleShape)
                .clickable(onClick = onEditarFoto),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.PhotoCamera,
                contentDescription = "Cambiar foto",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun FilaDato(
    icon: ImageVector,
    label: String,
    valor: String,
    onEditar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Blue100.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Blue500, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(Modifier.height(2.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
        IconButton(onClick = onEditar) {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = "Editar $label",
                tint = Blue500,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SelectorTipoPerfil(
    tipo: TipoPerfil,
    icon: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fondo = if (seleccionado) Blue700 else Blue100.copy(alpha = 0.25f)
    val colorTitulo = if (seleccionado) Color.White else Blue700
    val colorTexto = if (seleccionado) Color.White.copy(alpha = 0.85f) else TextSecondary

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(fondo)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = colorTitulo, modifier = Modifier.size(22.dp))
            if (seleccionado) {
                Icon(Icons.Outlined.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = tipo.titulo,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colorTitulo
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = tipo.descripcion,
            style = MaterialTheme.typography.bodySmall,
            color = colorTexto,
            lineHeight = 14.sp
        )
    }
}

private fun formatearFecha(millis: Long?): String {
    if (millis == null) return "Sin especificar"
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formato.format(millis)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    HealthControlTheme {
        ProfileScreen(
            perfil = PerfilUsuario(
                nombreCompleto = "Juan Pérez",
                contacto = "juan.perez@example.com",
                configurado = true
            )
        )
    }
}
