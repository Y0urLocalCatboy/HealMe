package com.example.healme.ui.screens.admin

import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.util.*

@Composable
fun AdminNewsletterScreen(navController: NavHostController) {
    var message by remember { mutableStateOf("") }
    var sendToDoctors by remember { mutableStateOf(false) }
    var sendToPatients by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(-1) }
    var selectedMinute by remember { mutableStateOf(-1) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.navigate("admin") }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Send Newsletter",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = sendToDoctors,
                onCheckedChange = { sendToDoctors = it }
            )
            Text("Send to Doctors")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = sendToPatients,
                onCheckedChange = { sendToPatients = it }
            )
            Text("Send to Patients")
        }

        Button(onClick = {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _: TimePicker, hour: Int, minute: Int ->
                    selectedHour = hour
                    selectedMinute = minute
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }) {
            Text("Pick Time")
        }

        if (selectedHour != -1 && selectedMinute != -1) {
            Text("Selected time: %02d:%02d".format(selectedHour, selectedMinute))
        }

        Button(
            onClick = {
                // TODO: Replace with your Firebase logic
                println("Sending message: $message to ${if (sendToDoctors) "Doctors " else ""}${if (sendToPatients) "Patients" else ""} at $selectedHour:$selectedMinute")
                navController.popBackStack()
            },
            enabled = message.isNotBlank() && (sendToDoctors || sendToPatients)
        ) {
            Text("Send Newsletter")
        }
    }
}
