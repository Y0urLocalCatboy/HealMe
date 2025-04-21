package com.example.healme.ui.components.menu

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.healme.data.network.FirestoreClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        time = startDate
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    }

    val days = List(5) { index ->
        Calendar.getInstance().apply {
            time = calendar.time
            add(Calendar.DAY_OF_MONTH, index)
        }.time
    }

    val hours = (8..18).toList()
    val availabilityMap = remember { mutableStateMapOf<Long, Boolean>() }

    LaunchedEffect(Unit) {
        val userMap = firestore.loadUser(doctorId)
        val weeklyAvailability = userMap?.get("weeklyAvailability") as? Map<*, *>
        weeklyAvailability?.forEach { (_, value) ->
            val slot = value as? Map<*, *>
            val timestamp = (slot?.get("timestamp") as? Number)?.toLong()
            val status = slot?.get("status") as? String
            if (timestamp != null && status != null) {
                availabilityMap[timestamp] = status == "available"
            }
        }
    }

    val horizontalScrollState = rememberScrollState()

    Column(modifier = Modifier.padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Hour", modifier = Modifier.width(60.dp), fontSize = 12.sp)
            days.forEach { day ->
                val shortDay = SimpleDateFormat("EE", Locale.getDefault()).format(day).take(2)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = shortDay, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        hours.forEach { hour ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    "$hour:00",
                    modifier = Modifier.width(60.dp),
                    fontSize = 12.sp
                )

                Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                    days.forEach { day ->
                        val timestamp = Calendar.getInstance().apply {
                            time = day
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis / 1000

                        val isAvailable = availabilityMap[timestamp] ?: false

                        Box(
                            modifier = Modifier
                                .size(35.dp)
                                .padding(4.dp)
                                .background(
                                    color = if (isAvailable)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable {
                                    val newStatus = !isAvailable
                                    availabilityMap[timestamp] = newStatus

                                    val updateMap = mapOf(
                                        "weeklyAvailability.$timestamp.status" to if (newStatus) "available" else "unavailable",
                                        "weeklyAvailability.$timestamp.timestamp" to timestamp
                                    )

                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            firestore.updateDoctorAvailability(doctorId, updateMap)
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast
                                                    .makeText(context, "Failed to update availability", Toast.LENGTH_SHORT)
                                                    .show()
                                            }
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            Text(" Available", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Text(" Unavailable", style = MaterialTheme.typography.bodySmall)
        }
    }
}