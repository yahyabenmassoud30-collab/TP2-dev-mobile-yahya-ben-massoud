package com.example.patients.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.patients.PatientsApplication
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
import com.example.patients.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PatientViewModel(
    private val repository: PatientRepository
) : ViewModel() {

    // État de l'écran : modifiable ici, lecture seule pour l'UI
    private val _uiState = MutableStateFlow<PatientsUiState>(PatientsUiState.Loading)
    val uiState: StateFlow<PatientsUiState> = _uiState.asStateFlow()

    // Messages ponctuels à afficher une seule fois (Snackbar)
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    // Exercice 2 : filtre de statut choisi (null = Tous)
    private val _filter = MutableStateFlow<String?>(null)
    val filter: StateFlow<String?> = _filter.asStateFlow()

    init {
        loadPatients()
    }

    fun loadPatients() {
        _uiState.value = PatientsUiState.Loading
        viewModelScope.launch {
            repository.getPatients(_filter.value)
                .onSuccess { patients -> _uiState.value = PatientsUiState.Success(patients) }
                .onFailure { e -> _uiState.value = PatientsUiState.Error(e.message ?: "Erreur inconnue") }
        }
    }

    // Exercice 2
    fun setFilter(statut: String?) {
        if (statut == _filter.value) return
        _filter.value = statut
        loadPatients()
    }

    fun addPatient(request: PatientRequest) {
        viewModelScope.launch {
            repository.createPatient(request)
                .onSuccess { patient ->
                    // Un nouveau patient est « Actif » par défaut : ne l'affiche
                    // que si le filtre courant le permet.
                    val f = _filter.value
                    if (f == null || f == patient.statut) updateList { it + patient }
                    _messages.emit("${patient.prenom} ${patient.nom} ajouté")
                }
                .onFailure { e -> _messages.emit(e.message ?: "Ajout impossible") }
        }
    }

    // Exercice 1
    fun updatePatient(id: String, request: PatientRequest) {
        viewModelScope.launch {
            repository.updatePatient(id, request)
                .onSuccess { patient ->
                    updateList { list -> list.map { if (it.id == patient.id) patient else it } }
                    _messages.emit("${patient.prenom} ${patient.nom} modifié")
                }
                .onFailure { e -> _messages.emit(e.message ?: "Modification impossible") }
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            repository.deletePatient(patient.id)
                .onSuccess {
                    updateList { list -> list.filterNot { it.id == patient.id } }
                    _messages.emit("${patient.prenom} ${patient.nom} supprimé")
                }
                .onFailure { e -> _messages.emit(e.message ?: "Suppression impossible") }
        }
    }

    // Modifie la liste affichée sans recharger depuis le serveur
    private fun updateList(transform: (List<Patient>) -> List<Patient>) {
        _uiState.update { state ->
            if (state is PatientsUiState.Success) state.copy(patients = transform(state.patients))
            else state
        }
    }

    // Indique à Android comment construire ce ViewModel (il a besoin du repository)
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PatientsApplication
                PatientViewModel(app.container.patientRepository)
            }
        }
    }
}
