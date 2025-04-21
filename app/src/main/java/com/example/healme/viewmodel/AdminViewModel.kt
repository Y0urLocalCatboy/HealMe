package com.example.healme.viewmodel

import com.google.firebase.firestore.ListenerRegistration
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
import kotlin.collections.remove

class AdminViewModel : ViewModel() {

    private val firestoreClass = FirestoreClass()

    private val users_flow = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = users_flow

    private val allUsers = mutableListOf<User>()

    init {
        startListeningForUsers()
    }
    private var patientListener: ListenerRegistration? = null
    private var doctorListener: ListenerRegistration? = null

    fun startListeningForUsers() {
        patientListener?.remove()
        doctorListener?.remove()

        patientListener = firestoreClass.listenForPatients { patients ->
            updateUsersList(patients, isDoctors = false)
        }

        doctorListener = firestoreClass.listenForDoctors { doctors ->
            updateUsersList(doctors, isDoctors = true)
        }
    }
    private fun updateUsersList(users: List<User>, isDoctors: Boolean) {
        synchronized(allUsers) {
            // Usuwamy starych użytkowników tego typu
            allUsers.removeAll {
                if (isDoctors) it is Doctor else it is Patient
            }
            // Dodajemy nowych
            allUsers.addAll(users)
            // Emitujemy nową wartość
            users_flow.value = allUsers.toList()
        }
    }

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

    fun changeUserType(user: User) {
        viewModelScope.launch {
            try {
                when (user) {
                    is Doctor -> FirestoreClass().doctorToPatient(user.id)
                    is Patient -> FirestoreClass().patientToDoctor(user.id)
                    else -> null
                }
                loadAllUsers()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        patientListener?.remove()
        doctorListener?.remove()
        super.onCleared()
    }
}