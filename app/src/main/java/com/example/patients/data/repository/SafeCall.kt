package com.example.patients.data.repository

import com.example.patients.data.model.ApiError
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

// Même logique que safeCall de PatientRepository, partagée pour les nouveaux repositories
suspend fun <T> safeApiCall(json: Json, call: suspend () -> T): Result<T> =
    try {
        Result.success(call())
    } catch (e: HttpException) {
        Result.failure(Exception(errorMessage(json, e)))
    } catch (e: IOException) {
        Result.failure(Exception("Serveur injoignable. Vérifiez l'adresse et le réseau."))
    } catch (e: SerializationException) {
        Result.failure(Exception("Réponse inattendue du serveur."))
    }

private fun errorMessage(json: Json, e: HttpException): String {
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
