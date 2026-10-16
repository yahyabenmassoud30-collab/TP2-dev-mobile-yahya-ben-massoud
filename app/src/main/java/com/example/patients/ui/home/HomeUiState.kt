package com.example.patients.ui.home

// Chiffres affichés sur la page d'accueil
data class HomeStats(
    val patients: Int,
    val consultations: Int,
    val medecins: Int
)

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val stats: HomeStats) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
