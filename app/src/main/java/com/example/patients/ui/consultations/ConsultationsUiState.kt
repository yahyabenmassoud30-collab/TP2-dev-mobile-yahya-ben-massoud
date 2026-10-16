package com.example.patients.ui.consultations

import com.example.patients.data.model.Consultation

// États possibles de l'écran des consultations (propre à cet écran)
sealed interface ConsultationsUiState {
    data object Loading : ConsultationsUiState
    data class Success(val consultations: List<Consultation>) : ConsultationsUiState
    data class Error(val message: String) : ConsultationsUiState
}
