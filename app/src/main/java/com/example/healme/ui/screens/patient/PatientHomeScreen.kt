package com.example.healme.ui.screens.patient

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.healme.data.network.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PatientHomeScreen(navController: NavController) {
    val context = LocalContext.current
    val firestore = remember { FirestoreClass() }
    val scope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var doctorAvailability by remember { mutableStateOf<Map<String, Map<Long, String>>>(emptyMap()) }
    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }

    // Fetch doctors and their slots
    LaunchedEffect(Unit) {
        doctors = firestore.getAllDoctors()
        val availabilityMap = mutableMapOf<String, Map<Long, String>>()

        for (doctor in doctors) {
            val availability = firestore.getDoctorAvailability(doctor.id)
            val booked = firestore.getBookedTimestampsForDoctor(doctor.id)
            val freeSlots = availability.filter { (ts, status) ->
                status == "available" && ts !in booked
            }
            availabilityMap[doctor.id] = freeSlots
        }

        doctorAvailability = availabilityMap
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Welcome!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("calendar") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("View My Calendar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        doctors.forEach { doctor ->
            val availability = doctorAvailability[doctor.id] ?: emptyMap()

            Card(modifier = Modifier.padding(8.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Dr. ${doctor.name} ${doctor.surname}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (availability.isEmpty()) {
                        Text("No available slots", style = MaterialTheme.typography.bodySmall)
                    } else {
                        availability.keys.sorted().forEach { timestamp ->
                            val date = Date(timestamp * 1000)
                            val formatted = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault()).format(date)

                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            firestore.bookVisit(doctor.id, userId, timestamp)
                                            navController.navigate("confirmation/${doctor.name}/${doctor.surname}/$timestamp")
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Booking failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text("Book: $formatted")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo("welcome") { inclusive = true }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Log Out")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PatientHomeScreenPreview() {
    PatientHomeScreen(navController = rememberNavController())
}
