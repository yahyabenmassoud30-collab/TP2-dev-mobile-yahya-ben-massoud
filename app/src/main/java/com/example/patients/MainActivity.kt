package com.example.patients

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.patients.ui.patients.PatientsScreen
import com.example.patients.ui.theme.PatientsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PatientsTheme { // nom généré par Android Studio
                PatientsScreen()
            }
        }
    }
}
