package com.icm2630.proyecto.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.TipoPerfil
import com.icm2630.proyecto.data.repository.VinculacionSimulator
import com.icm2630.proyecto.navigation.Routes
import com.icm2630.proyecto.ui.components.HealthBottomNavigation
import com.icm2630.proyecto.ui.theme.Blue50
import com.icm2630.proyecto.ui.theme.Blue100
import com.icm2630.proyecto.ui.theme.Blue700
import com.icm2630.proyecto.ui.theme.TextSecondary


@Composable
fun MapScreen(
    perfil: PerfilUsuario = PerfilUsuario(),
    onNavigate: (Routes) -> Unit = {}
) {

    val context = LocalContext.current

    val esAcompanante =
        perfil.tipoPerfil == TipoPerfil.ACOMPANANTE

    val personaVinculada =
        perfil.personaVinculada

    val tienePersonaVinculada =
        esAcompanante &&
                personaVinculada != null

    // PERSONA QUE SE ESTÁ VISUALIZANDO
    var viendoMiUbicacion by rememberSaveable(
        perfil.tipoPerfil,
        personaVinculada?.codigo
    ) {
        mutableStateOf(
            !tienePersonaVinculada
        )
    }

    // HOME SEGÚN EL ROL
    val rutaInicio =
        if (esAcompanante) {

            Routes.Monitoreo

        } else {

            Routes.Home
        }

    // NOMBRE DE LA PERSONA SELECCIONADA
    val nombrePersona =
        if (viendoMiUbicacion) {

            perfil.nombreCompleto
                .ifBlank {
                    "Mi perfil"
                }

        } else {

            personaVinculada
                ?.nombre
                ?: "Persona vinculada"
        }

    val telefonoPersona =
        if (
            esAcompanante &&
            !viendoMiUbicacion
        ) {

            val codigo =
                personaVinculada
                    ?.codigo

            if (codigo.isNullOrBlank()) {

                ""

            } else {

                VinculacionSimulator
                    .telefonoParaCodigo(
                        codigo
                    )
            }

        } else {

            ""
        }

    // LLAMAR
    val llamarPersona: () -> Unit = {

        if (telefonoPersona.isBlank()) {

            Toast.makeText(
                context,
                "No hay un número de celular registrado.",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            val intent =
                Intent(
                    Intent.ACTION_DIAL
                ).apply {

                    data =
                        Uri.parse(
                            "tel:${Uri.encode(telefonoPersona)}"
                        )
                }

            context.startActivity(
                intent
            )
        }
    }

    // PANTALLA
    Scaffold(

        bottomBar = {

            HealthBottomNavigation(
                currentRoute = Routes.Mapa,
                onNavigate = onNavigate,
                rutaInicio = rutaInicio
            )
        },

        containerColor =
            Color(0xFFF8FAFF)

    ) { paddingValues ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    horizontal = 20.dp,
                    vertical = 24.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            // TÍTULO
            Text(
                text = "Mapa",
                style =
                    MaterialTheme
                        .typography
                        .headlineMedium,

                fontWeight = FontWeight.Bold,

                color = Blue700
            )

            if (tienePersonaVinculada) {

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text = "¿De quién quieres ver la ubicación?",

                    style = MaterialTheme
                            .typography
                            .titleSmall,

                    fontWeight = FontWeight.SemiBold,

                    color = TextSecondary
                )


                Spacer(
                    modifier = Modifier.height(4.dp)
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    // YO
                    if (viendoMiUbicacion) {

                        Button(
                            onClick = {
                                viendoMiUbicacion = true
                            },

                            modifier =
                                Modifier.weight(1f),

                            shape =
                                RoundedCornerShape(14.dp),

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Blue700
                                )
                        ) {

                            Text(
                                text = "Yo",
                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }

                    } else {

                        OutlinedButton(
                            onClick = {
                                viendoMiUbicacion = true
                            },

                            modifier =
                                Modifier.weight(1f),

                            shape =
                                RoundedCornerShape(14.dp)
                        ) {

                            Text(
                                text = "Yo",

                                color =
                                    Blue700,

                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }
                    }

                    // PERSONA VINCULADA
                    if (!viendoMiUbicacion) {

                        Button(
                            onClick = {
                                viendoMiUbicacion = false
                            },

                            modifier =
                                Modifier.weight(1f),

                            shape =
                                RoundedCornerShape(14.dp),

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Blue700
                                )
                        ) {

                            Text(
                                text =
                                    personaVinculada
                                        ?.nombre
                                        ?: "Persona",

                                maxLines = 1,

                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }

                    } else {

                        OutlinedButton(
                            onClick = {
                                viendoMiUbicacion = false
                            },

                            modifier =
                                Modifier.weight(1f),

                            shape =
                                RoundedCornerShape(14.dp)
                        ) {

                            Text(
                                text =
                                    personaVinculada
                                        ?.nombre
                                        ?: "Persona",

                                maxLines = 1,

                                color =
                                    Blue700,

                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )
            }

            // TEXTO DE UBICACIÓN
            Text(
                text =
                    if (viendoMiUbicacion) {

                        "Mi ubicación"

                    } else {

                        "Ubicación de $nombrePersona"
                    },

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,

                color =
                    TextSecondary
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            // ESPACIO PARA EL FUTURO MAPA
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),

                shape =
                    RoundedCornerShape(24.dp),

                color =
                    Blue50,

                border =
                    BorderStroke(
                        width = 1.dp,
                        color = Blue100
                    ),

                shadowElevation =
                    2.dp
            ) {

                Box(
                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally,

                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.LocationOn,

                            contentDescription =
                                null,

                            tint =
                                Blue700
                        )


                        Text(
                            text =
                                "Espacio para el mapa",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            fontWeight =
                                FontWeight.SemiBold,

                            color =
                                Blue700
                        )


                        Text(
                            text =
                                "La integración con el servicio de mapas se realizará posteriormente.",

                            modifier =
                                Modifier.padding(
                                    horizontal = 28.dp
                                ),

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,

                            color =
                                TextSecondary,

                            textAlign =
                                TextAlign.Center
                        )
                    }
                }
            }

            if (telefonoPersona.isNotBlank()) {

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )


                Button(
                    onClick =
                        llamarPersona,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),

                    shape =
                        RoundedCornerShape(16.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Blue700
                        )
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.Call,

                        contentDescription =
                            null
                    )


                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )


                    Text(
                        text =
                            "Llamar a $nombrePersona",

                        fontWeight =
                            FontWeight.Bold
                    )
                }


                Text(
                    text =
                        telefonoPersona,

                    modifier =
                        Modifier.fillMaxWidth(),

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        TextSecondary,

                    textAlign =
                        TextAlign.Center
                )
            }
        }
    }
}