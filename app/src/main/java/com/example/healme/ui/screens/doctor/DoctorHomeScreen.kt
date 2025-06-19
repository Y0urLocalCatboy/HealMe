package com.example.healme.ui.screens.doctor

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
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.healme.R
import com.example.healme.data.models.user.Patient
import com.example.healme.viewmodel.DoctorViewModel
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalTime


@Composable
fun DoctorHomeScreen(navController: NavHostController,
                     doctorViewModel: DoctorViewModel = viewModel()
){
    val auth = FirebaseAuth.getInstance()

    val currentUser = auth.currentUser

    val coroutineScope = rememberCoroutineScope()
    var doctor by remember { mutableStateOf<MutableMap<String, Any?>?>(null) }
    var patients by remember { mutableStateOf<List<Patient>>(emptyList()) }

    LaunchedEffect(currentUser?.uid) {
        doctor = doctorViewModel.getDoctorById(currentUser?.uid.toString()) as MutableMap<String, Any?>?
        patients = doctorViewModel.getDoctorsPatients(currentUser?.uid.toString()) ?: emptyList()
    }
    DoctorHomeContent(
        doctor = doctor,
        patientList = patients,
        onScheduleClick = {
            currentUser?.uid?.let { uid ->
                navController.navigate("doctor_schedule/$uid")
            }
        },
        onPatientsClick = { navController.navigate("doctor_patients") },
        onPrescriptionsClick = { navController.navigate("doctor_prescription") },
        onMessagesClick = { navController.navigate("doctor_chat") },
        onProfileClick = { navController.navigate("doctor_change_user")},
        onNewsletterClick = { navController.navigate("doctor_newsletter")}
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorHomeContent(
    doctor: MutableMap<String, Any?>?,
    patientList: List<Patient> = emptyList(),
    onScheduleClick: () -> Unit = {},
    onPatientsClick: () -> Unit = {},
    onPrescriptionsClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNewsletterClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.doctor_panel_title), style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier.fillMaxWidth(),
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = onMessagesClick) {
                        Icon(Icons.Default.Email, contentDescription = stringResource(R.string.doctor_panel_chat))
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = stringResource(R.string.doctor_panel_profile))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
            item { TodayAppointmentsSection() }
            item {
                DoctorFunctionsSection(
                    onScheduleClick = onScheduleClick,
                    onPatientsClick = onPatientsClick,
                    onPrescriptionsClick = onPrescriptionsClick,
                    onNewsletterClick = onNewsletterClick
                )
            }
            item { RecentPatientsSection(patientList) }
        }
    }
}

@Composable
fun WelcomeSection(doctorName: String, specialization: String) {
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

@Composable
fun TodayAppointmentsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.doctor_panel_appointments),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                AppointmentItem("9:00", "Brr Brr Patapim", "Control visit")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                AppointmentItem("10:30", "Tripi Tropi Tropa Tripa", "Control visit 2")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                AppointmentItem("12:15", "Lilili lalila", "Tomek zrób w końcu te appointments żeby działało")

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.doctor_panel_see_more))
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(time: String, patientName: String, reason: String) {
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

@Composable
fun DoctorFunctionsSection(
    onScheduleClick: () -> Unit,
    onPatientsClick: () -> Unit,
    onPrescriptionsClick: () -> Unit,
    onNewsletterClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.doctor_panel_fast_menu),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

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
            FunctionButton(
                title = stringResource(R.string.newsletter_title_patient),
                icon = Icons.Default.Email,
                onClick = { onNewsletterClick() },
                modifier = Modifier.weight(1f)
            )
        }

    }
}

@Composable
fun FunctionButton(
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


@Composable
fun RecentPatientsSection(patients: List<Patient>){
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

@Composable
fun PatientCard(name: String, lastVisit: String) {
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