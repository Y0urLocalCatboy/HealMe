package com.example.healme.ui.screens.patient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.healme.data.models.MedicalHistory
import com.example.healme.data.network.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavController
import com.example.healme.R

/**
 * Composable function to display the Patient's Medical History screen.
 *
 * @param navController Navigation controller for navigating between screens.
 */
@Composable
fun PatientMedicalHistoryScreen(navController: NavController) {
    val firestore = remember { FirestoreClass() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var records by remember { mutableStateOf<List<MedicalHistory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            records = firestore.getPatientMedicalHistory(userId)
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Medical History", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (records.isEmpty()) {
            Text("No medical history records found.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(records.sortedByDescending { it.timestamp }) { record ->
                    val dateFormatted = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())
                        .format(Date(record.timestamp * 1000)) // convert to milliseconds

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Doctor: ${record.doctorName}", style = MaterialTheme.typography.titleSmall)
                            Text("Date: $dateFormatted", style = MaterialTheme.typography.bodySmall)
                            if (record.content.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Note: ${record.content}", style = MaterialTheme.typography.bodySmall)
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
            Text(stringResource(R.string.back_button))
        }
    }
}
