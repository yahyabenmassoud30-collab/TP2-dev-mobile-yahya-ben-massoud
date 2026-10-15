package com.example.patients.data.repository

import com.example.patients.data.model.ApiError
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
import com.example.patients.data.remote.PatientApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

class PatientRepository(
    private val api: PatientApi,
    private val json: Json
) {

    // Exercice 2 : statut optionnel (null = pas de filtre, paramètre omis de l'URL)
    suspend fun getPatients(statut: String? = null): Result<List<Patient>> =
        safeCall { api.getPatients(statut) }

    suspend fun createPatient(request: PatientRequest): Result<Patient> =
        safeCall { api.createPatient(request) }

    // Exercice 1
    suspend fun updatePatient(id: String, request: PatientRequest): Result<Patient> =
        safeCall { api.updatePatient(id, request) }

    suspend fun deletePatient(id: String): Result<Unit> = safeCall {
        val response = api.deletePatient(id)
        if (!response.isSuccessful) throw HttpException(response)
    }

    // Exécute un appel réseau et transforme toute erreur en message lisible
    private suspend fun <T> safeCall(call: suspend () -> T): Result<T> =
        try {
            Result.success(call())
        } catch (e: HttpException) {          // le serveur a répondu 4xx ou 5xx
            Result.failure(Exception(messageFrom(e)))
        } catch (e: IOException) {            // pas de réponse : serveur éteint, mauvaise IP...
            Result.failure(Exception("Serveur injoignable. Vérifiez l'adresse et le réseau."))
        } catch (e: SerializationException) { // JSON reçu différent de la classe Kotlin
            Result.failure(Exception("Réponse inattendue du serveur."))
        }

    // Lit le corps JSON de l'erreur renvoyé par notre API Node
    private fun messageFrom(e: HttpException): String {
        val body = e.response()?.errorBody()?.string()
        val apiError = body?.let {
            runCatching { json.decodeFromString(ApiError.serializer(), it) }.getOrNull()
        }
        return when {
            apiError == null -> "Erreur ${e.code()}"
            apiError.erreurs.isNotEmpty() -> apiError.erreurs.joinToString("\n")
            else -> apiError.message
        }
    }
}
