package com.icm2630.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icm2630.proyecto.ui.components.ChipsCondiciones
import com.icm2630.proyecto.ui.components.ChipsTipoSangre
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.data.model.Genero
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.PersonaVinculada
import com.icm2630.proyecto.data.repository.VinculacionSimulator
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue500
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.ErrorRed
import com.icm2630.proyecto.ui.theme.ErrorRedBg
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.SuccessGreen
import com.icm2630.proyecto.ui.theme.SuccessGreenBg
import com.icm2630.proyecto.ui.theme.TextPrimary
import com.icm2630.proyecto.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val Fondo = Color(0xFFF8FAFF)
private val CardShape = RoundedCornerShape(24.dp)
private val HairLine = Color(0xFFE5E7EB)
private const val SIN_ESPECIFICAR = "Sin especificar"

/**
 * Identifica qué campo del perfil se está editando. Todos se pueden
 * editar en cualquier momento, incluso si quedaron vacíos en el onboarding.
 * FOTO solo se reporta hacia afuera (aún no hay selector de imagen).
 */
enum class CampoPerfilEditable {
    FOTO, NOMBRE, FECHA_NACIMIENTO, GENERO, CONTACTO,
    TIPO_SANGRE, ALERGIAS, CONDICIONES, CONTACTO_EMERGENCIA
}

