package com.example.patients

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.patients.ui.consultations.ConsultationsScreen
import com.example.patients.ui.home.HomeScreen
import com.example.patients.ui.patients.PatientsScreen
import com.example.patients.ui.theme.PatientsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PatientsTheme { // nom généré par Android Studio
                // Navigation minimale (sans bibliothèque) : quel patient est ouvert ?
                // false = page d'accueil, true = liste des patients
                var showPatients by rememberSaveable { mutableStateOf(false) }
                var patientId by rememberSaveable { mutableStateOf<String?>(null) }
                var patientName by rememberSaveable { mutableStateOf("") }
                val openedId = patientId

                when {
                    openedId != null -> {
                        // Le bouton retour du téléphone ramène à la liste des patients
                        BackHandler { patientId = null }
                        ConsultationsScreen(
                            patientId = openedId,
                            patientName = patientName,
                            onBack = { patientId = null }
                        )
                    }
                    showPatients -> {
                        // ... et depuis la liste, à l'accueil
                        BackHandler { showPatients = false }
                        PatientsScreen(
                            onBack = { showPatients = false },
                            onOpenConsultations = { patient ->
                                patientId = patient.id
                                patientName = "${patient.prenom} ${patient.nom}"
                            }
                        )
                    }
                    else -> HomeScreen(onOpenPatients = { showPatients = true })
                }
            }
        }
    }
}
