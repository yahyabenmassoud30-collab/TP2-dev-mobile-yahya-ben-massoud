package com.example.patients.data.repository

import com.example.patients.data.model.Consultation
import com.example.patients.data.model.ConsultationRequest
import com.example.patients.data.remote.ConsultationApi
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class ConsultationRepository(
    private val api: ConsultationApi,
    private val json: Json
) {
    suspend fun getConsultations(patientId: String): Result<List<Consultation>> =
        safeApiCall(json) { api.getConsultations(patientId = patientId) }

    // Toutes les consultations (page d'accueil)
    suspend fun getAllConsultations(): Result<List<Consultation>> =
        safeApiCall(json) { api.getConsultations() }

    suspend fun createConsultation(request: ConsultationRequest): Result<Consultation> =
        safeApiCall(json) { api.createConsultation(request) }

    suspend fun updateConsultation(id: String, request: ConsultationRequest): Result<Consultation> =
        safeApiCall(json) { api.updateConsultation(id, request) }

    suspend fun deleteConsultation(id: String): Result<Unit> = safeApiCall(json) {
        val response = api.deleteConsultation(id)
        if (!response.isSuccessful) throw HttpException(response)
    }
}
