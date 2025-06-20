package com.example.healme.ui.screens.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healme.R
import com.example.healme.data.models.Prescription
import com.example.healme.ui.theme.Crimson
import com.example.healme.ui.theme.DarkGreen
import com.example.healme.ui.theme.DarkTurquoise
import com.example.healme.viewmodel.PatientViewModel
import com.google.firebase.auth.FirebaseAuth

/** Composable function to display the Patient's Prescriptions screen.
 *
 * @param navController Navigation controller for navigating between screens.
 * @param patientViewModel ViewModel for managing patient's data and operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientPrescriptionsScreen(
    navController: NavController,
    patientViewModel: PatientViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    var prescriptions by remember { mutableStateOf<List<Prescription>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val noDataMessage = stringResource(R.string.patient_prescriptions_no_data)
    val errorLoadingMessage = stringResource(R.string.patient_prescriptions_error_message)

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            isLoading = true
            errorMessage = null
            patientViewModel.getPrescriptionsForPatient(currentUserId) { result ->
                prescriptions = result
                isLoading = false
                if (result == null) {
                    errorMessage = noDataMessage
                }
            }
        } else {
            isLoading = false
            errorMessage = errorLoadingMessage
        }
    }

    Scaffold{ paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.patient_prescriptions_loading))
                    }
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: stringResource(R.string.patient_prescriptions_error),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                prescriptions.isNullOrEmpty() -> {
                    Text(
                        text = stringResource(R.string.patient_prescriptions_no_prescriptions),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(prescriptions!!) { prescription ->
                            PrescriptionCard(prescription = prescription)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable function to display a single prescription card.
 *
 * @param prescription The prescription data to display.
 */
@Composable
fun PrescriptionCard(prescription: Prescription) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = prescription.medicationName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(label = stringResource(R.string.patient_prescriptions_issued_by, prescription.doctorName))
            InfoRow(label = stringResource(R.string.patient_prescriptions_date_issued, prescription.dateIssued))
            InfoRow(label = stringResource(R.string.patient_prescriptions_dosage, prescription.dosage))

            if (prescription.instructions.isNotBlank()) {
                InfoRow(label = stringResource(R.string.patient_prescriptions_instructions, prescription.instructions))
            }

            Spacer(modifier = Modifier.height(8.dp))

            AssistChip(
                onClick = {},
                label = { Text(stringResource(R.string.patient_prescriptions_status, prescription.status)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = when (prescription.status.lowercase()) {
                        "Active" -> DarkGreen.copy(alpha = 0.2f)
                        "Filled" -> DarkTurquoise.copy(alpha = 0.2f)
                        "Expired" -> Crimson.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    labelColor = when (prescription.status.lowercase()) {
                        "Active" -> DarkGreen
                        "Filled" -> DarkTurquoise
                        "Expired" -> Crimson
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String? = null) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}