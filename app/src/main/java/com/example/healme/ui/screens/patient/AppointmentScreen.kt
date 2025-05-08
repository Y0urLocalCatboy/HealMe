package com.example.healme.ui.screens.patient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.healme.data.models.user.Doctor
import com.example.healme.data.network.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast


@Composable
fun AppointmentScreen(navController: NavController) {
    val context = LocalContext.current
    val firestore = remember { FirestoreClass() }
    val scope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var doctorAvailability by remember { mutableStateOf<Map<String, Map<Long, String>>>(emptyMap()) }
    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            doctors = firestore.getAllDoctors() ?: emptyList()
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
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Available Appointments", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back")
        }
    }
}
