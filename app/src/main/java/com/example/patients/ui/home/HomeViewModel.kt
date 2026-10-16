package com.example.patients.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.patients.PatientsApplication
import com.example.patients.data.repository.ConsultationRepository
import com.example.patients.data.repository.PatientRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val patientRepository: PatientRepository,
    private val consultationRepository: ConsultationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        // Si on a déjà des chiffres, on les garde affichés pendant l'actualisation
        if (_uiState.value !is HomeUiState.Success) _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            // Les deux requêtes partent en parallèle
            val (patientsResult, consultationsResult) = coroutineScope {
                val p = async { patientRepository.getPatients() }
                val c = async { consultationRepository.getAllConsultations() }
                p.await() to c.await()
            }

            val patients = patientsResult.getOrNull()
            val consultations = consultationsResult.getOrNull()
            if (patients == null || consultations == null) {
                val message = (patientsResult.exceptionOrNull() ?: consultationsResult.exceptionOrNull())
                    ?.message ?: "Erreur inconnue"
                _uiState.value = HomeUiState.Error(message)
                return@launch
            }

            // Pas de route « médecins » : on compte les noms distincts (sans casse ni espaces)
            val medecins = (patients.mapNotNull { it.medecin } + consultations.map { it.medecin })
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .toSet()

            _uiState.value = HomeUiState.Success(
                HomeStats(
                    patients = patients.size,
                    consultations = consultations.size,
                    medecins = medecins.size
                )
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PatientsApplication
                HomeViewModel(
                    app.container.patientRepository,
                    app.container.consultationRepository
                )
            }
        }
    }
}
