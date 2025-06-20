package com.example.healme.ui.screens.patient

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.healme.R
import com.example.healme.data.models.user.Doctor
import com.example.healme.data.network.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable function to display the Appointment screen for patients.
 *
 * @param navController Navigation controller for navigating between screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentScreen(navController: NavController) {
    val context = LocalContext.current
    val firestore = remember { FirestoreClass() }
    val scope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var doctorAvailability by remember { mutableStateOf<Map<String, Map<Long, String>>>(emptyMap()) }
    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }

    val allSpecialitiesString = stringResource(id = R.string.appointment_screen_all_specialties)
    var selectedSpecialty by remember { mutableStateOf(allSpecialitiesString) }
    var specialties by remember { mutableStateOf<List<String>>(listOf(allSpecialitiesString)) }

    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            doctors = firestore.getAllDoctors() ?: emptyList()

            val allSpecialties = doctors.mapNotNull { it.specialization }.distinct().sorted()
            specialties = listOf(allSpecialitiesString) + allSpecialties

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

    val filteredDoctors = doctors.filter { doctor ->
        val matchesSearch = searchQuery.isEmpty() ||
                "${doctor.name} ${doctor.surname}".contains(searchQuery, ignoreCase = true)
        val matchesSpecialty = selectedSpecialty == allSpecialitiesString ||
                doctor.specialization == selectedSpecialty

        matchesSearch && matchesSpecialty
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.appointment_screen_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(stringResource(R.string.appointment_screen_search_doctor_label)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, stringResource(R.string.appointment_screen_search_icon_description)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = it }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                value = selectedSpecialty,
                onValueChange = {},
                label = { Text(stringResource(R.string.appointment_screen_specialization_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) }
            )

            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                specialties.forEach { specialty ->
                    DropdownMenuItem(
                        text = { Text(specialty) },
                        onClick = {
                            selectedSpecialty = specialty
                            isDropdownExpanded = false
                            selectedDoctor = null
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (selectedDoctor == null) {
            if (filteredDoctors.isEmpty()) {
                Text(
                    stringResource(R.string.appointment_screen_no_doctors_matching_criteria),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn {
                    items(filteredDoctors) { doctor ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedDoctor = doctor }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    stringResource(R.string.appointment_screen_doctor_prefix, doctor.name, doctor.surname),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    doctor.specialization,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        } else {
            val doctor = selectedDoctor!!
            val availability = doctorAvailability[doctor.id] ?: emptyMap()

            Button(
                onClick = { selectedDoctor = null },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(stringResource(R.string.appointment_screen_back_to_doctors_list_button))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Dr. ${doctor.name} ${doctor.surname}", style = MaterialTheme.typography.headlineSmall)
            Text(doctor.specialization, style = MaterialTheme.typography.titleSmall)

            Spacer(modifier = Modifier.height(16.dp))

            if (availability.isEmpty()) {
                Text(stringResource(R.string.appointment_screen_doctor_prefix, doctor.name, doctor.surname), style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(stringResource(R.string.appointment_screen_available_slots_title), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(availability.keys.sorted()) { timestamp ->
                        val date = Date(timestamp * 1000)
                        val formatted = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault()).format(date)

                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        firestore.bookVisit(doctor.id, userId, timestamp)

                                        val patientName = firestore.getCurrentPatientName(userId) ?: ""
                                        firestore.saveDoctorAppointment(doctor.id, patientName, timestamp) {
                                            navController.navigate("confirmation/${doctor.name}/${doctor.surname}/$timestamp")
                                        }



                                        navController.navigate("confirmation/${doctor.name}/${doctor.surname}/$timestamp")
                                    } catch (e: Exception) {
                                        Toast.makeText(context, context.getString(R.string.appointment_screen_booking_failed_toast), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text(stringResource(R.string.appointment_screen_book_slot_button, formatted))
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
            Text(stringResource(R.string.appointment_screen_back_button))
        }
    }
}
