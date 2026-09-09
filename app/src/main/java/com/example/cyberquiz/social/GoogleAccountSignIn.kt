package com.example.cyberquiz.social

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

internal object GoogleAccountSignIn {
    suspend fun getIdToken(context: Context, serverClientId: String): String {
        require(serverClientId.isNotBlank()) { "Google client ID manquant" }
        val option = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val result = CredentialManager.create(context).getCredential(
            context = context,
            request = request
        )
        val credential = result.credential
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("Réponse Google non reconnue")
        }
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }

    suspend fun clearState(context: Context) {
        runCatching {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        }
    }
}
