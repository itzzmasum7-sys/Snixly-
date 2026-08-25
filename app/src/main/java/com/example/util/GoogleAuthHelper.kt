package com.example.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

object GoogleAuthHelper {

  suspend fun getGoogleIdToken(context: Context, webClientId: String? = null): Result<String> {
    return try {
      val credentialManager = CredentialManager.create(context)
      
      val clientId = webClientId?.takeIf { it.isNotBlank() } 
        ?: "1083442657748-default.apps.googleusercontent.com"

      val rawNonce = UUID.randomUUID().toString()
      val bytes = rawNonce.toByteArray()
      val md = MessageDigest.getInstance("SHA-256")
      val digest = md.digest(bytes)
      val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

      val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(false)
        .setNonce(hashedNonce)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val response: GetCredentialResponse = credentialManager.getCredential(
        request = request,
        context = context
      )

      val credential = response.credential
      if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        Result.success(googleIdTokenCredential.idToken)
      } else {
        Result.failure(Exception("Received unrecognized credential type from Google provider."))
      }
    } catch (e: GetCredentialCancellationException) {
      Result.failure(Exception("Google Sign-In was cancelled."))
    } catch (e: GetCredentialException) {
      Result.failure(Exception("Google Sign-In failed: ${e.message ?: "Authentication error"}"))
    } catch (e: Exception) {
      Result.failure(Exception("Google Sign-In encountered an error: ${e.localizedMessage ?: "Unknown error"}"))
    }
  }
}
