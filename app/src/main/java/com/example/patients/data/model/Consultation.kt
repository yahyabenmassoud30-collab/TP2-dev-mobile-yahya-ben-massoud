package com.example.patients.data.model

import kotlinx.serialization.Serializable

// Patient « résumé » renvoyé par populate("patient", "nom prenom")
@Serializable
data class PatientRef(
    val id: String,
    val nom: String,
    val prenom: String
)

// Consultation telle que renvoyée par l'API
@Serializable
data class Consultation(
    val id: String,
    val patient: PatientRef? = null,   // null si le patient a été supprimé
    val date: String,                  // ISO 8601, ex. "2026-10-05T00:00:00.000Z"
    val motif: String,
    val traitement: String? = null,
    val medecin: String,
    val notes: String? = null,
    val statut: String = "En attente"
)

// Données envoyées pour créer ou modifier une consultation (POST / PUT).
// Aucune valeur par défaut : avec encodeDefaults = false, une valeur par défaut
// serait omise du JSON (ex. le statut) et ne serait donc pas envoyée au serveur.
@Serializable
data class ConsultationRequest(
    val patient: String,         // id du patient
    val date: String,            // "2026-10-05"
    val motif: String,
    val traitement: String?,
    val medecin: String,
    val notes: String?,
    val statut: String
)
