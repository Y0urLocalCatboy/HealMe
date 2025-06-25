package com.example.healme.ui.screens.doctor

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.healme.R
import com.example.healme.data.models.user.Patient
import com.example.healme.data.network.FirestoreClass
import com.example.healme.viewmodel.DoctorViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import com.instacart.library.truetime.TrueTimeRx


/**
 * Composable function to display the Doctor's Home screen.
 *
 * @param navController Navigation controller for navigating between screens.
 * @param doctorViewModel ViewModel for managing doctor's data and operations.
 */
@Composable
fun DoctorHomeScreen(navController: NavHostController, doctorViewModel: DoctorViewModel = viewModel()) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var doctor by remember { mutableStateOf<MutableMap<String, Any?>?>(null) }
    var patients by remember { mutableStateOf<List<Patient>>(emptyList()) }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid.toString()
        doctor = doctorViewModel.getDoctorById(uid) as MutableMap<String, Any?>?
        patients = doctorViewModel.getDoctorsPatients(uid) ?: emptyList()
        doctorViewModel.cleanUpPastAppointments(uid)
    }

    DoctorHomeContent(
        doctor = doctor,
        patientList = patients,
        navController = navController,
        onScheduleClick = { currentUser?.uid?.let { navController.navigate("doctor_schedule/$it") } },
        onPatientsClick = { navController.navigate("doctor_patients") },
        onPrescriptionsClick = { navController.navigate("doctor_prescription") },
        onMessagesClick = { navController.navigate("doctor_chat") },
        onProfileClick = { navController.navigate("doctor_change_user") },
        onNewsletterClick = { navController.navigate("doctor_newsletter") },
        onAppointmentsClick = { navController.navigate("doctor_appointments") },
        onPastAppointmentsClick = { navController.navigate("doctor_past_appointments") },
        onLogoutClick = {
            auth.signOut()
            navController.navigate("splash") {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
        }
    )
}

/**
 * Composable function to display the content of the Doctor's Home screen.
 *
 * @param doctor Doctor's data as a map.
 * @param patientList List of patients to display.
 * @param navController Navigation controller for navigating between screens.
 * @param onScheduleClick Callback when the schedule button is clicked.
 * @param onPatientsClick Callback when the patients button is clicked.
 * @param onPrescriptionsClick Callback when the prescriptions button is clicked.
 * @param onMessagesClick Callback when the messages button is clicked.
 * @param onProfileClick Callback when the profile button is clicked.
 * @param onNewsletterClick Callback when the newsletter button is clicked.
 * @param onAppointmentsClick Callback when the appointments button is clicked.
 * @param onLogoutClick Callback when the logout button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorHomeContent(
    doctor: MutableMap<String, Any?>?,
    patientList: List<Patient> = emptyList(),
    navController: NavHostController,
    onScheduleClick: () -> Unit = {},
    onPatientsClick: () -> Unit = {},
    onPrescriptionsClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNewsletterClick: () -> Unit = {},
    onAppointmentsClick: () -> Unit = {},
    onPastAppointmentsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.doctor_panel_title), style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier.fillMaxWidth(),
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = onMessagesClick) {
                        Icon(Icons.Default.Email, contentDescription = stringResource(R.string.doctor_panel_chat), tint = MaterialTheme.colorScheme.onTertiary)
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = stringResource(R.string.doctor_panel_profile), tint = MaterialTheme.colorScheme.onTertiary)
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.Logout, contentDescription = stringResource(R.string.doctor_panel_logout), tint = MaterialTheme.colorScheme.onTertiary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onTertiary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WelcomeSection(
                    doctorName = doctor?.get("surname") as? String ?: "",
                    specialization = doctor?.get("specialization") as? String ?: "placeholder"
                )
            }
            item { UpcomingAppointmentsSection(navController) }
            item {
                DoctorFunctionsSection(
                    onScheduleClick = onScheduleClick,
                    onPatientsClick = onPatientsClick,
                    onPrescriptionsClick = onPrescriptionsClick,
                    onNewsletterClick = onNewsletterClick,
                    onAppointmentsClick = onAppointmentsClick,
                    onPastAppointmentsClick = onPastAppointmentsClick
                )
            }
            item { RecentPatientsSection(patientList) }
        }
    }
}

/**
 * Composable function to display the welcome section on the Doctor's Home screen.
 *
 * @param doctorName Name of the doctor.
 * @param specialization Specialization of the doctor.
 */
