package com.example.healme.ui.screens.mutual

import android.util.Patterns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreClass
import com.example.healme.viewmodel.ChangeUserViewModel
import com.example.healme.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChangeUserScreen(navController: NavController,
                     authViewModel: AuthViewModel,
                     changeUserViewModel: ChangeUserViewModel
){
    val fs = FirestoreClass()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var chosenUser by remember { mutableStateOf(User) }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf("") }
    // Doctor specific
    var specialization by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(" ") }
    var errorMessage by remember { mutableStateOf("") }

    val nameError = if (name.isNotEmpty()) authViewModel.nameValidity(name) else null
    val surnameError = if (surname.isNotEmpty()) authViewModel.surnameValidity(surname) else null
    val dobError = if (dateOfBirth.isNotEmpty()) authViewModel.ageValidity(dateOfBirth) else null
    val emailError = !Patterns.EMAIL_ADDRESS.matcher(email.toString().trim { it <= ' ' }).matches()

    val isFormValid = name.isNotEmpty() && surname.isNotEmpty() &&
            email.isNotEmpty() && dateOfBirth.isNotEmpty() &&
            nameError == null && surnameError == null && dobError == null

    LaunchedEffect(currentUser?.uid) {


    }
}

@Composable
fun ChangeUserContent(
    chosenUser: User,
    name: String,
    surname: String,
    email: String,
    dateOfBirth: String,
    contacts: String,
    specialization: String,
    showError: Boolean,
    errorMessage: String,
    isFormValid: Boolean,
    onNameChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onContactsChange: (String) -> Unit,
    onSpecializationChange: (String) -> Unit
) {


}