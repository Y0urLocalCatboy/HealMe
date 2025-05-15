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
import com.example.healme.data.models.user.Doctor
import com.example.healme.data.network.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch
import java.util.Date
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


/**
 * PatientHomeScreen is the home screen for patients after they log in.
 * It displays a TODO()
 *
 * @param navController The NavController used for navigation.
 */
@Composable
fun PatientHomeScreen(navController: NavController) {
    val context = LocalContext.current
    val firestore = remember { FirestoreClass() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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

        Button(
            onClick = { navController.navigate("available_dates") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Book an Appointment")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo("patient") { inclusive = true }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Log Out")
        }

        Button(
            onClick = { navController.navigate("medical_history") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("View Medical History")
        }

    }
}

/**
 * Preview of the PatientHomeScreen.
 */
@Preview(showBackground = true)
@Composable
fun PatientHomeScreenPreview() {
    PatientHomeScreen(navController = rememberNavController())
}
