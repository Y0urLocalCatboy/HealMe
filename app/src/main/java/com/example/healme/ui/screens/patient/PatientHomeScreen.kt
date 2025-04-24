package com.example.healme.ui.screens.patient

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

/**
 * PatientHomeScreen is the home screen for patients after they log in.
 * It displays a TODO()
 *
 * @param navController The NavController used for navigation.
 */
@Composable
fun PatientHomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("You have successfully logged in.")
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo("welcome") { inclusive = true }
                }
            }
        ) {
            Text("Log Out")
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