@Composable
private fun WelcomeSection(doctorName: String, specialization: String) {
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = when (LocalTime.now().hour) {
                    in 6..11 -> stringResource(R.string.doctor_panel_welcome_morning) + " " + doctorName
                    in 12..17 -> stringResource(R.string.doctor_panel_welcome_afternoon) + " " + doctorName
                    in 18..22 -> stringResource(R.string.doctor_panel_welcome_evening) + " " + doctorName
                    else -> stringResource(R.string.doctor_panel_welcome_night) + " " + doctorName
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if(specialization != "placeholder" && !specialization.isEmpty()) specialization else stringResource(R.string.doctor_panel_unspecified_specialization),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.doctor_panel_today) + "$today",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Composable function to display the upcoming appointments section on the Doctor's Home screen.
 *
 * @param navController Navigation controller for navigating to the appointments screen.
 */
@Composable
private fun UpcomingAppointmentsSection(navController: NavHostController) {
    val TAG = "UpcomingAppointments"

    val firestore = remember { FirestoreClass() }
    val scope = rememberCoroutineScope()
    val doctorId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    var appointments by remember { mutableStateOf<Map<String, Map<String, Any>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            Log.d(TAG, "Fetching appointments for doctorId: $doctorId")
            appointments = firestore.getDoctorAppointments(doctorId) ?: emptyMap()
            Log.d(TAG, "Fetched ${appointments.size} appointments")
            appointments.forEach { (id, data) ->
                Log.d(TAG, "Appointment ID: $id, Data: $data")
            }
            isLoading = false
        }
    }

    val nowMillis = try {
        com.instacart.library.truetime.TrueTimeRx.now().time
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    val startEpoch = nowMillis / 1000
    val endEpoch = startEpoch + 7 * 86400

    Log.d(TAG, "Time range: $startEpoch to $endEpoch (${Date(startEpoch * 1000)} to ${Date(endEpoch * 1000)})")

    val upcomingAppointments = appointments.values.filter {
        val timestamp = it["timestamp"] as? Long ?: return@filter false
        val isInRange = timestamp in startEpoch..endEpoch
        Log.d(TAG, "Checking appointment at $timestamp (${Date(timestamp * 1000)}), inRange: $isInRange")
        isInRange
    }.sortedBy { it["timestamp"] as Long }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.doctor_title_appointments),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    upcomingAppointments.isEmpty() -> {
                        Log.d(TAG, "No upcoming appointments found")
                        Text(
                            text = stringResource(R.string.doctor_panel_no_today_appointments),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        upcomingAppointments.forEachIndexed { index, appointment ->
                            val patientName = appointment["patientId"] as? String ?: ""
                            val time = (appointment["timestamp"] as? Long)?.let { ts ->
                                val date = Date(ts * 1000)
                                SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(date)
                            } ?: "--:--"

                            Log.d(TAG, "Displaying appointment for $patientName at $time")

                            AppointmentItem(time, patientName, "Upcoming visit")

                            if (index != upcomingAppointments.lastIndex) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { navController.navigate("doctor_appointments") },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(R.string.doctor_panel_see_more))
                        }
                    }
                }
            }
        }
    }
}


/**
 * Composable function to display a single appointment item in the upcoming appointments section.
 *
 * @param time The time of the appointment.
 * @param patientName The name of the patient.
 * @param reason The reason for the appointment.
 */
@Composable
private fun AppointmentItem(time: String, patientName: String, reason: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(50.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = patientName, fontWeight = FontWeight.Medium)
            Text(text = reason, style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.Info, contentDescription = stringResource(R.string.doctor_panel_apointment_details))
        }
    }
}

/**
 * Composable function to display the doctor's functions section on the Home screen.
 *
 * @param onScheduleClick Callback when the schedule button is clicked.
 * @param onPatientsClick Callback when the patients button is clicked.
 * @param onPrescriptionsClick Callback when the prescriptions button is clicked.
 * @param onNewsletterClick Callback when the newsletter button is clicked.
 * @param onAppointmentsClick Callback when the appointments button is clicked.
 */
@Composable
private fun DoctorFunctionsSection(
    onScheduleClick: () -> Unit,
    onPatientsClick: () -> Unit,
    onPrescriptionsClick: () -> Unit,
    onNewsletterClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onPastAppointmentsClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.doctor_panel_fast_menu),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FunctionButton(
                    title = stringResource(R.string.doctor_panel_schedule),
                    icon = Icons.Default.CalendarToday,
                    onClick = onScheduleClick,
                    modifier = Modifier.weight(1f)
                )
                FunctionButton(
                    title = stringResource(R.string.doctor_panel_patients),
                    icon = Icons.Default.Group,
                    onClick = onPatientsClick,
                    modifier = Modifier.weight(1f)
                )
                FunctionButton(
                    title = stringResource(R.string.doctor_panel_receipts),
                    icon = Icons.Default.Description,
                    onClick = onPrescriptionsClick,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FunctionButton(
                    title = stringResource(R.string.newsletter_title_patient),
                    icon = Icons.Default.Email,
                    onClick = onNewsletterClick,
                    modifier = Modifier.weight(1f)
                )
                FunctionButton(
                    title = stringResource(R.string.doctor_fast_menu_appointments),
                    icon = Icons.Default.Info,
                    onClick = onAppointmentsClick,
                    modifier = Modifier.weight(1f)
                )
                FunctionButton(
                    title = stringResource(R.string.pastappointments),
                    icon = Icons.Default.History,
                    onClick = onPastAppointmentsClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Composable function to display a button with an icon and title for the doctor's functions section.
 *
 * @param title The title of the button.
 * @param icon The icon to display on the button.
 * @param onClick Callback when the button is clicked.
 * @param modifier Modifier to apply to the button.
 */
@Composable
private fun FunctionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * Composable function to display the recent patients section on the Doctor's Home screen.
 *
 * @param patients List of recent patients to display.
 */
@Composable
private fun RecentPatientsSection(patients: List<Patient>){
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.doctor_panel_latest_patients),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(patients) { patient ->
                PatientCard(name = patient.name, lastVisit = patient.surname)
            }
        }
    }
}

/**
 * Composable function to display a card for a single patient.
 *
 * @param name The name of the patient.
 * @param lastVisit The last visit date of the patient.
 */
@Composable
private fun PatientCard(name: String, lastVisit: String) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.doctor_panel_last_visit),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = lastVisit,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}