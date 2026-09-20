package com.icm2630.proyecto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.R
import com.icm2630.proyecto.data.repository.SesionRepository
import com.icm2630.proyecto.ui.theme.*

private val FieldShape = RoundedCornerShape(50)
private val PlaceholderBlue = Color(0xFF8FB3D9)
private val HairLine = Color(0xFFE5E7EB)

@Composable
fun LoginScreen(
    onLogin: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onAppleClick: () -> Unit = {},
    onIrARegister: () -> Unit = {}
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var verPassword by rememberSaveable { mutableStateOf(false) }
    var recordar by rememberSaveable { mutableStateOf(false) }

    // Estado para errores de validación
    var errorTexto by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFF)) // Un fondo ligeramente azulado como en la imagen
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Logo()

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Health Control",
                style = MaterialTheme.typography.displaySmall,
                color = Blue700,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Monitoreo de tus tratamientos y citas clínicas",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(32.dp))

            // Card para el formulario
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp), ambientColor = Blue100, spotColor = Blue100)
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                CampoTexto(
                    label = "Correo Electrónico",
                    value = correo,
                    onValueChange = { 
                        correo = it
                        errorTexto = null 
                    },
                    placeholder = "test@gmail.com",
                    leadingIcon = Icons.Outlined.MailOutline,
                    keyboardType = KeyboardType.Email
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.labelLarge,
                        color = Blue700,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        style = MaterialTheme.typography.labelMedium,
                        color = Blue500,
                        modifier = Modifier.clickable { /* Olvidé contraseña */ }
                    )
                }

                Spacer(Modifier.height(8.dp))

                TextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorTexto = null 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = FieldShape,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    placeholder = {
                        Text(text = "12345678", color = PlaceholderBlue)
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, null, tint = Blue700, modifier = Modifier.size(22.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { verPassword = !verPassword }) {
                            Icon(
                                imageVector = if (verPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = Blue500,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    visualTransformation = if (verPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Blue100,
                        unfocusedContainerColor = Blue100,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Blue700
                    )
                )

                if (errorTexto != null) {
                    Text(
                        text = errorTexto!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = recordar,
                        onCheckedChange = { recordar = it },
                        colors = CheckboxDefaults.colors(checkedColor = Blue700)
                    )
                    Text(
                        text = "Recordar en este dispositivo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { 
                        if (correo.isBlank() || password.isBlank()) {
                            errorTexto = "Por favor, completa todos los campos"
                        } else if (SesionRepository.validarCredenciales(correo, password)) {
                            onLogin()
                        } else {
                            errorTexto = "Credenciales incorrectas"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = FieldShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue700)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Iniciar Sesión",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            SeparadorConTexto(texto = "O CONTINUAR CON")

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BotonSocial(
                    texto = "Google",
                    icon = Icons.Outlined.Cancel, // Simulado
                    onClick = onGoogleClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(32.dp))
        }

        // Pie
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿No tienes una cuenta? ",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "Regístrate gratis",
                style = MaterialTheme.typography.labelLarge,
                color = Blue700,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onIrARegister() }
            )
        }
    }
}

@Composable
private fun Logo() {
    Image(
        painter = painterResource(R.drawable.loguitouwu),
        contentDescription = "Logo de Health Control",
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(32.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun CampoTexto(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Blue700,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = FieldShape,
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = {
                Text(text = placeholder, color = PlaceholderBlue)
            },
            leadingIcon = {
                Icon(leadingIcon, null, tint = Blue700, modifier = Modifier.size(22.dp))
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Blue100,
                unfocusedContainerColor = Blue100,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Blue700
            )
        )

        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun SeparadorConTexto(texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = HairLine, thickness = 1.dp)
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = HairLine, thickness = 1.dp)
    }
}

@Composable
private fun BotonSocial(
    texto: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = FieldShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Blue700
        ),
        border = BorderStroke(1.dp, HairLine)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    HealthControlTheme {
        LoginScreen()
    }
}
