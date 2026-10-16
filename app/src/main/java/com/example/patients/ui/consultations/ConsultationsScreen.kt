@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.patients.ui.consultations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patients.data.model.Consultation
import com.example.patients.data.model.ConsultationRequest
import com.example.patients.ui.patients.ErrorContent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ConsultationsScreen(
    patientId: String,
    patientName: String,
    onBack: () -> Unit,
    // La clé distingue les ViewModels de patients différents
    viewModel: ConsultationViewModel = viewModel(
        key = "consultations-$patientId",
        factory = ConsultationViewModel.factory(patientId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    // id de la consultation en cours de modification (sauvegardé à la rotation)
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consultations – $patientName", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une consultation")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ConsultationsUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ConsultationsUiState.Error ->
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.load() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                is ConsultationsUiState.Success ->
                    ConsultationList(
                        consultations = state.consultations,
                        onClick = { editingId = it.id },
                        onDelete = { viewModel.deleteConsultation(it) }
                    )
            }
        }
    }

    if (showAddDialog) {
        ConsultationDialog(
            consultation = null,
            patientId = patientId,
            onDismiss = { showAddDialog = false },
            onConfirm = { request ->
                viewModel.addConsultation(request)
                showAddDialog = false
            }
        )
    }

    val editing = (uiState as? ConsultationsUiState.Success)
        ?.consultations?.firstOrNull { it.id == editingId }
    if (editing != null) {
        ConsultationDialog(
            consultation = editing,
            patientId = patientId,
            onDismiss = { editingId = null },
            onConfirm = { request ->
                viewModel.updateConsultation(editing.id, request)
                editingId = null
            }
        )
    }
}

@Composable
fun ConsultationList(
    consultations: List<Consultation>,
    onClick: (Consultation) -> Unit,
    onDelete: (Consultation) -> Unit
) {
    if (consultations.isEmpty()) {
        Text("Aucune consultation pour ce patient", modifier = Modifier.padding(24.dp))
    } else {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(consultations, key = { it.id }) {
                ConsultationCard(it, onClick = { onClick(it) }, onDelete = { onDelete(it) })
            }
        }
    }
}

@Composable
fun ConsultationCard(consultation: Consultation, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // "2026-10-05T00:00:00.000Z" -> "2026-10-05"
                Text(consultation.date.take(10), style = MaterialTheme.typography.labelLarge)
                Text(consultation.motif, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${consultation.medecin} · ${consultation.statut}",
                    style = MaterialTheme.typography.bodyMedium
                )
                consultation.traitement?.let {
                    Text("Traitement : $it", style = MaterialTheme.typography.bodyMedium)
                }
                consultation.notes?.let {
                    Text("Notes : $it", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer")
            }
        }
    }
}

private fun utcFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

// Boîte de dialogue partagée : ajout (consultation = null) ou modification
@Composable
fun ConsultationDialog(
    consultation: Consultation?,
    patientId: String,
    onDismiss: () -> Unit,
    onConfirm: (ConsultationRequest) -> Unit
) {
    var date by rememberSaveable(consultation?.id) { mutableStateOf(consultation?.date?.take(10) ?: today()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var motif by rememberSaveable(consultation?.id) { mutableStateOf(consultation?.motif ?: "") }
    var medecin by rememberSaveable(consultation?.id) { mutableStateOf(consultation?.medecin ?: "") }
    var traitement by rememberSaveable(consultation?.id) { mutableStateOf(consultation?.traitement ?: "") }
    var notes by rememberSaveable(consultation?.id) { mutableStateOf(consultation?.notes ?: "") }
    var statut by rememberSaveable(consultation?.id) { mutableStateOf(consultation?.statut ?: "En attente") }

    val formulaireValide = date.isNotBlank() && motif.isNotBlank() && medecin.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (consultation == null) "Nouvelle consultation" else "Modifier la consultation") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Date : $date") }
                OutlinedTextField(
                    value = motif, onValueChange = { motif = it },
                    label = { Text("Motif *") }, singleLine = true
                )
                OutlinedTextField(
                    value = medecin, onValueChange = { medecin = it },
                    label = { Text("Médecin *") }, singleLine = true
                )
                OutlinedTextField(
                    value = traitement, onValueChange = { traitement = it },
                    label = { Text("Traitement") }, singleLine = true
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = statut == "En attente",
                        onClick = { statut = "En attente" },
                        label = { Text("En attente") }
                    )
                    FilterChip(
                        selected = statut == "Terminée",
                        onClick = { statut = "Terminée" },
                        label = { Text("Terminée") }
                    )
                }
                if (!formulaireValide) {
                    Text(
                        "Le motif et le médecin sont obligatoires.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = formulaireValide,
                onClick = {
                    onConfirm(
                        ConsultationRequest(
                            patient = patientId,
                            date = date.trim(),
                            motif = motif.trim(),
                            traitement = traitement.trim().ifBlank { null },
                            medecin = medecin.trim(),
                            notes = notes.trim().ifBlank { null },
                            statut = statut
                        )
                    )
                }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = runCatching { utcFormat().parse(date)?.time }.getOrNull()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { date = utcFormat().format(Date(it)) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) { DatePicker(state = pickerState) }
    }
}
