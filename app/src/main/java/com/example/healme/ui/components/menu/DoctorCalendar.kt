package com.example.healme.ui.components.menu

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.healme.data.network.FirestoreClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.colorResource
import com.example.healme.R

/**
 * Composable function to display a calendar picker for selecting a week and managing doctor availability.
 *
 * @param doctorId The ID of the doctor whose availability is being managed.
 * @param onExit Callback function to handle exit action.
 */
@Composable
fun CalendarPicker(doctorId: String, onExit: () -> Unit ) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedWeek by remember { mutableStateOf<Date?>(null) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { showDialog = true }) {
            Text("Select Week")
        }

        selectedWeek?.let {
            Text("Selected Week: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)}")
            AvailabilityPicker(startDate = it, firestore = FirestoreClass(), doctorId = doctorId)
        }

        Button(
            onClick = onExit,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Exit")
        }

        if (showDialog) {
            WeekSelectionDialog(
                onDismiss = { showDialog = false },
                onWeekSelected = {
                    selectedWeek = it
                    showDialog = false
                }
            )
        }
    }

    if (showDialog) {
        WeekSelectionDialog(onDismiss = { showDialog = false }, onWeekSelected = { selectedWeek = it })
    }
}

/**
 * Composable function to display a dialog for selecting a week.
 *
 * @param onDismiss Callback function to handle dismiss action.
 * @param onWeekSelected Callback function to handle week selection.
 */
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

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

/**
 * Composable function to display a picker for managing doctor availability.
 *
 * @param startDate The start date of the week for which availability is being managed.
 * @param firestore Instance of FirestoreClass to interact with Firestore.
 * @param doctorId The ID of the doctor whose availability is being managed.
 */
@Composable
fun AvailabilityPicker(startDate: Date, firestore: FirestoreClass, doctorId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val green = colorResource(id = R.color.green)
    val gray = colorResource(id = R.color.gray)
    val darkerRed = colorResource(id = R.color.darker_red)

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
    val availabilityMap = remember { mutableStateMapOf<Long, String>() }
    val bookedTimestamps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(doctorId) {
        try {
            val doctorAvailability = firestore.getDoctorAvailability(doctorId)
            doctorAvailability.forEach { (timestamp, status) ->
                availabilityMap[timestamp] = status
            }

            val booked = firestore.getBookedTimestampsForDoctor(doctorId)
            bookedTimestamps.clear()
            bookedTimestamps.addAll(booked)

            booked.forEach { timestamp ->
                availabilityMap[timestamp] = "booked"
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load availability or bookings", Toast.LENGTH_SHORT).show()
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

                        val status = availabilityMap[timestamp] ?: "unavailable"
                        val isBooked = status == "booked"

                        val backgroundColor = when {
                            isBooked -> darkerRed
                            status == "available" -> green
                            else -> gray
                        }

                        Box(
                            modifier = Modifier
                                .size(35.dp)
                                .padding(4.dp)
                                .background(
                                    color = backgroundColor,
                                    shape = MaterialTheme.shapes.small
                                )
                                .then(
                                    if (!isBooked) Modifier.clickable {
                                        val newStatus = if (status == "available") "unavailable" else "available"
                                        availabilityMap[timestamp] = newStatus

                                        val updateMap = mapOf(
                                            "weeklyAvailability.$timestamp.status" to newStatus,
                                            "weeklyAvailability.$timestamp.timestamp" to timestamp
                                        )

                                        coroutineScope.launch {
                                            try {
                                                firestore.updateDoctorAvailability(doctorId, updateMap)
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed to update availability",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    } else Modifier
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(green)
            )
            Text(" Available", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(gray)
            )
            Text(" Unavailable", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(darkerRed)
            )
            Text(" Booked", style = MaterialTheme.typography.bodySmall)
        }
    }
}
