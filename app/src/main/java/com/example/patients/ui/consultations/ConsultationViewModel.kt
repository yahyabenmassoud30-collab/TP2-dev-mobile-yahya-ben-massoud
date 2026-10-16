package com.example.patients.ui.consultations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.patients.PatientsApplication
import com.example.patients.data.model.Consultation
import com.example.patients.data.model.ConsultationRequest
import com.example.patients.data.repository.ConsultationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConsultationViewModel(
    private val repository: ConsultationRepository,
    private val patientId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConsultationsUiState>(ConsultationsUiState.Loading)
    val uiState: StateFlow<ConsultationsUiState> = _uiState.asStateFlow()

    // Messages ponctuels (Snackbar)
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = ConsultationsUiState.Loading
        viewModelScope.launch {
            repository.getConsultations(patientId)
                .onSuccess { _uiState.value = ConsultationsUiState.Success(it) }
                .onFailure { _uiState.value = ConsultationsUiState.Error(it.message ?: "Erreur inconnue") }
        }
    }

    // POST puis rechargement : la liste reste triée (plus récentes d'abord) comme le serveur
    fun addConsultation(request: ConsultationRequest) {
        viewModelScope.launch {
            repository.createConsultation(request)
                .onSuccess {
                    _messages.emit("Consultation ajoutée")
                    reloadSilently()
                }
                .onFailure { _messages.emit(it.message ?: "Ajout impossible") }
        }
    }

    // PUT
    fun updateConsultation(id: String, request: ConsultationRequest) {
        viewModelScope.launch {
            repository.updateConsultation(id, request)
                .onSuccess {
                    _messages.emit("Consultation modifiée")
                    reloadSilently()
                }
                .onFailure { _messages.emit(it.message ?: "Modification impossible") }
        }
    }

    // DELETE
    fun deleteConsultation(consultation: Consultation) {
        viewModelScope.launch {
            repository.deleteConsultation(consultation.id)
                .onSuccess {
                    _messages.emit("Consultation supprimée")
                    reloadSilently()
                }
                .onFailure { _messages.emit(it.message ?: "Suppression impossible") }
        }
    }

    // Recharge sans repasser par l'écran de chargement
    private suspend fun reloadSilently() {
        repository.getConsultations(patientId)
            .onSuccess { _uiState.value = ConsultationsUiState.Success(it) }
    }

    companion object {
        // Ce ViewModel a besoin d'un paramètre (l'id du patient) : la Factory est donc une fonction
        fun factory(patientId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PatientsApplication
                ConsultationViewModel(app.container.consultationRepository, patientId)
            }
        }
    }
}
