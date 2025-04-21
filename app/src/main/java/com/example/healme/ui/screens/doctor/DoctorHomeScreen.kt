package com.example.healme.ui.screens.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.healme.data.network.FirestoreClass
import com.example.healme.ui.components.menu.CalendarPicker

@Composable
fun DoctorHomeScreen(navController: NavHostController) {
    var showCalendar by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Button(onClick = { showCalendar = true }) {
                Text("Open Calendar Picker")
            }

            if (showCalendar) {
                AlertDialog(
                    onDismissRequest = { showCalendar = false },
                    confirmButton = {
                        TextButton(onClick = { showCalendar = false }) {
                            Text("Close")
                        }
                    },
                    title = { Text("Doctor Calendar") },
                    text = {
                        CalendarPicker("dNFkQwa9wqSrj0ZVDQEvrJ9si8T2")
                    }
                )
            }
        }
    }
}