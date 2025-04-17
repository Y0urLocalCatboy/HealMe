package com.example.healme.ui.screens.mutual

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healme.R
import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreClass
import com.example.healme.viewmodel.ChangeUserViewModel
import com.example.healme.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ChangeUserScreen(navController: NavController,
                     authViewModel: AuthViewModel = viewModel()
){
    val fs = FirestoreClass()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val coroutineScope = rememberCoroutineScope()

    var user by remember { mutableStateOf<MutableMap<String, Any?>?>(null) }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var isDoctor by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(" ") }
    var errorMessage by remember { mutableStateOf("") }

    var specialization by remember { mutableStateOf("") }

    val nameError = if (name.isNotEmpty()) authViewModel.nameValidity(name) else null
    val surnameError = if (surname.isNotEmpty()) authViewModel.surnameValidity(surname) else null
    val dobError = if (dateOfBirth.isNotEmpty()) authViewModel.ageValidity(dateOfBirth) else null

    val isFormValid = name.isNotEmpty() && surname.isNotEmpty() &&
            email.isNotEmpty() && dateOfBirth.isNotEmpty() &&
            nameError == null && surnameError == null && dobError == null

    var dataLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(currentUser?.uid) {
        if (!dataLoaded) {
            val userData = fs.loadUser(currentUser?.uid ?: "")
            user = userData?.toMutableMap()
            isDoctor = user?.get("specialization") != null
            if(isDoctor) {
                specialization = user?.get("specialization") as String? ?: ""
            }
            name = user?.get("name") as? String ?: ""
            surname = user?.get("surname") as? String ?: ""
            email = user?.get("email") as? String ?: ""
            dateOfBirth = user?.get("dateOfBirth") as? String ?: ""
            dataLoaded = true
        }
    }

    ChangeUserContent(
        isDoctor = isDoctor,
        name = name,
        nameError = nameError,
        surname = surname,
        surnameError = surnameError,
        email = email,
        dateOfBirth = dateOfBirth,
        dobError = dobError,
        specialization = specialization,
        showError = showError.isNotEmpty(),
        errorMessage = errorMessage,
        isFormValid = isFormValid,
        onNameChange = { name = it },
        onSurnameChange = { surname = it },
        onEmailChange = { email = it },
        onDateOfBirthChange = { dateOfBirth = it },
        onSpecializationChange = { specialization = it },
        onSaveClick = {
            if (isFormValid) {
                val updateData = mapOf(
                    "name" to name,
                    "surname" to surname,
                    "email" to email,
                    "dateOfBirth" to dateOfBirth
                ).toMutableMap()

                if (isDoctor) {
                    updateData["specialization"] = specialization
                }

                try {
                    coroutineScope.launch {
                        try {
                            fs.updateUser(User.fromMap(user as Map<String, Any>), updateData)
                            showError = ""
                        } catch (e: Exception) {
                            showError = "error"
                            errorMessage = e.message ?: "it shouldn't happen (onSaveClick changeuserScreen)"
                        }
                    }
                } catch (e: Exception) {
                    showError = "error"
                    errorMessage = e.message ?: "it shouldn't happen (DITTO changeuserScreen)"
                }
            }
        }
    )
}

@Composable
fun ChangeUserContent(
    isDoctor: Boolean,
    name: String,
    nameError: String?,
    surname: String,
    surnameError: String?,
    email: String,
    dateOfBirth: String,
    dobError: String?,
    specialization: String,
    showError: Boolean,
    errorMessage: String,
    isFormValid: Boolean,
    onNameChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onSpecializationChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.edit_profile),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(id = R.string.name)) },
            isError = nameError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (nameError != null) {
            Text(nameError, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = surname,
            onValueChange = onSurnameChange,
            label = { Text(stringResource(id = R.string.surname)) },
            isError = surnameError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (surnameError != null) {
            Text(surnameError, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(id = R.string.email)) },
            isError = !Patterns.EMAIL_ADDRESS.matcher(email).matches(),
            modifier = Modifier.fillMaxWidth()
        )
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Text(stringResource(id = R.string.invalid_email), color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = onDateOfBirthChange,
            label = { Text(stringResource(id = R.string.birthdate)) },
            isError = dobError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (dobError != null) {
            Text(dobError, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if(isDoctor) {
            OutlinedTextField(
                value = specialization,
                onValueChange = onSpecializationChange,
                label = { Text(stringResource(id = R.string.specialization)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showError) {
            Text(errorMessage, color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onSaveClick,
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.save_profile))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangeUserContentPreview() {

    ChangeUserContent(
        isDoctor = true,
        name = "Gregory",
        nameError = null,
        surname = "House",
        surnameError = null,
        email = "greg.house@wp.pl",
        dateOfBirth = "1990-01-01",
        dobError = null,
        specialization = "pediatrist",
        showError = false,
        errorMessage = "",
        isFormValid = true,
        onNameChange = {},
        onSurnameChange = {},
        onEmailChange = {},
        onDateOfBirthChange = {},
        onSpecializationChange = {},
        onSaveClick = {}
    )
}