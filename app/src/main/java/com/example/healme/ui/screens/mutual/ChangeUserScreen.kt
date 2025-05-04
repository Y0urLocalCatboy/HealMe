package com.example.healme.ui.screens.mutual

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.example.healme.viewmodel.AdminViewModel
import com.example.healme.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * ChangeUserScreen is a Composable function that displays a screen for changing user information.
 *
 * @param navController The NavController used for navigation.
 * @param userId The ID of the user to be changed. Default is "null".
 * @param authViewModel The AuthViewModel instance used for authentication-related operations.
 * @param adminViewModel The AdminViewModel instance used for admin-related operations.
 */
@Composable
fun ChangeUserScreen(navController: NavController,
                     userId: String = "null",
                     authViewModel: AuthViewModel = viewModel(),
                     adminViewModel: AdminViewModel = viewModel()
){
    val fs = FirestoreClass()
    val auth = FirebaseAuth.getInstance()

    val currentUserId = if(userId != "null") userId else auth.currentUser?.uid
    val coroutineScope = rememberCoroutineScope()
    val adminMode = userId != "null"

    var user by remember { mutableStateOf<MutableMap<String, Any?>?>(null) }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var isDoctor by remember { mutableStateOf(false) }
    var changesMade by remember { mutableStateOf(false) }


    var errorMessage by remember { mutableStateOf("") }

    var specialization by remember { mutableStateOf("") }

    val nameError = if (name.isNotEmpty()) authViewModel.nameValidity(name) else null
    val surnameError = if (surname.isNotEmpty()) authViewModel.surnameValidity(surname) else null
    val dobError = if (dateOfBirth.isNotEmpty()) authViewModel.ageValidity(dateOfBirth) else null

    val isFormValid = name.isNotEmpty() && surname.isNotEmpty() &&
            email.isNotEmpty() && dateOfBirth.isNotEmpty() &&
            nameError == null && surnameError == null && dobError == null

    var dataLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(currentUserId) {
        if (!dataLoaded) {
            val userData = fs.loadUser(currentUserId ?: "")
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
        adminMode = adminMode,
        isDoctor = isDoctor,
        name = name,
        nameError = nameError,
        surname = surname,
        surnameError = surnameError,
        email = email,
        dateOfBirth = dateOfBirth,
        dobError = dobError,
        specialization = specialization,
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
                } else if (changesMade) {
                    updateData.remove("specialization")
                }

                try {
                    coroutineScope.launch {
                        try {
                            fs.updateUser(User.fromMap(user as Map<String, Any>), updateData)
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "it shouldn't happen (onSaveClick changeuserScreen)"
                        }
                        if (changesMade) {
                            adminViewModel.changeUserType(User.fromMap(user as Map<String, Any>))
                            changesMade = false
                        }
                    }
                    navController.popBackStack()
                } catch (e: Exception) {
                    errorMessage = e.message ?: "it shouldn't happen (DITTO changeuserScreen)"
                }
            }
        },
        onToggleDoctorStatus = {
            isDoctor = !isDoctor
            changesMade = true
        },
        onCancelClick = {
            navController.popBackStack()
        }
    )
}

/**
 * ChangeUserContent is a Composable function that displays the content of the ChangeUserScreen.
 *
 * @param adminMode Indicates if the screen is in admin mode.
 * @param isDoctor Indicates if the user is a doctor.
 * @param name The name of the user.
 * @param nameError The error message for the name field.
 * @param surname The surname of the user.
 * @param surnameError The error message for the surname field.
 * @param email The email of the user.
 * @param dateOfBirth The date of birth of the user.
 * @param dobError The error message for the date of birth field.
 * @param specialization The specialization of the user (if applicable - if isDoctor == true).
 * @param errorMessage The error message to be displayed.
 * @param isFormValid Indicates if the form is valid.
 * @param onNameChange Callback for name change.
 * @param onSurnameChange Callback for surname change.
 * @param onEmailChange Callback for email change.
 * @param onDateOfBirthChange Callback for date of birth change.
 * @param onSpecializationChange Callback for specialization change.
 * @param onSaveClick Callback for save button click.
 * @param onToggleDoctorStatus Callback for toggling doctor status.
 * @param onCancelClick Callback for cancel button click.
 */
@Composable
fun ChangeUserContent(
    adminMode: Boolean = false,
    isDoctor: Boolean,
    name: String,
    nameError: String?,
    surname: String,
    surnameError: String?,
    email: String,
    dateOfBirth: String,
    dobError: String?,
    specialization: String,
    errorMessage: String,
    isFormValid: Boolean,
    onNameChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onSpecializationChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onToggleDoctorStatus: () -> Unit = {},
    onCancelClick: () -> Unit = {}
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (adminMode) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onToggleDoctorStatus,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        if (isDoctor)
                            stringResource(R.string.edit_profile_doctor_to_patient)
                        else
                            stringResource(R.string.edit_profile_patient_to_doctor)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.edit_profile_title),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

        } else {
            Text(
                text = stringResource(R.string.edit_profile_title),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(id = R.string.register_name)) },
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
            label = { Text(stringResource(id = R.string.register_surname)) },
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
            label = { Text(stringResource(id = R.string.register_email)) },
            isError = !Patterns.EMAIL_ADDRESS.matcher(email).matches(),
            modifier = Modifier.fillMaxWidth()
        )
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Text(
                stringResource(id = R.string.register_invalid_email),
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = onDateOfBirthChange,
            label = { Text(stringResource(id = R.string.register_birthdate_long)) },
            isError = dobError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (dobError != null) {
            Text(dobError, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isDoctor) {
            OutlinedTextField(
                value = specialization,
                onValueChange = onSpecializationChange,
                label = { Text(stringResource(id = R.string.edit_profile_specialization)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(errorMessage, color = Color.Red, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = onSaveClick,
            enabled = isFormValid && Patterns.EMAIL_ADDRESS.matcher(email).matches(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.edit_profile_save_changes))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCancelClick ,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(id = R.string.edit_profile_cancel)
            )
        }
    }
}

/**
 * Preview for ChangeUserContent.
 */
@Preview(showBackground = true)
@Composable
fun ChangeUserContentPreview() {
    ChangeUserContent(
        adminMode = true,
        isDoctor = true,
        name = "Gregory",
        nameError = null,
        surname = "House",
        surnameError = null,
        email = "greg.house@wp.pl",
        dateOfBirth = "1990-01-01",
        dobError = null,
        specialization = "pediatrist",
        errorMessage = "",
        isFormValid = true,
        onNameChange = {},
        onSurnameChange = {},
        onEmailChange = {},
        onDateOfBirthChange = {},
        onSpecializationChange = {},
        onSaveClick = {},
        onToggleDoctorStatus = {}
    )
}