@Composable
fun ProfileScreen(
    perfil: PerfilUsuario = PerfilUsuario(),
    onNavigate: (Routes) -> Unit = {},
    onEditarCampo: (CampoPerfilEditable) -> Unit = {},
    onActualizarPerfil: (PerfilUsuario) -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    var mostrarConfirmacionCierre by remember { mutableStateOf(false) }

    // Copia local para que la pantalla refleje al instante lo editado;
    // la fuente de verdad (SesionRepository) se actualiza vía onActualizarPerfil.
    var datos by remember(perfil) { mutableStateOf(perfil) }
    // Mientras no haya backend se muestran dos personas de ejemplo.
    val personas = datos.acompanantes.ifEmpty { PERSONAS_EJEMPLO }
    var campoEditando by remember { mutableStateOf<CampoPerfilEditable?>(null) }

    var mostrarAgregarPersona by remember { mutableStateOf(false) }

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
                    text = datos.nombreCompleto.ifBlank { SIN_ESPECIFICAR },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Blue700
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
                    valor = datos.nombreCompleto.ifBlank { SIN_ESPECIFICAR },
                    onEditar = { campoEditando = CampoPerfilEditable.NOMBRE }
                )
                DivisorFila()
                FilaDato(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "Fecha de nacimiento",
                    valor = formatearFecha(datos.fechaNacimientoMillis),
                    onEditar = { campoEditando = CampoPerfilEditable.FECHA_NACIMIENTO }
                )
                DivisorFila()
                FilaDato(
                    icon = Icons.Outlined.Info,
                    label = "Género",
                    valor = datos.genero?.etiqueta ?: SIN_ESPECIFICAR,
                    onEditar = { campoEditando = CampoPerfilEditable.GENERO }
                )
                DivisorFila()
                FilaDato(
                    icon = Icons.Outlined.MailOutline,
                    label = "Teléfono o correo",
                    valor = datos.contacto.ifBlank { SIN_ESPECIFICAR },
                    onEditar = { campoEditando = CampoPerfilEditable.CONTACTO }
                )
            }

            Spacer(Modifier.height(24.dp))

            // ---------- Información de salud ----------
            SeccionTitulo("Información de salud")
            Spacer(Modifier.height(12.dp))
            TarjetaBlanca {
                FilaDato(
                    icon = Icons.Outlined.Bloodtype,
                    label = "Tipo de sangre",
                    valor = datos.tipoSangre?.etiqueta ?: SIN_ESPECIFICAR,
                    onEditar = { campoEditando = CampoPerfilEditable.TIPO_SANGRE }
                )
                DivisorFila()
                FilaDato(
                    icon = Icons.Outlined.Warning,
                    label = "Alergias",
                    valor = datos.alergias.ifBlank { SIN_ESPECIFICAR },
                    onEditar = { campoEditando = CampoPerfilEditable.ALERGIAS }
                )
                DivisorFila()
                FilaDato(
                    icon = Icons.Outlined.MedicalServices,
                    label = "Condiciones médicas",
                    valor = (datos.condiciones + datos.condicionRelevante.trim())
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                        .ifBlank { SIN_ESPECIFICAR },
                    onEditar = { campoEditando = CampoPerfilEditable.CONDICIONES }
                )
                DivisorFila()
                FilaDato(
                    icon = Icons.Outlined.Shield,
                    label = "Contacto de emergencia",
                    valor = listOf(datos.contactoEmergencia, datos.telefonoEmergencia)
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                        .ifBlank { SIN_ESPECIFICAR },
                    onEditar = { campoEditando = CampoPerfilEditable.CONTACTO_EMERGENCIA }
                )
            }

            Spacer(Modifier.height(24.dp))

            // ---------- Personas en mi HealthControl ----------
            SeccionTitulo("Personas en mi HealthControl")
            Spacer(Modifier.height(12.dp))
            TarjetaBlanca {
                personas.forEachIndexed { indice, persona ->
                    if (indice > 0) DivisorFila()
                    FilaPersona(persona = persona, colorIndice = indice)
                }

                if (personas.isNotEmpty()) DivisorFila()

                FilaAgregarPersona(onClick = { mostrarAgregarPersona = true })
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

    campoEditando?.let { campo ->
        DialogoEditarCampo(
            campo = campo,
            perfil = datos,
            onGuardar = { nuevo ->
                datos = nuevo
                onActualizarPerfil(nuevo)
                campoEditando = null
            },
            onCancelar = { campoEditando = null }
        )
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

    if (mostrarAgregarPersona) {
        DialogoAgregarPersona(
            onConfirmar = { nueva ->
                val actualizado = datos.copy(acompanantes = personas + nueva)
                datos = actualizado
                onActualizarPerfil(actualizado)
                mostrarAgregarPersona = false
            },
            onCancelar = { mostrarAgregarPersona = false }
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

private val PERSONAS_EJEMPLO = listOf(
    PersonaVinculada(nombre = "Elena Ramírez", relacion = "Madre", codigo = "HC4F7K"),
    PersonaVinculada(nombre = "Carlos Gómez", relacion = "Hermano", codigo = "HC9M2P")
)

private val ColoresAvatar = listOf(
    Color(0xFF2F6FDE), Color(0xFF1FA37A), Color(0xFFE08A1E), Color(0xFF8E5BD9)
)

@Composable
private fun AvatarPersona(nombre: String, colorIndice: Int, tamano: Dp = 52.dp) {
    val iniciales = nombre.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    val color = ColoresAvatar[colorIndice % ColoresAvatar.size]
    Box(
        modifier = Modifier
            .size(tamano)
            .background(color.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (iniciales.isEmpty()) {
            Icon(Icons.Outlined.Person, null, tint = color, modifier = Modifier.size(tamano / 2))
        } else {
            Text(
                text = iniciales,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun FilaPersona(persona: PersonaVinculada, colorIndice: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarPersona(persona.nombre, colorIndice)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = persona.nombre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = persona.relacion.ifBlank { "Familiar" },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun FilaAgregarPersona(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Blue100.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.PersonAdd, null, tint = Blue700, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = "Añadir una nueva persona en mi HealthControl",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Blue700,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Outlined.ChevronRight, null, tint = Blue500)
    }
}

/** Pide el ID de la persona a añadir. Sin backend, el ID se resuelve con el simulador. */
@Composable
private fun DialogoAgregarPersona(
    onConfirmar: (PersonaVinculada) -> Unit,
    onCancelar: () -> Unit
) {
    var id by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Añadir una persona") },
        text = {
            Column {
                Text(
                    "Ingresa el ID de la persona que quieres añadir a tu HealthControl.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = id,
                    onValueChange = {
                        id = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(VinculacionSimulator.LONGITUD_CODIGO)
                        error = null
                    },
                    label = { Text("ID de la persona") },
                    placeholder = { Text("Ej. 7K3PQ9") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (id.length != VinculacionSimulator.LONGITUD_CODIGO) {
                    error = "El ID debe tener ${VinculacionSimulator.LONGITUD_CODIGO} caracteres"
                } else {
                    onConfirmar(
                        PersonaVinculada(
                            nombre = VinculacionSimulator.nombreParaCodigo(id),
                            relacion = "Familiar",
                            codigo = id
                        )
                    )
                }
            }) { Text("Añadir", color = Blue700) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", color = TextSecondary) }
        }
    )
}

/**
 * Diálogo de edición de un solo campo. Guardar nunca exige que el campo
 * tenga valor: se puede dejar vacío (salvo la fecha, que solo se cambia).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoEditarCampo(
    campo: CampoPerfilEditable,
    perfil: PerfilUsuario,
    onGuardar: (PerfilUsuario) -> Unit,
    onCancelar: () -> Unit
) {
    if (campo == CampoPerfilEditable.FECHA_NACIMIENTO) {
        val estado = rememberDatePickerState(initialSelectedDateMillis = perfil.fechaNacimientoMillis)
        DatePickerDialog(
            onDismissRequest = onCancelar,
            confirmButton = {
                TextButton(
                    enabled = estado.selectedDateMillis != null,
                    onClick = { onGuardar(perfil.copy(fechaNacimientoMillis = estado.selectedDateMillis)) }
                ) { Text("Guardar", color = Blue700) }
            },
            dismissButton = {
                TextButton(onClick = onCancelar) { Text("Cancelar", color = TextSecondary) }
            }
        ) { DatePicker(state = estado) }
        return
    }

    var nombre by remember { mutableStateOf(perfil.nombreCompleto) }
    var contacto by remember { mutableStateOf(perfil.contacto) }
    var genero by remember { mutableStateOf(perfil.genero) }
    var tipoSangre by remember { mutableStateOf(perfil.tipoSangre) }
    var alergias by remember { mutableStateOf(perfil.alergias) }
    var condiciones by remember { mutableStateOf(perfil.condiciones) }
    var otraCondicion by remember { mutableStateOf(perfil.condicionRelevante) }
    var emergencia by remember { mutableStateOf(perfil.contactoEmergencia) }
    var telefonoEmergencia by remember { mutableStateOf(perfil.telefonoEmergencia) }

    val titulo = when (campo) {
        CampoPerfilEditable.NOMBRE -> "Nombre completo"
        CampoPerfilEditable.GENERO -> "Género"
        CampoPerfilEditable.CONTACTO -> "Teléfono o correo"
        CampoPerfilEditable.TIPO_SANGRE -> "Tipo de sangre"
        CampoPerfilEditable.ALERGIAS -> "Alergias"
        CampoPerfilEditable.CONDICIONES -> "Condiciones médicas"
        CampoPerfilEditable.CONTACTO_EMERGENCIA -> "Contacto de emergencia"
        else -> ""
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                when (campo) {
                    CampoPerfilEditable.NOMBRE -> CampoDialogo("Nombre completo", nombre) { nombre = it }
                    CampoPerfilEditable.CONTACTO -> CampoDialogo("Teléfono o correo", contacto) { contacto = it }
                    CampoPerfilEditable.GENERO -> Genero.entries.forEach { opcion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { genero = if (genero == opcion) null else opcion },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = genero == opcion, onClick = { genero = if (genero == opcion) null else opcion })
                            Text(opcion.etiqueta, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    CampoPerfilEditable.TIPO_SANGRE -> ChipsTipoSangre(
                        seleccionado = tipoSangre,
                        onSeleccionar = { tipoSangre = if (tipoSangre == it) null else it }
                    )
                    CampoPerfilEditable.ALERGIAS -> CampoDialogo("Ej. Penicilina, mariscos...", alergias) { alergias = it }
                    CampoPerfilEditable.CONDICIONES -> {
                        ChipsCondiciones(
                            seleccionadas = condiciones,
                            onToggle = { c -> condiciones = if (c in condiciones) condiciones - c else condiciones + c }
                        )
                        Spacer(Modifier.height(12.dp))
                        CampoDialogo("Otra condición", otraCondicion) { otraCondicion = it }
                    }
                    CampoPerfilEditable.CONTACTO_EMERGENCIA -> {
                        CampoDialogo("Nombre y relación", emergencia) { emergencia = it }
                        Spacer(Modifier.height(8.dp))
                        CampoDialogo("Teléfono", telefonoEmergencia, KeyboardType.Phone) {
                            telefonoEmergencia = it.filter { c -> c.isDigit() || c == '+' }
                        }
                    }
                    else -> Unit
                }
                Text(
                    text = "Puedes dejarlo vacío y completarlo cuando quieras.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onGuardar(
                    when (campo) {
                        CampoPerfilEditable.NOMBRE -> perfil.copy(nombreCompleto = nombre.trim())
                        CampoPerfilEditable.CONTACTO -> perfil.copy(contacto = contacto.trim())
                        CampoPerfilEditable.GENERO -> perfil.copy(genero = genero)
                        CampoPerfilEditable.TIPO_SANGRE -> perfil.copy(tipoSangre = tipoSangre)
                        CampoPerfilEditable.ALERGIAS -> perfil.copy(alergias = alergias.trim())
                        CampoPerfilEditable.CONDICIONES -> perfil.copy(
                            condiciones = condiciones,
                            condicionRelevante = otraCondicion.trim()
                        )
                        CampoPerfilEditable.CONTACTO_EMERGENCIA -> perfil.copy(
                            contactoEmergencia = emergencia.trim(),
                            telefonoEmergencia = telefonoEmergencia.trim()
                        )
                        else -> perfil
                    }
                )
            }) { Text("Guardar", color = Blue700) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", color = TextSecondary) }
        }
    )
}

@Composable
private fun CampoDialogo(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun formatearFecha(millis: Long?): String {
    if (millis == null) return SIN_ESPECIFICAR
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
