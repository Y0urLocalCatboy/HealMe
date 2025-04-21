package com.example.healme.ui.screens.mutual

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.healme.data.models.user.Patient
import com.example.healme.viewmodel.AuthViewModel
import com.example.healme.R

/**
 * Composable function for the Register screen.
 *
 * @param navController The NavController for navigation.
 * @param authViewModel The AuthViewModel for handling authentication logic.
 */
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val passwordMismatchError = stringResource(R.string.register_password_mismatch)
    val registrationSuccess = stringResource(R.string.register_registration_successful)

    val nameError = if (name.isNotEmpty()) authViewModel.nameValidity(name) else null
    val surnameError = if (surname.isNotEmpty()) authViewModel.surnameValidity(surname) else null
    val dobError = if (dateOfBirth.isNotEmpty()) authViewModel.ageValidity(dateOfBirth) else null
    val emailError = !Patterns.EMAIL_ADDRESS.matcher(email.toString().trim { it <= ' ' }).matches()
    val passwordError = if (password.isNotEmpty()) authViewModel.passwordValidity(password) else null
    val isFormValid = name.isNotEmpty() && surname.isNotEmpty() && email.isNotEmpty() &&
            dateOfBirth.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() &&
            nameError == null && surnameError == null && dobError == null && passwordError == null &&
            password == confirmPassword

    RegisterContent(
        name = name,
        surname = surname,
        email = email,
        dateOfBirth = dateOfBirth,
        password = password,
        confirmPassword = confirmPassword,
        errorMessage = errorMessage,
        nameError = nameError,
        surnameError = surnameError,
        dobError = dobError,
        passwordError = passwordError,
        emailError = emailError,
        isFormValid = isFormValid,
        onNameChange = { name = it },
        onSurnameChange = { surname = it },
        onEmailChange = { email = it },
        onDateOfBirthChange = { dateOfBirth = it },
        onPasswordChange = { password = it },
        onConfirmPasswordChange = { confirmPassword = it },
        onRegisterClick = {
            if (password != confirmPassword) {
                errorMessage = passwordMismatchError
                return@RegisterContent
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val newPatient = Patient(
                        id = userId,
                        email = email,
                        name = name,
                        surname = surname,
                        dateOfBirth = dateOfBirth
                    )
                    db.collection("patients").document(userId).set(newPatient)
                        .addOnSuccessListener {
                            errorMessage = registrationSuccess
                            navController.navigate("login")
                        }
                        .addOnFailureListener { e ->
                            errorMessage = "${e.message}"
                        }
                } else {
                    errorMessage = "${task.exception?.message}"
                }
            }
        },
        onLoginClick = { navController.navigate("login") }
    )
}

/**
 * Composable function for the content of the Register screen.
 *
 * @param name The user's name.
 * @param surname The user's surname.
 * @param email The user's email.
 * @param dateOfBirth The user's date of birth.
 * @param password The user's password.
 * @param confirmPassword The user's confirmed password.
 * @param errorMessage The error message to display, if any.
 * @param nameError The error message for the name field, if any.
 * @param surnameError The error message for the surname field, if any.
 * @param dobError The error message for the date of birth field, if any.
 * @param passwordError The error message for the password field, if any.
 * @param emailError Boolean indicating if there is an error with the email field.
 * @param isFormValid Boolean indicating if the form is valid.
 * @param onNameChange Callback for when the name changes.
 * @param onSurnameChange Callback for when the surname changes.
 * @param onEmailChange Callback for when the email changes.
 * @param onDateOfBirthChange Callback for when the date of birth changes.
 * @param onPasswordChange Callback for when the password changes.
 * @param onConfirmPasswordChange Callback for when the confirmed password changes.
 * @param onRegisterClick Callback for when the register button is clicked.
 * @param onLoginClick Callback for when the login button is clicked.
 */
@Composable
private fun RegisterContent(
    name: String,
    surname: String,
    email: String,
    dateOfBirth: String,
    password: String,
    confirmPassword: String,
    errorMessage: String?,
    nameError: String?,
    surnameError: String?,
    dobError: String?,
    passwordError: String?,
    emailError: Boolean,
    isFormValid: Boolean,
    onNameChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.register_register_word),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.register_name)) },
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        OutlinedTextField(
            value = surname,
            onValueChange = onSurnameChange,
            label = { Text(stringResource(R.string.register_surname)) },
            isError = surnameError != null,
            supportingText = { surnameError?.let { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(R.string.register_email)) },
            isError = emailError && email.isNotEmpty(),
            supportingText = { if(emailError && email.isNotEmpty()) Text(stringResource(R.string.register_invalid_email)) else null },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = {
                val validChars = it.filter { char -> char.isDigit() || char == '-' }
                onDateOfBirthChange(validChars)
            },
            label = { Text(stringResource(R.string.register_birthdate_long)) },
            isError = dobError != null,
            supportingText = { dobError?.let { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.register_password)) },
            isError = passwordError != null,
            supportingText = { passwordError?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text(stringResource(R.string.register_confirm_password)) },
            visualTransformation = PasswordVisualTransformation(),
            isError = password != confirmPassword && confirmPassword.isNotEmpty(),
            supportingText = {
                if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                    Text(stringResource(R.string.register_password_mismatch))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = onRegisterClick,
            enabled = isFormValid && !emailError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(stringResource(R.string.register_register_word))
        }

        TextButton(
            onClick = onLoginClick,
            shape = MaterialTheme.shapes.large
        ) {
            Text(stringResource(R.string.register_already_have_account))
        }
    }
}

/**
 * Preview function for the Register screen.
 * This function is used to display a preview of the RegisterScreen composable in the Android Studio preview.
 */
@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    RegisterContent(name = "", surname = "", email = "", dateOfBirth = "", password = "", confirmPassword = "",
        errorMessage = null, nameError = null, surnameError = null, dobError = null, passwordError = null,
        emailError = false, isFormValid = false, onNameChange = {}, onSurnameChange = {}, onEmailChange = {},
        onDateOfBirthChange = {}, onPasswordChange = {}, onConfirmPasswordChange = {}, onRegisterClick = {},
        onLoginClick = {})
}