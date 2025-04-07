package com.example.healme.ui.components.menu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.sp
import com.example.healme.data.network.FirestoreClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarPicker()
        }
    }
}

@Composable
fun CalendarPicker() {
    var showDialog by remember { mutableStateOf(false) }
    var selectedWeek by remember { mutableStateOf<Date?>(null) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { showDialog = true }) {
            Text("Select Week")
        }

        selectedWeek?.let {
            Text("Selected Week: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)}")
            AvailabilityPicker(startDate = it, firestore = FirestoreClass())
        }
    }

    if (showDialog) {
        WeekSelectionDialog(onDismiss = { showDialog = false }, onWeekSelected = { selectedWeek = it })
    }
}

@Composable
fun WeekSelectionDialog(onDismiss: () -> Unit, onWeekSelected: (Date) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val weeks = List(4) {
            calendar.time.also { calendar.add(Calendar.WEEK_OF_YEAR, 1) }
        }

        Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select a Week", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onWeekSelected(week)
                                onDismiss()
                            }
                            .padding(8.dp)
                    ) {
                        Text(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(week))
                    }
                }
            }
        }
    }
}

@Composable
fun AvailabilityPicker(startDate: Date, firestore: FirestoreClass) {
    val doctorId = "dNFkQwa9wqSrj0ZVDQEvrJ9si8T2"
    val calendar = Calendar.getInstance()
    calendar.time = startDate

    // Ensure we start from Monday
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val days = List(5) { index ->
        Calendar.getInstance().apply {
            time = calendar.time
            add(Calendar.DAY_OF_MONTH, index)
        }.time
    }

    val horizontalScrollState = rememberScrollState()

    Column(modifier = Modifier.padding(16.dp)) {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                "Hour",
                modifier = Modifier
                    .width(60.dp)
                    .padding(4.dp),
                fontSize = 12.sp
            )

            // Loop through the days and create a label for each day
            days.forEach { day ->
                val dayOfWeek = Calendar.getInstance().apply { time = day }.get(Calendar.DAY_OF_WEEK)
                val shortDay = when (dayOfWeek) {
                    Calendar.MONDAY -> "Mo"
                    Calendar.TUESDAY -> "Tu"
                    Calendar.WEDNESDAY -> "W"
                    Calendar.THURSDAY -> "Th"
                    Calendar.FRIDAY -> "Fr"
                    else -> ""
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shortDay,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hourly rows
        Column(modifier = Modifier.fillMaxSize()) {
            for (hourIndex in 0 until 11) {
                val hour = 8 + hourIndex

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        "$hour:00",
                        modifier = Modifier
                            .width(60.dp)
                            .padding(4.dp),
                        fontSize = 12.sp
                    )

                    // Create time slot boxes for each day
                    Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                        days.forEach { day ->
                            val timeSlot = Calendar.getInstance().apply {
                                time = day
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, 0)
                            }.time

                            val timestamp = timeSlot.time / 1000
                            var isAvailable by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(4.dp)
                                    .background(
                                        if (isAvailable) Color.Green else Color.Gray,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        isAvailable = !isAvailable
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val updateMap = mapOf(
                                                "weeklyAvailability.$timestamp.status" to if (isAvailable) "available" else "unavailable",
                                                "weeklyAvailability.$timestamp.timestamp" to timestamp
                                            )
                                            firestore.updateDoctorAvailability(doctorId, updateMap)
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Green)
            )
            Text(" Available", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Gray)
            )
            Text(" Unavailable", style = MaterialTheme.typography.bodySmall)
        }
    }
}
