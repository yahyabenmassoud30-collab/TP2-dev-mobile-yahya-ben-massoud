package com.example.patients.data.remote

import com.example.patients.data.model.Consultation
import com.example.patients.data.model.ConsultationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ConsultationApi {

    // GET /api/consultations?patient=<id>&statut=<statut> (paramètres null omis)
    @GET("consultations")
    suspend fun getConsultations(
        @Query("patient") patientId: String? = null,
        @Query("statut") statut: String? = null
    ): List<Consultation>

    @POST("consultations")
    suspend fun createConsultation(@Body consultation: ConsultationRequest): Consultation

    @PUT("consultations/{id}")
    suspend fun updateConsultation(
        @Path("id") id: String,
        @Body consultation: ConsultationRequest
    ): Consultation

    // 204 = réponse sans corps
    @DELETE("consultations/{id}")
    suspend fun deleteConsultation(@Path("id") id: String): Response<Unit>
}
