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
import androidx.compose.runtime.*
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
import com.example.healme.data.network.FirestoreClass
import com.example.healme.viewmodel.PatientViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class VisitData(
    val doctorName: String,
    val specialization: String,
    val timestamp: Long
)

@Composable
fun PatientHomeScreen(
    navController: NavController,
    patientViewModel: PatientViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var patient by remember { mutableStateOf<Patient?>(null) }
    var upcomingVisit by remember { mutableStateOf<VisitData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val firestore = remember { FirestoreClass() }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            println("📡 Fetching patient info for UID: $uid")

            patientViewModel.getPatientById(uid) { fetchedPatient ->
                patient = fetchedPatient
                println("✅ Patient fetched: ${fetchedPatient?.name}")

                scope.launch {
                    val upcomingPair = firestore.getUpcomingVisitForPatient(uid)
                    println("🔥 Raw upcoming visit + doctor: $upcomingPair")

                    upcomingPair?.let { (visit, doctor) ->
                        println("🎯 Upcoming visit timestamp: ${visit.timestamp}")
                        println("👨‍⚕️ Doctor name: ${doctor.name} ${doctor.surname}")
                        println("🩺 Specialization: ${doctor.specialization}")

                        upcomingVisit = VisitData(
                            doctorName = "${doctor.name} ${doctor.surname}",
                            specialization = doctor.specialization ?: "General",
                            timestamp = visit.timestamp
                        )
                    } ?: println("⚠️ No upcoming visits found.")

                    isLoading = false
                }
            }
        } ?: run {
            println("❌ No current user UID found")
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
            upcomingVisit = upcomingVisit,
            onVisitDetailsClick = {
                navController.navigate("visit_detail/${upcomingVisit?.timestamp ?: 0}")
            },
            onFindDoctorClick = { navController.navigate("available_dates") },
            onMedicalHistoryClick = { navController.navigate("medical_history") },
            onPrescriptionsClick = { navController.navigate("patient_prescription") },
            onMessagesClick = { navController.navigate("chat") },
            onCalendarClick = { navController.navigate("calendar") }
        )
    }
}

@Composable
fun PatientHomeContent(
    patient: Patient?,
    upcomingVisit: VisitData?,
    onVisitDetailsClick: () -> Unit,
    onFindDoctorClick: () -> Unit,
    onPrescriptionsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onMedicalHistoryClick: () -> Unit,
    onCalendarClick: () -> Unit
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
                    onMedicalHistoryClick = onMedicalHistoryClick,
                    onCalendarClick = onCalendarClick
                )
            }

            item {
                if (upcomingVisit != null) {
                    UpcomingAppointmentCard(
                        visitData = upcomingVisit,
                        onDetailsClick = onVisitDetailsClick
                    )
                } else {
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
                            Text(
                                text = stringResource(R.string.patient_panel_no_upcoming_visits),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
    onMedicalHistoryClick: () -> Unit,
    onCalendarClick: () -> Unit
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
            Spacer(modifier = Modifier.width(12.dp))
            ActionButton(
                text = stringResource(R.string.patient_panel_my_calendar),
                icon = Icons.Filled.CalendarToday,
                onClick = onCalendarClick,
                modifier = Modifier.weight(1f)
            )
        }

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
fun UpcomingAppointmentCard(
    visitData: VisitData,
    onDetailsClick: () -> Unit
) {
    val appointmentDate = remember(visitData.timestamp) {
        SimpleDateFormat("EEEE, dd MMMM yyyy 'at' HH:mm", Locale.getDefault())
            .format(Date(visitData.timestamp * 1000))
    }

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
                        text = visitData.doctorName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = visitData.specialization,
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
                onClick = onDetailsClick,
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
