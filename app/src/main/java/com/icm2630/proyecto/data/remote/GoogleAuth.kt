package com.icm2630.proyecto.data.remote

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.icm2630.proyecto.R
import com.icm2630.proyecto.data.repository.SesionRepository


object GoogleAuth {

    /**
     * Muestra el selector de cuentas de Google y devuelve el idToken
     * que Firebase necesita para iniciar sesión.
     *
     * [context] debe ser el de la Activity (LocalContext en Compose).
     */
    suspend fun obtenerIdToken(context: Context): String {
        val opcion = GetSignInWithGoogleOption.Builder(
            context.getString(R.string.default_web_client_id)
        ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(opcion)
            .build()

        val resultado = CredentialManager.create(context).getCredential(context, request)
        return GoogleIdTokenCredential.createFrom(resultado.credential.data).idToken
    }

    /**
     * Flujo completo: selector de cuentas → Firebase → perfil en Firestore.
     * Si el usuario cierra el selector, [onError] recibe un mensaje vacío:
     * solo sirve para quitar el estado de carga, no hay nada que mostrar.
     */
    suspend fun iniciarSesion(
        context: Context,
        onListo: () -> Unit,
        onError: (String) -> Unit
    ) {
        val idToken = try {
            obtenerIdToken(context)
        } catch (e: GetCredentialCancellationException) {
            onError("")
            return
        } catch (e: NoCredentialException) {
            onError("No hay cuentas de Google en este dispositivo.")
            return
        } catch (e: GetCredentialException) {
            onError("No se pudo iniciar sesión con Google.")
            return
        } catch (e: GoogleIdTokenParsingException) {
            onError("No se pudo iniciar sesión con Google.")
            return
        }

        SesionRepository.iniciarSesionConGoogle(idToken, onListo, onError)
    }
}
