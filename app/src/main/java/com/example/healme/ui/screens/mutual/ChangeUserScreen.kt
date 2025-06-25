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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healme.R
import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreClass
import com.example.healme.viewmodel.AdminViewModel
import com.example.healme.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.collectAsState
import com.example.healme.data.models.user.Patient


/**
 * ChangeUserScreen is a Composable function that displays a screen for changing user information.
 *
 * @param navController The NavController used for navigation.
 * @param userId The ID of the user to be changed. Default is "null".
 * @param loginViewModel The LoginViewModel instance used for authentication-related operations.
 * @param adminViewModel The AdminViewModel instance used for admin-related operations.
 */
@Composable
fun ChangeUserScreen(navController: NavController,
                     userId: String = "null",
                     loginViewModel: LoginViewModel = viewModel(),
                     adminViewModel: AdminViewModel = viewModel()
){

    val fs = FirestoreClass()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

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

    val nameError = if (name.isNotEmpty()) loginViewModel.nameValidity(name) else null
    val surnameError = if (surname.isNotEmpty()) loginViewModel.surnameValidity(surname) else null
    val dobError = if (dateOfBirth.isNotEmpty()) loginViewModel.ageValidity(dateOfBirth) else null

    val isFormValid = name.isNotEmpty() && surname.isNotEmpty() &&
            email.isNotEmpty() && dateOfBirth.isNotEmpty() &&
            nameError == null && surnameError == null && dobError == null

    var initialName by remember { mutableStateOf("") }
    var initialSurname by remember { mutableStateOf("") }
    var initialEmail by remember { mutableStateOf("") }
    var initialDateOfBirth by remember { mutableStateOf("") }
    var initialIsDoctor by remember { mutableStateOf(false) }
    var initialSpecialization by remember { mutableStateOf("") }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showAddPatientDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    val allPatients by adminViewModel.allPatients.collectAsState()
    var showRemovePatientDialog by remember { mutableStateOf(false) }
    val doctorPatients by adminViewModel.doctorPatients.collectAsState()

    var dataLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(currentUserId) {
        if (!dataLoaded) {
            val userData = fs.loadUser(currentUserId ?: "")
            user = userData?.toMutableMap()

            val loadedIsDoctor = user?.get("specialization") != null
            isDoctor = user?.get("specialization") != null

            if(isDoctor) {
                val loadedSpecialization = if (loadedIsDoctor) user?.get("specialization") as String? ?: "" else ""
                specialization = loadedSpecialization
                initialSpecialization = loadedSpecialization
            }

            val loadedName = user?.get("name") as? String ?: ""
            name = loadedName
            initialName = loadedName

            val loadedSurname = user?.get("surname") as? String ?: ""
            surname = loadedSurname
            initialSurname = loadedSurname

            val loadedEmail = user?.get("email") as? String ?: ""
            email = loadedEmail
            initialEmail = loadedEmail

            val loadedDob = user?.get("dateOfBirth") as? String ?: ""
            dateOfBirth = loadedDob
            initialDateOfBirth = loadedDob

            dataLoaded = true
        }

        if (adminMode || isDoctor) {
            adminViewModel.loadAllPatients()
            currentUserId?.let { adminViewModel.loadPatientsForDoctor(it)}
        }
    }

    if (showConfirmDialog) {
        ConfirmChangesDialog(
            name = name, initialName = initialName,
            surname = surname, initialSurname = initialSurname,
            email = email, initialEmail = initialEmail,
            dateOfBirth = dateOfBirth, initialDateOfBirth = initialDateOfBirth,
            isDoctor = isDoctor, initialIsDoctor = initialIsDoctor,
            specialization = specialization, initialSpecialization = initialSpecialization,
            onConfirm = {
                showConfirmDialog = false
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
            onDismiss = { showConfirmDialog = false }
        )
    }

    if (showAddPatientDialog) {
        AddPatientDialog(
            patients = allPatients,
            onDismiss = { showAddPatientDialog = false },
            onConfirm = { patientId ->
                showAddPatientDialog = false
                coroutineScope.launch {
                    currentUserId?.let { doctorId ->
                        fs.addPatientToDoctor(doctorId, patientId) { success, message ->
                            resultMessage = if (success) {
                                context.getString(R.string.edit_profile_add_patient_success)
                            } else {
                                context.getString(R.string.edit_profile_add_patient_failure, message)
                            }
                            showResultDialog = true
                        }
                    }
                }
            }
        )
    }

    if (showRemovePatientDialog) {
        RemovePatientDialog(
            patients = doctorPatients,
            onDismiss = { showRemovePatientDialog = false },
            onConfirm = { patientId ->
                showRemovePatientDialog = false
                coroutineScope.launch {
                    currentUserId?.let { doctorId ->
                        fs.removePatientFromDoctor(doctorId, patientId) { success, message ->
                            resultMessage = if (success) {
                                context.getString(R.string.edit_profile_remove_patient_success)
                            } else {
                                context.getString(R.string.edit_profile_remove_patient_failure, message)
                            }
                            showResultDialog = true
                            if (success) {
                                adminViewModel.loadPatientsForDoctor(doctorId)
                            }
                        }
                    }
                }
            }
        )
    }

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text(stringResource(R.string.edit_profile_operation_result_title)) },
            text = { Text(resultMessage) },
            confirmButton = {
                Button(onClick = { showResultDialog = false }) {
                    Text(stringResource(R.string.confirm_button))
                }
            }
        )
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
                showConfirmDialog = true
            }
        },
        onToggleDoctorStatus = {
            isDoctor = !isDoctor
            changesMade = true
        },
        onCancelClick = {
            navController.popBackStack()
        },
        onNavigateBack = {
            navController.popBackStack()
        },
        onAddPatientClick = {
            showAddPatientDialog = true
        },
        onRemovePatientClick = {
            showRemovePatientDialog = true
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
    onCancelClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onAddPatientClick: () -> Unit = {},
    onRemovePatientClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (isDoctor) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, top = 0.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (adminMode || isDoctor) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = onAddPatientClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.edit_profile_add_patient))
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onRemovePatientClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.edit_profile_remove_patient))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(errorMessage, color = Color.Red, fontSize = 14.sp)
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
 * ConfirmChangesDialog is a Composable function that displays a dialog to confirm changes made by the user.
 *
 * @param name The new name of the user.
 * @param initialName The initial name of the user before changes.
 * @param surname The new surname of the user.
 * @param initialSurname The initial surname of the user before changes.
 * @param email The new email of the user.
 * @param initialEmail The initial email of the user before changes.
 * @param dateOfBirth The new date of birth of the user.
 * @param initialDateOfBirth The initial date of birth of the user before changes.
 * @param isDoctor Indicates if the user is a doctor.
 * @param initialIsDoctor Indicates if the user was a doctor before changes.
 * @param specialization The new specialization of the doctor (if applicable).
 * @param initialSpecialization The initial specialization of the doctor before changes (if applicable).
 * @param onConfirm Callback for confirming changes.
 * @param onDismiss Callback for dismissing the dialog without confirming changes.
 */
