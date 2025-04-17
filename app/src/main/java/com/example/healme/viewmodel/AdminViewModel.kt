package com.example.healme.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healme.R
import com.example.healme.data.models.user.Doctor
import com.example.healme.data.models.user.Patient
import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val firestoreClass = FirestoreClass()

    private val users_flow = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = users_flow

    fun loadAllUsers() {
        viewModelScope.launch {
            val allUsers = mutableListOf<User>()

            val patientDocs = firestoreClass.getAllPatients()
            if (patientDocs != null) {
                allUsers.addAll(patientDocs)
            }

            val doctorDocs = firestoreClass.getAllDoctors()
            if (doctorDocs != null) {
                allUsers.addAll(doctorDocs)
            }

            users_flow.value = allUsers
        }
    }
}