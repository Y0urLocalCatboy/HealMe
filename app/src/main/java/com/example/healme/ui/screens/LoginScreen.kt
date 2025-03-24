package com.example.healme.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth

/**
 * LoginScreen is the entry point for the login activity.
 * It sets up the navigation and handles the login logic.
 */
class LoginScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    var errorMessage by remember { mutableStateOf<String?>(null) }

                    LoginScreenContent(
                        onLogin = { email, password ->
                            val auth = FirebaseAuth.getInstance()
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        navController.navigate("home")
                                    } else {
                                        errorMessage = "Login failed: ${task.exception?.message}"
                                    }
                                }
                        },
                        onRegisterClick = { navController.navigate("register") },
                        errorMessage = errorMessage
                    )
                }
                composable("register") {
                    RegisterScreen(navController)
                }
                composable("home") {
                    Text("Welcome to Home Screen")
                }
            }
        }
    }
}

/**
 * LoginScreenContent displays the UI components for login, including email, password fields,
 * error messages, and buttons. It accepts callback functions for login and registration navigation.
 *
 * @param onLogin A function to handle login logic.
 * @param onRegisterClick A function to navigate to the registration screen.
 * @param errorMessage A string to display error messages during login.
 */
@Composable
fun LoginScreenContent(
    onLogin: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    errorMessage: String?
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log In")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Don't have an account? Register here",
            modifier = Modifier.clickable { onRegisterClick() },
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Preview of the LoginScreenContent composable, showing how the login screen would look
 * in the preview without any action functionality.
 */
@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreenContent(onLogin = { _, _ -> }, onRegisterClick = {}, errorMessage = null)
}
