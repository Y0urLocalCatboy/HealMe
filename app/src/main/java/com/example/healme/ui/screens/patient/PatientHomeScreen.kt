package com.example.healme.ui.screens.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healme.R
import com.example.healme.data.models.user.Patient
import com.example.healme.viewmodel.PatientViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PatientHomeScreen is the home screen for patients after they log in.
 *
 * @param navController The NavController used for navigation.
 * @param patientViewModel The ViewModel for patient data.
 */
@Composable
fun PatientHomeScreen(
    navController: NavController,
    patientViewModel: PatientViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var patient by remember { mutableStateOf<Patient?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let {
            patientViewModel.getPatientById(it) { fetchedPatient ->
                patient = fetchedPatient
                isLoading = false
            }
        } ?: run {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        PatientHomeContent(
            patient = patient,
            onFindDoctorClick = { navController.navigate("available_dates") },
            onMedicalHistoryClick = { navController.navigate("medical_history") },
            onPrescriptionsClick = { navController.navigate("patient_prescription") },
            onMessagesClick = { navController.navigate("chat") },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHomeContent(
    patient: Patient?,
    onFindDoctorClick: () -> Unit,
    onPrescriptionsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onMedicalHistoryClick: () -> Unit,
) {
    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                WelcomePatientSection(patientName = patient?.name ?: stringResource(R.string.patient_panel_default_name))
            }

            item {
                QuickActionsGrid(
                    onFindDoctorClick = onFindDoctorClick,
                    onPrescriptionsClick = onPrescriptionsClick,
                    onMessagesClick = onMessagesClick,
                    onMedicalHistoryClick = onMedicalHistoryClick
                )
            }

            item {
                UpcomingAppointmentCard() //Placeholder
            }

            item {
                HealthTipCard()
            }
        }
    }
}

@Composable
fun WelcomePatientSection(patientName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "${stringResource(R.string.patient_panel_welcome_back)}, $patientName!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.patient_panel_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickActionsGrid(
    onFindDoctorClick: () -> Unit,
    onPrescriptionsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onMedicalHistoryClick: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.patient_panel_quick_actions),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ActionButton(
                text = stringResource(R.string.patient_panel_find_doctor),
                icon = Icons.Filled.Search,
                onClick = onFindDoctorClick,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ActionButton(
                text = stringResource(R.string.patient_panel_my_prescriptions),
                icon = Icons.Filled.Description,
                onClick = onPrescriptionsClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            ActionButton(
                text = stringResource(R.string.patient_panel_messages),
                icon = Icons.Filled.Chat,
                onClick = onMessagesClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            ActionButton(
                text = stringResource(R.string.patient_panel_medical_history),
                icon = Icons.Filled.CalendarToday,
                onClick = onMedicalHistoryClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun UpcomingAppointmentCard() {
    // Placeholder!!!!!!!!!!!!!!!!!!!!!
    val appointmentDate = SimpleDateFormat("EEEE, dd MMMM yyyy 'at' HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() + 86400000 * 2)) // Example: 2 days from now
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.patient_panel_upcoming_appointment),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CalendarToday,
                    contentDescription = stringResource(R.string.patient_panel_appointment_icon),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Dr. Greogry House", // Placeholder
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "PSYCHIATSITS", // Placeholder
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = appointmentDate,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { TODO("Appoitnmen details") },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.patient_panel_details))
            }
        }
    }
}

@Composable
fun HealthTipCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.patient_panel_health_tip_of_the_day),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    id = remember {
                        val healthTips = listOf(
                            R.string.patient_panel_health_tip_placeholder_first,
                            R.string.patient_panel_health_tip_placeholder_second,
                            R.string.patient_panel_health_tip_placeholder_third,
                            R.string.patient_panel_health_tip_placeholder_fourth,
                            R.string.patient_panel_health_tip_placeholder_fifth,
                            R.string.patient_panel_health_tip_placeholder_sixth,
                            R.string.patient_panel_health_tip_placeholder_seventh
                        )
                        healthTips.random()
                    }
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}