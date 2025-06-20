package com.example.healme.ui.screens.doctor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import com.example.healme.data.models.user.Patient
import com.example.healme.viewmodel.DoctorViewModel
import com.google.firebase.auth.FirebaseAuth

/**Composable function to display the Doctor's Patients screen.
 *
 * @param navController Navigation controller for navigating between screens.
 * @param doctorViewModel ViewModel for managing doctor's data and operations.
 */
@Composable
fun DoctorPatientsScreen(
    navController: NavHostController,
    doctorViewModel: DoctorViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var patients by remember { mutableStateOf<List<Patient>>(emptyList()) }

    LaunchedEffect(currentUser?.uid) {
        patients = doctorViewModel.getDoctorsPatients(currentUser?.uid.toString()) ?: emptyList()
    }

    DoctorPatientsContent(
        patientList = patients,
        onNavigateBack = { navController.popBackStack() },
        onPatientClick = { patientId ->
            navController.navigate("doctor_patient_details/$patientId")
        }
    )
}

/**
 * Composable function to display the content of the Doctor's Patients screen.
 *
 * @param patientList List of patients to display.
 * @param onNavigateBack Callback to navigate back to the previous screen.
 * @param onPatientClick Callback when a patient is clicked, passing the patient's ID.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPatientsContent(
    patientList: List<Patient>,
    onNavigateBack: () -> Unit,
    onPatientClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.doctor_panel_patients)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        if (patientList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.doctor_patients_no_patients))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(patientList) { patient ->
                    PatientListItem(patient = patient, onPatientClick = { onPatientClick(patient.id) })
                }
            }
        }
    }
}

/**
 * Composable function to display a single patient item in the list.
 *
 * @param patient The patient data to display.
 * @param onPatientClick Callback when the patient item is clicked.
 */
@Composable
fun PatientListItem(
    patient: Patient,
    onPatientClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onPatientClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${patient.name} ${patient.surname}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.doctor_patients_email) + ": ${patient.email}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (patient.dateOfBirth.isNotEmpty()){
                    Text(
                        text = stringResource(R.string.doctor_patients_dob) + ": ${patient.dateOfBirth}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            IconButton(onClick = onPatientClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.doctor_patient_details_icon_description)
                )
            }
        }
    }
}