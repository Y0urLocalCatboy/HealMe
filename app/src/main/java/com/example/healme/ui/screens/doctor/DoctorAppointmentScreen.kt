package com.example.healme.ui.screens.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.healme.data.network.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorAppointmentScreen(navController: NavController) {

    val firestore = remember { FirestoreClass() }
    val scope = rememberCoroutineScope()
    val doctorId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var appointments by remember { mutableStateOf<Map<String, Map<String, Any>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            appointments = firestore.getDoctorAppointments(doctorId) ?: emptyMap()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Appointments") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (appointments.isEmpty()) {
                Text("You have no upcoming appointments.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(appointments.entries.sortedBy { it.key }) { (key, value) ->
                        val patientName = value["patientId"]?.toString() ?: "Unknown"
                        val timestamp = (value["timestamp"] as? Long) ?: 0L
                        val formatted = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())
                            .format(Date(timestamp * 1000))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Patient: $patientName", style = MaterialTheme.typography.titleMedium)
                                Text("Date: $formatted", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
