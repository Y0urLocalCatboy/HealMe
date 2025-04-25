package com.example.healme.ui.screens.patient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BookingConfirmationScreen(doctorName: String, doctorSurname: String, timestamp: Long, navController: NavController) {
    val formattedDate = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())
        .format(Date(timestamp * 1000))

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Booking Confirmed!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Doctor: Dr. $doctorName $doctorSurname", style = MaterialTheme.typography.titleMedium)
        Text("Date: $formattedDate", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.popBackStack("patient_home", false) }) {
            Text("Back to Home")
        }
    }
}
