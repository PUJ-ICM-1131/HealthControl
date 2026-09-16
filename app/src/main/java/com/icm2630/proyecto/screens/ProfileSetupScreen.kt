package com.icm2630.proyecto.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.icm2630.proyecto.model.Genero
import com.icm2630.proyecto.model.PerfilUsuario
import com.icm2630.proyecto.model.TipoPerfil
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue500
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextPrimary
import com.icm2630.proyecto.ui.theme.TextSecondary
import com.icm2630.proyecto.viewmodel.PerfilSetupViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val Fondo = Color(0xFFEFF4FA)
private val CampoBg = Color(0xFFEFF5FC)
private val CampoShape = RoundedCornerShape(14.dp)
private val CardShape = RoundedCornerShape(24.dp)
private val PlaceholderGris = Color(0xFF9CA9BC)

@Composable
fun ProfileSetupScreen(
    viewModel: PerfilSetupViewModel = viewModel(),
    onContinuar: (PerfilUsuario) -> Unit = {},
    onPedirPermisoUbicacion: () -> Unit = {},
    onPedirPermisoNotificaciones: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var mostrarDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ---------- Encabezado ----------
            Box(modifier = Modifier.fillMaxWidth()) {
                BotonCircular(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    descripcion = "Volver al inicio de sesión",
                    onClick = onCerrarSesion,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Text(
                    text = "Configura tu perfil",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Blue700,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Selecciona cómo usarás HealthControl",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // ---------- Tarjeta de datos personales ----------
            TarjetaBlanca {
                AvatarConCamara(modifier = Modifier.align(Alignment.CenterHorizontally))

                Spacer(Modifier.height(20.dp))

                CampoPerfil(
                    label = "Nombre completo",
                    value = state.nombre,
                    onValueChange = viewModel::onNombreChange,
                    placeholder = "Juan Pérez"
                )

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CampoFecha(
                        label = "Fecha de nacimiento",
                        millis = state.fechaNacimientoMillis,
                        onClick = { mostrarDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    CampoGenero(
                        genero = state.genero,
                        onSeleccionar = viewModel::onGeneroChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                CampoPerfil(
                    label = "Teléfono o correo",
                    value = state.contacto,
                    onValueChange = viewModel::onContactoChange,
                    placeholder = "juan.perez@example.com",
                    keyboardType = KeyboardType.Email
                )

                Spacer(Modifier.height(16.dp))

                // HU-20: aquí se podría abrir el selector de contactos de la agenda.
                CampoPerfil(
                    label = "Contacto de emergencia (Nombre y relación)",
                    value = state.contactoEmergencia,
                    onValueChange = viewModel::onContactoEmergenciaChange,
                    placeholder = "María Pérez (Hermana)"
                )
            }

            Spacer(Modifier.height(24.dp))

            // ---------- Tipo de perfil (HU-02) ----------
            Text(
                text = "Tipo de perfil",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Blue700,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaTipoPerfil(
                    tipo = TipoPerfil.INDIVIDUAL,
                    icon = Icons.Outlined.FavoriteBorder,
                    seleccionado = state.tipoPerfil == TipoPerfil.INDIVIDUAL,
                    onClick = { viewModel.onTipoPerfilChange(TipoPerfil.INDIVIDUAL) },
                    modifier = Modifier.weight(1f)
                )
                TarjetaTipoPerfil(
                    tipo = TipoPerfil.ASOCIADO,
                    icon = Icons.Outlined.Groups,
                    seleccionado = state.tipoPerfil == TipoPerfil.ASOCIADO,
                    onClick = { viewModel.onTipoPerfilChange(TipoPerfil.ASOCIADO) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ---------- Vinculación, condición y permisos ----------
            TarjetaBlanca {
                // HU-03: el titular genera el código, el acompañante lo ingresa.
                OutlinedButton(
                    onClick = { viewModel.generarCodigoInvitacion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = CampoShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Blue700
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Blue700)
                ) {
                    Icon(Icons.Outlined.VpnKey, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (state.tipoPerfil == TipoPerfil.INDIVIDUAL)
                            "Generar código de invitación"
                        else
                            "Ingresar código de invitación",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                state.codigoInvitacion?.let { codigo ->
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Blue100.copy(alpha = 0.4f), CampoShape)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = codigo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Blue700,
                            fontSize = 22.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                CampoPerfil(
                    label = "Condición médica relevante (Opcional)",
                    value = state.condicionRelevante,
                    onValueChange = viewModel::onCondicionChange,
                    placeholder = "Ej. Diabetes Tipo 2, Hipertensión...",
                    imeAction = ImeAction.Done
                )

                Spacer(Modifier.height(20.dp))

                FilaSwitch(
                    titulo = "Permiso de ubicación",
                    subtitulo = "Para sugerir centros médicos cercanos",
                    checked = state.permisoUbicacion,
                    onCheckedChange = { activar ->
                        if (activar) onPedirPermisoUbicacion()
                        else viewModel.onPermisoUbicacionResultado(false)
                    }
                )

                Spacer(Modifier.height(16.dp))

                FilaSwitch(
                    titulo = "Notificaciones activas",
                    subtitulo = "Recordatorios de medicación y citas",
                    checked = state.notificacionesActivas,
                    onCheckedChange = { activar ->
                        if (activar) onPedirPermisoNotificaciones()
                        else viewModel.onPermisoNotificacionesResultado(false)
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onContinuar(viewModel.construirPerfil()) },
                enabled = state.puedeContinuar && !state.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue700,
                    contentColor = Color.White,
                    disabledContainerColor = Blue700.copy(alpha = 0.35f),
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = "Continuar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Podrás cambiar esto más adelante en Ajustes",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (mostrarDatePicker) {
        DialogoFecha(
            millisIniciales = state.fechaNacimientoMillis,
            onConfirmar = {
                viewModel.onFechaChange(it)
                mostrarDatePicker = false
            },
            onCerrar = { mostrarDatePicker = false }
        )
    }
}

/* ---------------------------------------------------------------- */
/*  Componentes                                                      */
/* ---------------------------------------------------------------- */

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
private fun BotonCircular(
    icon: ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, descripcion, tint = Blue700, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun AvatarConCamara(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(96.dp)) {
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
                .clickable { /* TODO: abrir cámara o galería */ },
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
private fun CampoPerfil(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EtiquetaCampo(label)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = CampoShape,
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = {
                Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = PlaceholderGris)
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            colors = coloresCampo()
        )
    }
}

@Composable
private fun CampoFecha(
    label: String,
    millis: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formato = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    Column(modifier = modifier) {
        EtiquetaCampo(label)
        CajaClicable(onClick = onClick) {
            Text(
                text = millis?.let { formato.format(it) } ?: "dd/mm/aaaa",
                style = MaterialTheme.typography.bodyLarge,
                color = if (millis != null) TextPrimary else PlaceholderGris,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Outlined.CalendarMonth, null, tint = Blue500, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CampoGenero(
    genero: Genero?,
    onSeleccionar: (Genero) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        EtiquetaCampo("Género")
        Box {
            CajaClicable(onClick = { expandido = true }) {
                Text(
                    text = genero?.etiqueta ?: "Seleccionar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (genero != null) TextPrimary else PlaceholderGris,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Outlined.ExpandMore, null, tint = Blue500, modifier = Modifier.size(20.dp))
            }
            DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                Genero.entries.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion.etiqueta, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            onSeleccionar(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CajaClicable(
    onClick: () -> Unit,
    contenido: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(CampoBg, CampoShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = contenido
    )
}

@Composable
private fun EtiquetaCampo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        color = Blue700,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun coloresCampo() = TextFieldDefaults.colors(
    focusedContainerColor = CampoBg,
    unfocusedContainerColor = CampoBg,
    disabledContainerColor = CampoBg,
    errorContainerColor = CampoBg,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent,
    cursorColor = Blue700,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)

@Composable
private fun TarjetaTipoPerfil(
    tipo: TipoPerfil,
    icon: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fondo = if (seleccionado) Blue700 else Color.White
    val colorTitulo = if (seleccionado) Color.White else Blue700
    val colorTexto = if (seleccionado) Color.White.copy(alpha = 0.85f) else TextSecondary

    Column(
        modifier = modifier
            .height(150.dp)
            .background(fondo, CardShape)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, null, tint = colorTitulo, modifier = Modifier.size(24.dp))
            if (seleccionado) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.White, CircleShape)
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = tipo.titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorTitulo
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = tipo.descripcion,
            style = MaterialTheme.typography.bodySmall,
            color = colorTexto,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun FilaSwitch(
    titulo: String,
    subtitulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge,
                color = Blue700
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Blue500,
                checkedBorderColor = Blue500,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1),
                uncheckedBorderColor = Color(0xFFCBD5E1)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoFecha(
    millisIniciales: Long?,
    onConfirmar: (Long?) -> Unit,
    onCerrar: () -> Unit
) {
    val estado = rememberDatePickerState(initialSelectedDateMillis = millisIniciales)
    DatePickerDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(onClick = { onConfirmar(estado.selectedDateMillis) }) {
                Text("Aceptar", color = Blue700)
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    ) {
        DatePicker(state = estado)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileSetupPreview() {
    HealthControlTheme {
        ProfileSetupScreen()
    }
}