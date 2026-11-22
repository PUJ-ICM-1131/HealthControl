package com.icm2630.proyecto.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.icm2630.proyecto.data.model.ContactoDispositivo
import com.icm2630.proyecto.data.repository.ContactosRepository
import com.icm2630.proyecto.ui.theme.Blue700
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun SeleccionarContactoButton(
    onSeleccionado: (ContactoDispositivo) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val seleccionado by rememberUpdatedState(onSeleccionado)
    val scope = rememberCoroutineScope()
    var leyendo by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val selector = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        val uri = resultado.data?.data
        // Cancelar conserva los datos que ya estaban escritos en el formulario.
        if (resultado.resultCode == Activity.RESULT_OK && uri != null) {
            leyendo = true
            error = null
            scope.launch {
                try {
                    seleccionado(ContactosRepository.obtenerTelefono(context.applicationContext, uri))
                } catch (e: CancellationException) {
                    throw e
                } catch (_: SecurityException) {
                    error = "No se pudo acceder a ese contacto. Selecciónalo de nuevo o escribe los datos."
                } catch (e: Exception) {
                    Log.w("SelectorContacto", "No se pudo leer el teléfono seleccionado", e)
                    error = "No se pudo leer el contacto. Elige otro con teléfono o escribe los datos manualmente."
                } finally {
                    leyendo = false
                }
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedButton(
            onClick = {
                error = null
                try {
                    selector.launch(Intent(Intent.ACTION_PICK).apply {
                        type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                    })
                } catch (_: ActivityNotFoundException) {
                    error = "No hay una aplicación de contactos disponible. Puedes escribir los datos manualmente."
                } catch (_: SecurityException) {
                    error = "No se pudo abrir el selector de contactos. Puedes escribir los datos manualmente."
                }
            },
            enabled = enabled && !leyendo,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            if (leyendo) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Blue700)
            else Icon(Icons.Outlined.Contacts, contentDescription = null, tint = Blue700)
            Spacer(Modifier.width(8.dp))
            Text(if (leyendo) "Leyendo contacto…" else "Elegir de mis contactos")
        }
        error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
    }
}
