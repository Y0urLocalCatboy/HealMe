import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healme.R
import com.example.healme.data.models.Prescription
import com.example.healme.data.models.user.Patient
import com.example.healme.data.models.user.User
import com.example.healme.viewmodel.DoctorViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun DoctorPrescriptionsScreen(
    navController: NavController,
    viewModel: DoctorViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedPatient by remember { mutableStateOf<User?>(null) }
    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showPatientSelector by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var patients by remember { mutableStateOf<List<Patient>>(emptyList()) }
    var prescriptions by remember { mutableStateOf<List<Prescription>?>(emptyList()) }

    LaunchedEffect(key1 = selectedPatient) {
        coroutineScope.launch {
            patients = viewModel.getDoctorsPatients(currentUser?.uid.toString()) ?: emptyList()
            if (selectedPatient != null) {
                prescriptions = viewModel.loadPrescriptions(selectedPatient?.id?:"") ?: emptyList()
            } else {
                prescriptions = emptyList()
            }
        }
    }

    val isFormValid = selectedPatient != null &&
            medicationName.isNotBlank() &&
            dosage.isNotBlank()

    DoctorPrescriptionsContent(
        onNavigateBack = { navController.popBackStack() },
        selectedPatient = selectedPatient,
        medicationName = medicationName,
        dosage = dosage,
        instructions = instructions,
        errorMessage = errorMessage,
        showPatientSelector = showPatientSelector,
        patients = patients,
        prescriptions = prescriptions,
        isFormValid = isFormValid,
        onMedicationNameChange = { medicationName = it },
        onDosageChange = { dosage = it },
        onInstructionsChange = { instructions = it },
        onShowPatientSelector = { showPatientSelector = it },
        onSelectPatient = { selectedPatient = it },
        onSavePrescription = { patient ->
            val prescription = Prescription(
                id = "placeholder",
                patientId = patient.id,
                medicationName = medicationName,
                dosage = dosage,
                instructions = instructions,
                dateIssued = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date()),
                doctorName = "Dr.",
                status = "Active"
            )
            coroutineScope.launch {
                try {
                    viewModel.savePrescription(prescription) { success, message ->
                        if (success) {
                            coroutineScope.launch {
                                prescriptions = viewModel.loadPrescriptions(selectedPatient?.id ?: "")
                            }
                        } else {
                            errorMessage = message ?: "Couldn't save prescription"
                        }
                    }
                    medicationName = ""
                    dosage = ""
                    instructions = ""
                    selectedPatient = null
                } catch (e: Exception) {
                    errorMessage = e.message ?: "PRESCRIPTION ERROR DR_PRES_SCREEN"
                }
            }
        }
    )
}

@Composable
fun DoctorPrescriptionsContent(
    onNavigateBack: () -> Unit,
    selectedPatient: User?,
    medicationName: String,
    dosage: String,
    instructions: String,
    errorMessage: String,
    showPatientSelector: Boolean,
    patients: List<Patient>,
    prescriptions: List<Prescription>?,
    isFormValid: Boolean,
    onMedicationNameChange: (String) -> Unit,
    onDosageChange: (String) -> Unit,
    onInstructionsChange: (String) -> Unit,
    onShowPatientSelector: (Boolean) -> Unit,
    onSelectPatient: (User) -> Unit,
    onSavePrescription: (User) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { onNavigateBack() },
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_arrow)
                )
            }

            Text(
                text = stringResource(R.string.doctor_prescriptions_title, ""),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.doctor_prescriptions_new_prescriptions, ""),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onShowPatientSelector(true) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = selectedPatient?.let { "${it.name} ${it.surname}" }
                                    ?: stringResource(R.string.doctor_prescriptions_select_patient, "")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = medicationName,
                    onValueChange = onMedicationNameChange,
                    label = { Text(stringResource(R.string.doctor_prescriptions_medication, "")) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dosage,
                    onValueChange = onDosageChange,
                    label = { Text(stringResource(R.string.doctor_prescriptions_dosage, "")) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = instructions,
                    onValueChange = onInstructionsChange,
                    label = { Text(stringResource(R.string.doctor_prescriptions_instructions, "")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = Color.Red, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        selectedPatient?.let { patient ->
                            onSavePrescription(patient)
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.doctor_prescriptions_issue_prescription, ""))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = selectedPatient?.let {
                stringResource(R.string.doctor_prescriptions_recent_prescription, "")
            } ?: stringResource(R.string.doctor_prescriptions_select_patient_first, ""),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (selectedPatient == null) {
                item {
                    Text(
                        text = stringResource(R.string.doctor_prescriptions_select_patient_first),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (prescriptions.isNullOrEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.doctor_prescriptions_no_prescriptions),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(prescriptions) { prescription ->
                    PrescriptionItem(prescription = prescription)
                }
            }
        }
    }

    if (showPatientSelector) {
        PatientSelectorDialog(
            patients = patients,
            onSelectPatient = {
                onSelectPatient(it)
                onShowPatientSelector(false)
            },
            onDismiss = { onShowPatientSelector(false) }
        )
    }
}

@Composable
fun PatientSelectorDialog(
    patients: List<User>,
    onSelectPatient: (User) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.doctor_prescriptions_select_patient, "")) },
        text = {
            LazyColumn {
                items(patients) { patient ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPatient(patient) }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("${patient.name} ${patient.surname}")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.edit_profile_cancel, ""))
            }
        }
    )
}

@Composable
fun PrescriptionItem(prescription: Prescription) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = prescription.medicationName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = prescription.dateIssued,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.doctor_prescriptions_dosage_value, "") + ": ${prescription.dosage}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (prescription.instructions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = prescription.instructions,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.doctor_prescriptions_patient, "") + ": ${prescription.patientId}",
                    style = MaterialTheme.typography.bodySmall
                )

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = prescription.status,
                            color = when(prescription.status) {
                                "Active" -> Color(0xFF1B5E20)
                                "Filled" -> Color(0xFF0D47A1)
                                "Expired" -> Color(0xFFB71C1C)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when(prescription.status) {
                            "Active" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            "Filled" -> Color(0xFF2196F3).copy(alpha = 0.2f)
                            "Expired" -> Color(0xFFF44336).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
            }
        }
    }
}