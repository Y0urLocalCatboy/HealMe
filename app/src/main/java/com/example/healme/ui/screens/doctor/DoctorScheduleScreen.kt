package com.example.healme.ui.screens.doctor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healme.R
import com.example.healme.data.network.FirestoreClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(doctorId: String, onExit: () -> Unit) {
    val calendar = remember { Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) } }
    val weeks = remember {
        List(4) {
            calendar.time.also { calendar.add(Calendar.WEEK_OF_YEAR, 1) }
        }
    }

    var expanded by remember { mutableStateOf(false) }
    var selectedWeek by remember { mutableStateOf(weeks.firstOrNull()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar_screen_select_a_week)) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.calendar_screen_back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.green),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )

        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedWeek?.let {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.calendar_screen_week)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    weeks.forEach { week ->
                        DropdownMenuItem(
                            text = {
                                Text(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(week))
                            },
                            onClick = {
                                selectedWeek = week
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            selectedWeek?.let {
                Text("${stringResource(R.string.calendar_screen_selected_week)} ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)}")
                Spacer(modifier = Modifier.height(8.dp))
                AvailabilityPicker(startDate = it, firestore = FirestoreClass(), doctorId = doctorId)
            }

            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}

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

    val hours = (8..17).toList()
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.calendar_screen_hour),
                modifier = Modifier.width(70.dp),
                fontSize = 16.sp
            )
            days.forEach { day ->
                val dayLabel = SimpleDateFormat("EE", Locale.getDefault()).format(day).take(2)
                Text(
                    text = dayLabel,
                    modifier = Modifier
                        .width(45.dp)
                        .padding(horizontal = 2.dp),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        hours.forEach { hour ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$hour:00",
                    modifier = Modifier.width(70.dp),
                    fontSize = 16.sp
                )

                days.forEach { day ->
                    val timestamp = Calendar.getInstance().apply {
                        time = day
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis / 1000

                    val now = System.currentTimeMillis() / 1000
                    val status = availabilityMap[timestamp] ?: "unavailable"
                    val isBooked = status == "booked"
                    val isPast = timestamp < now

                    val backgroundColor = when {
                        isPast -> colorResource(id = R.color.dark_uneditable_gray)
                        isBooked -> darkerRed
                        status == "available" -> green
                        else -> gray
                    }

                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .padding(3.dp)
                            .background(
                                color = backgroundColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable(enabled = !isBooked && !isPast) {
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
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.size(26.dp).background(green))
            Text(" ${stringResource(R.string.calendar_screen_available)}", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(26.dp).background(gray))
            Text(" ${stringResource(R.string.calendar_screen_unavailable)}", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(26.dp).background(darkerRed))
            Text(" ${stringResource(R.string.calendar_screen_booked)}", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(26.dp).background(colorResource(id = R.color.dark_uneditable_gray)))
            Text(" ${stringResource(R.string.calendar_screen_past)}", fontSize = 14.sp)
        }
    }
}
