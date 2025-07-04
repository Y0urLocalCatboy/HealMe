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

/**
 * Composable function to display the Patient's Home screen.
 *
 * @param navController Navigation controller for navigating between screens.
 * @param patientViewModel ViewModel for managing patient's data and operations.
 */
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
    var showVisitDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val firestore = remember { FirestoreClass() }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            patientViewModel.getPatientById(uid) { fetchedPatient ->
                patient = fetchedPatient
                scope.launch {
                    firestore.cleanUpPastVisits(uid)
                    val upcomingPair = firestore.getUpcomingVisitForPatient(uid)
                    upcomingPair?.let { (visit, doctor) ->
                        upcomingVisit = VisitData(
                            doctorName = "${doctor.name} ${doctor.surname}",
                            specialization = doctor.specialization ?: "General",
                            timestamp = visit.timestamp
                        )
                    }
                    isLoading = false
                }

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
            upcomingVisit = upcomingVisit,
            onVisitDetailsClick = { showVisitDialog = true },
            onFindDoctorClick = { navController.navigate("available_dates") },
            onMedicalHistoryClick = { navController.navigate("medical_history") },
            onPrescriptionsClick = { navController.navigate("patient_prescription") },
            onMessagesClick = { navController.navigate("chat") },
            onCalendarClick = { navController.navigate("calendar") },
            onNewsletterClick = { navController.navigate("patient_newsletter") }
        )

        upcomingVisit?.let { visit ->
            if (showVisitDialog) {
                VisitDetailDialog(
                    visitData = visit,
                    onDismiss = { showVisitDialog = false }
                )
            }
        }

    }
}

/**
 * Composable function to display the content of the Patient's Home screen.
 *
 * @param patient The patient data to display.
 * @param upcomingVisit The upcoming visit data to display.
 * @param onVisitDetailsClick Callback when the visit details button is clicked.
 * @param onFindDoctorClick Callback when the find doctor button is clicked.
 * @param onPrescriptionsClick Callback when the prescriptions button is clicked.
 * @param onMessagesClick Callback when the messages button is clicked.
 * @param onMedicalHistoryClick Callback when the medical history button is clicked.
 * @param onCalendarClick Callback when the calendar button is clicked.
 * @param onNewsletterClick Callback when the newsletter button is clicked.
 */
@Composable
fun PatientHomeContent(
    patient: Patient?,
    upcomingVisit: VisitData?,
    onVisitDetailsClick: () -> Unit,
    onFindDoctorClick: () -> Unit,
    onPrescriptionsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onMedicalHistoryClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onNewsletterClick: () -> Unit
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
                    onCalendarClick = onCalendarClick,
                    onNewsletterClick = onNewsletterClick
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

/**
 * Composable function to display the welcome section for the patient.
 *
 * @param patientName The name of the patient to display in the welcome message.
 */
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

/**
 * Composable function to display the quick actions grid for the patient.
 *
 * @param onFindDoctorClick Callback when the find doctor button is clicked.
 * @param onPrescriptionsClick Callback when the prescriptions button is clicked.
 * @param onMessagesClick Callback when the messages button is clicked.
 * @param onMedicalHistoryClick Callback when the medical history button is clicked.
 * @param onCalendarClick Callback when the calendar button is clicked.
 * @param onNewsletterClick Callback when the newsletter button is clicked.
 */
@Composable
fun QuickActionsGrid(
    onFindDoctorClick: () -> Unit,
    onPrescriptionsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onMedicalHistoryClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onNewsletterClick: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.patient_panel_quick_actions),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = stringResource(R.string.patient_panel_find_doctor),
                icon = Icons.Filled.Search,
                onClick = onFindDoctorClick,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = stringResource(R.string.patient_panel_my_calendar),
                icon = Icons.Filled.CalendarToday,
                onClick = onCalendarClick,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = stringResource(R.string.admin_newsletter),
                icon = Icons.Filled.Description,
                onClick = onNewsletterClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = stringResource(R.string.patient_panel_my_prescriptions),
                icon = Icons.Filled.Description,
                onClick = onPrescriptionsClick,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = stringResource(R.string.patient_panel_messages),
                icon = Icons.Filled.Chat,
                onClick = onMessagesClick,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = stringResource(R.string.patient_panel_medical_history),
                icon = Icons.Filled.CalendarToday,
                onClick = onMedicalHistoryClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Composable function to display an action button with an icon and text.
 *
 * @param text The text to display on the button.
 * @param icon The icon to display on the button.
 * @param onClick Callback when the button is clicked.
 * @param modifier Modifier to apply to the button.
 */
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

/**
 * Composable function to display the upcoming appointment card.
 *
 * @param visitData The visit data containing doctor name, specialization, and timestamp.
 * @param onDetailsClick Callback when the details button is clicked.
 */
@Composable
fun UpcomingAppointmentCard(
    visitData: VisitData,
    onDetailsClick: () -> Unit
) {
    val appointmentDate = remember(visitData.timestamp) {
        val timeMillis = try {
            com.instacart.library.truetime.TrueTime.now().time
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

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
                    imageVector = Icons.Filled.CalendarToday,
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


/**
 * Composable function to display the visit detail dialog.
 *
 * @param visitData The visit data containing doctor name, specialization, and timestamp.
 * @param onDismiss Callback when the dialog is dismissed.
 */
@Composable
fun VisitDetailDialog(
    visitData: VisitData,
    onDismiss: () -> Unit
) {
    val formattedDate = remember(visitData.timestamp) {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
            .format(Date(visitData.timestamp * 1000))
    }
    val formattedTime = remember(visitData.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(visitData.timestamp * 1000))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = {
            Text("Appointment Details")
        },
        text = {
            Column {
                Text("Doctor: ${visitData.doctorName}")
                Text("Specialty: ${visitData.specialization}")
                Text("Date: $formattedDate")
                Text("Time: $formattedTime")
            }
        }
    )
}

/**
 * Composable function to display a health tip card.
 *
 * This card displays a random health tip from a predefined list.
 */
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
