package com.example.healme.ui.screens.doctor

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.healme.R
import com.example.healme.viewmodel.DoctorViewModel
import com.google.firebase.auth.FirebaseAuth
import com.instacart.library.truetime.TrueTimeRx
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable screen to display all past appointments for the currently logged-in doctor.
 *
 * @param navController Used for navigation.
 * @param doctorViewModel ViewModel that provides access to Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPastAppointmentScreen(
    navController: NavHostController,
    doctorViewModel: DoctorViewModel = viewModel()
) {
    val doctorId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    var pastAppointments by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val allAppointments = doctorViewModel.getPastAppointments(doctorId) ?: emptyMap()
        val now = TrueTimeRx.now().toInstant().epochSecond

        pastAppointments = allAppointments.values.filter {
            val timestamp = it["timestamp"] as? Long ?: 0L
            timestamp < now
        }.sortedByDescending { it["timestamp"] as Long }

        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.doctor_past_appointments_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.doctor_past_appointments_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (pastAppointments.isEmpty()) {
                Text(stringResource(R.string.doctor_past_appointments_none))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pastAppointments) { appointment ->
                        PastAppointmentItem(appointment)
                    }
                }
            }
        }
    }
}

/**
 * Composable function to display a single past appointment item.
 *
 * @param appointment The appointment data to display.
 */
@Composable
private fun PastAppointmentItem(appointment: Map<String, Any>) {
    val patientId = appointment["patientId"] as? String ?: "Unknown"
    val timestamp = appointment["timestamp"] as? Long ?: 0L
    val dateTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp * 1000))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.doctor_past_appointments_patient_id, patientId), fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.doctor_past_appointments_date_time, dateTime))
        }
    }
}