@Composable
fun ConfirmChangesDialog(
    name: String,
    initialName: String,
    surname: String,
    initialSurname: String,
    email: String,
    initialEmail: String,
    dateOfBirth: String,
    initialDateOfBirth: String,
    isDoctor: Boolean,
    initialIsDoctor: Boolean,
    specialization: String,
    initialSpecialization: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile_confirm_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.edit_profile_confirm_question))
                Spacer(Modifier.height(8.dp))

                val changesText = mutableListOf<String>()
                if (name != initialName) {
                    changesText.add(stringResource(R.string.edit_profile_confirm_change_from_to, stringResource(R.string.register_name), initialName, name))
                }
                if (surname != initialSurname) {
                    changesText.add(stringResource(R.string.edit_profile_confirm_change_from_to, stringResource(R.string.register_surname), initialSurname, surname))
                }
                if (email != initialEmail) {
                    changesText.add(stringResource(R.string.edit_profile_confirm_change_from_to, stringResource(R.string.register_email), initialEmail, email))
                }
                if (dateOfBirth != initialDateOfBirth) {
                    changesText.add(stringResource(R.string.edit_profile_confirm_change_from_to, stringResource(R.string.register_birthdate_long), initialDateOfBirth, dateOfBirth))
                }
                if (isDoctor != initialIsDoctor) {
                    val from = if (initialIsDoctor) stringResource(R.string.edit_profile_confirm_role_doctor) else stringResource(R.string.edit_profile_confirm_role_patient)
                    val to = if (isDoctor) stringResource(R.string.edit_profile_confirm_role_doctor) else stringResource(R.string.edit_profile_confirm_role_patient)
                    changesText.add(stringResource(R.string.edit_profile_confirm_change_from_to, stringResource(R.string.edit_profile_confirm_role), from, to))
                }
                if (isDoctor && specialization != initialSpecialization) {
                    changesText.add(stringResource(R.string.edit_profile_confirm_change_from_to, stringResource(R.string.edit_profile_specialization), initialSpecialization, specialization))
                }

                if (changesText.isNotEmpty()) {
                    changesText.forEach { change ->
                        Text(text = change, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Text(stringResource(R.string.edit_profile_confirm_no_changes))
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.confirm_button))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

/**
 * AddPatientDialog is a Composable function that displays a dialog to add a patient to a doctor's list.
 *
 * @param patients The list of all patients available for selection.
 * @param onDismiss Callback for dismissing the dialog.
 * @param onConfirm Callback for confirming the addition of a patient.
 */
@Composable
fun AddPatientDialog(
    patients: List<Patient>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedPatientId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile_select_patient_title)) },
        text = {
            if (patients.isEmpty()) {
                Text("No patients available to add.")
            } else {
                LazyColumn {
                    items(patients) { patient ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { selectedPatientId = patient.id }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedPatientId == patient.id),
                                onClick = { selectedPatientId = patient.id }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("${patient.name} ${patient.surname}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedPatientId?.let { onConfirm(it) }
                },
                enabled = selectedPatientId != null
            ) {
                Text(stringResource(R.string.confirm_button))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

/**
 * RemovePatientDialog is a Composable function that displays a dialog to remove a patient from a doctor's list.
 *
 * @param patients The list of patients associated with the doctor.
 * @param onDismiss Callback for dismissing the dialog.
 * @param onConfirm Callback for confirming the removal of a patient.
 */
@Composable
fun RemovePatientDialog(
    patients: List<Patient>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedPatientId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile_select_patient_to_remove_title)) },
        text = {
            if (patients.isEmpty()) {
                Text(stringResource(R.string.edit_profile_no_patients_to_remove))
            } else {
                LazyColumn {
                    items(patients) { patient ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { selectedPatientId = patient.id }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedPatientId == patient.id),
                                onClick = { selectedPatientId = patient.id }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("${patient.name} ${patient.surname}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedPatientId?.let { onConfirm(it) }
                },
                enabled = selectedPatientId != null && patients.isNotEmpty()
            ) {
                Text(stringResource(R.string.confirm_button))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}