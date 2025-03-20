package com.example.healme

    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.Button
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.OutlinedTextField
    import androidx.compose.material3.Text
    import androidx.compose.material3.TextButton
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.text.input.PasswordVisualTransformation
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.navigation.NavController
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore
    import com.example.healme.firebase.user.Patient

@Composable
fun RegisterScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    RegisterContent(
        name = name,
        surname = surname,
        email = email,
        dateOfBirth = dateOfBirth,
        password = password,
        confirmPassword = confirmPassword,
        errorMessage = errorMessage,
        onNameChange = { name = it },
        onSurnameChange = { surname = it },
        onEmailChange = { email = it },
        onDateOfBirthChange = { dateOfBirth = it },
        onPasswordChange = { password = it },
        onConfirmPasswordChange = { confirmPassword = it },
        onRegisterClick = {
            if (password != confirmPassword) {
                errorMessage = "Passwords do not match"
                return@RegisterContent
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val newPatient = Patient(
                        medicalHistory = mutableListOf(),
                        messages = mutableListOf(),
                        id = userId,
                        email = email,
                        name = name,
                        surname = surname,
                        dateOfBirth = dateOfBirth
                    )
                    db.collection("patients").document(userId).set(newPatient)
                        .addOnSuccessListener {
                            errorMessage = "Registration successful! Please log in."
                            navController.navigate("login")
                        }
                        .addOnFailureListener { e ->
                            errorMessage = "Error saving user data: ${e.message}"
                        }
                } else {
                    errorMessage = "Registration failed: ${task.exception?.message}"
                }
            }
        },
        onLoginClick = { navController.navigate("login") }
    )
}  @Composable private fun RegisterContent( name: String,
                                            surname: String,
                                            email: String,
                                            dateOfBirth: String,
                                            password: String,
                                            confirmPassword: String,
                                            errorMessage: String?,
                                            onNameChange: (String) -> Unit,
                                            onSurnameChange: (String) -> Unit,
                                            onEmailChange: (String) -> Unit,
                                            onDateOfBirthChange: (String) -> Unit,
                                            onPasswordChange: (String) -> Unit,
                                            onConfirmPasswordChange: (String) -> Unit,
                                            onRegisterClick: () -> Unit,
                                            onLoginClick: () -> Unit ) {
    Column( modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally )
    { Text( text = "Register",
        fontSize = 34.sp,
        modifier = Modifier.padding(bottom = 16.dp) )
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Name") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )

    OutlinedTextField(
        value = surname,
        onValueChange = onSurnameChange,
        label = { Text("Surname") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )

    OutlinedTextField(
        value = dateOfBirth,
        onValueChange = onDateOfBirthChange,
        label = { Text("Birthdate") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = { Text("Confirm Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    errorMessage?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }

    Button(
        onClick = onRegisterClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("Register")
    }

    TextButton(onClick = onLoginClick) {
        Text("Already have an account? Log in")
    }
}
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    MaterialTheme {
        RegisterContent( name = "John",
            surname = "Doe",
            email = "john.doe@example.com",
            dateOfBirth = "01/01/1990",
            password = "password",
            confirmPassword = "password",
            errorMessage = null,
            onNameChange = {},
            onSurnameChange = {},
            onEmailChange = {},
            onDateOfBirthChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegisterClick = {},
            onLoginClick = {} )
    }
}