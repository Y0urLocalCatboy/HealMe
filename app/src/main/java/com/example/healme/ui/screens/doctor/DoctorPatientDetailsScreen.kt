package com.example.healme.ui.screens.doctor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.healme.R
import com.example.healme.data.models.MedicalHistory
import com.example.healme.data.models.user.Patient
import com.example.healme.viewmodel.DoctorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable function to display the details of a patient in the doctor's panel.
 *
 * @param navController The navigation controller to handle navigation actions.
 * @param patientId The ID of the patient whose details are to be displayed.
 * @param doctorViewModel The ViewModel instance for the doctor, used to fetch patient data.
 */
@Composable
fun DoctorPatientDetailsScreen(
    navController: NavHostController,
    patientId: String,
    doctorViewModel: DoctorViewModel = viewModel()
) {
    var patient by remember { mutableStateOf<Patient?>(null) }
    var medicalHistory by remember { mutableStateOf<List<MedicalHistory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(patientId) {
        try {
            isLoading = true
            val patientDataMap = doctorViewModel.getDoctorById(patientId)
            if (patientDataMap != null) {
                patient = Patient.fromMap(patientDataMap)
            } else {
                error = "Patient not found"
            }

            val history = doctorViewModel.getPatientMedicalHistory(patientId)
            if (history != null) {
                medicalHistory = history.sortedByDescending { it.timestamp }
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    DoctorPatientDetailsContent(
        patient = patient,
        medicalHistory = medicalHistory,
        isLoading = isLoading,
        error = error,
        onNavigateBack = { navController.popBackStack() }
    )
}

/**
 * Composable function to display the content of the DoctorPatientDetailsScreen.
 *
 * @param patient The patient whose details are being displayed.
 * @param medicalHistory The list of medical history records for the patient.
 * @param isLoading Boolean indicating if the data is currently being loaded.
 * @param error An optional error message to display if data loading fails.
 * @param onNavigateBack Callback function to handle navigation back action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPatientDetailsContent(
    patient: Patient?,
    medicalHistory: List<MedicalHistory>,
    isLoading: Boolean,
    error: String?,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(patient?.name ?: stringResource(R.string.doctor_panel_patient_details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(text = "Error: $error")
                }
            }
            patient != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        PatientInfoCard(patient = patient)
                    }
                    item {
                        Text(
                            text = stringResource(R.string.doctor_panel_medical_history),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }
                    if (medicalHistory.isEmpty()) {
                        item {
                            Text(stringResource(R.string.doctor_panel_no_medical_history_found))
                        }
                    } else {
                        items(medicalHistory) { historyItem ->
                            MedicalHistoryItem(historyItem = historyItem)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable function to display the patient's information in a card format.
 *
 * @param patient The patient whose information is to be displayed.
 */
@Composable
fun PatientInfoCard(patient: Patient) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "${patient.name} ${patient.surname}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(text = "Email: ${patient.email}", style = MaterialTheme.typography.bodyLarge)
            if (patient.dateOfBirth.isNotEmpty()) {
                Text(text = stringResource(R.string.doctor_patients_dob) + ": ${patient.dateOfBirth}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

/**
 * Composable function to display a single medical history item in a card format.
 *
 * @param historyItem The medical history item to be displayed.
 */
@Composable
fun MedicalHistoryItem(historyItem: MedicalHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val date = Date(historyItem.timestamp * 1000)
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            Text(
                text = format.format(date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = historyItem.content.ifEmpty { stringResource(R.string.doctor_panel_no_visit_summary) },
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.doctor_panel_doctor) + ": ${historyItem.doctorName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}