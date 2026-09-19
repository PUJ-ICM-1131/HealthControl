package com.icm2630.proyecto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icm2630.proyecto.R
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue500
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.HealthControlTheme
import com.icm2630.proyecto.ui.theme.TextPrimary
import com.icm2630.proyecto.ui.theme.TextSecondary

private val FieldShape = RoundedCornerShape(50)
private val PlaceholderBlue = Color(0xFF8FB3D9)
private val HairLine = Color(0xFFE5E7EB)

@Composable
fun RegisterScreen(
    onRegister: (nombre: String, correo: String, password: String) -> Unit = { _, _, _ -> },
    onGoogleClick: () -> Unit = {},
    onIrALogin: () -> Unit = {}
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var verPassword by rememberSaveable { mutableStateOf(false) }
    var verConfirmar by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        // ---------- Contenido desplazable ----------
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Logo()

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.displaySmall,
                color = Blue700,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Regístrate para comenzar a organizar tu día de forma inteligente",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(28.dp))

            CampoTexto(
                label = "Nombre completo",
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = "Juan Pérez",
                leadingIcon = Icons.Outlined.Person,
                keyboardType = KeyboardType.Text
            )

            CampoTexto(
                label = "Correo electrónico",
                value = correo,
                onValueChange = { correo = it },
                placeholder = "ejemplo@correo.com",
                leadingIcon = Icons.Outlined.MailOutline,
                keyboardType = KeyboardType.Email
            )

            CampoTexto(
                label = "Contraseña",
                value = password,
                onValueChange = { password = it },
                placeholder = "Mínimo 8 caracteres",
                leadingIcon = Icons.Outlined.Lock,
                esPassword = true,
                visible = verPassword,
                onToggleVisibilidad = { verPassword = !verPassword }
            )

            CampoTexto(
                label = "Confirmar contraseña",
                value = confirmar,
                onValueChange = { confirmar = it },
                placeholder = "Repite tu contraseña",
                leadingIcon = Icons.Outlined.Shield,
                esPassword = true,
                visible = verConfirmar,
                onToggleVisibilidad = { verConfirmar = !verConfirmar },
                imeAction = ImeAction.Done
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { onRegister(nombre, correo, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = FieldShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue500,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Registrarse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            SeparadorConTexto(texto = "O CONTINUAR CON")

            Spacer(Modifier.height(24.dp))

            BotonSocial(
                texto = "Google",
                // Reemplaza por painterResource(R.drawable.ic_google) cuando agregues el asset oficial
                icon = Icons.Outlined.Cancel,
                onClick = onGoogleClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))
        }

        // ---------- Pie fijo ----------
        HorizontalDivider(color = HairLine, thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Ya tienes cuenta? ",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "Inicia sesión",
                style = MaterialTheme.typography.labelLarge,
                color = Blue700,
                modifier = Modifier.clickable { onIrALogin() }
            )
        }
    }
}

/* ---------------------------------------------------------------- */
/*  Componentes                                                      */
/* ---------------------------------------------------------------- */

@Composable
private fun Logo() {
    Box(
        modifier = Modifier
            .size(130.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Blue500,
                spotColor = Blue500
            )
            .background(Color.White, RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.loguitouwu),
            contentDescription = "Logo de Health Control",
            modifier = Modifier.size(94.dp)
        )
    }
}

@Composable
private fun CampoTexto(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    esPassword: Boolean = false,
    visible: Boolean = false,
    onToggleVisibilidad: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
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
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PlaceholderBlue
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Blue700,
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = if (esPassword) {
                {
                    IconButton(onClick = onToggleVisibilidad) {
                        Icon(
                            imageVector = if (visible) Icons.Outlined.Visibility
                            else Icons.Outlined.VisibilityOff,
                            contentDescription = if (visible) "Ocultar contraseña"
                            else "Mostrar contraseña",
                            tint = Blue500,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (esPassword && !visible) PasswordVisualTransformation()
            else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (esPassword) KeyboardType.Password else keyboardType,
                imeAction = imeAction
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Blue100,
                unfocusedContainerColor = Blue100,
                disabledContainerColor = Blue100,
                errorContainerColor = Blue100,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = Blue700,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
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
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1.2f)
                .padding(horizontal = 12.dp)
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    HealthControlTheme {
        RegisterScreen()
    }
}