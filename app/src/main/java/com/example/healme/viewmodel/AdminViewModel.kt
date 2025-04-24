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
/**
 * ViewModel class for managing admin-related data and operations.
 *
 * @property firestoreClass Instance of FirestoreClass for database operations.
 * @property users_flow MutableStateFlow containing a list of User objects.
 * @property users StateFlow exposing the list of User objects.
 * @property allUsers List to store all users fetched from Firestore.
 *
 * @function startListeningForUsers Starts listening for changes in the patients and doctors collections in Firestore.
 * @function updateUsersList Updates the users list and emits the new list to the flow.
 * @function loadAllUsers Loads all users from Firestore and updates the users flow.
 * @function changeUserType Changes the user type from Doctor to Patient or vice versa.
 * @function onCleared Clears the listeners when the ViewModel is cleared.
 */
class AdminViewModel : ViewModel() {

    private val firestoreClass = FirestoreClass()

    private val users_flow = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = users_flow

    private val allUsers = mutableListOf<User>()

    /**
     * Initializes the ViewModel and starts listening for users.
     */
    init {
        startListeningForUsers()
    }

    private var patientListener: ListenerRegistration? = null
    private var doctorListener: ListenerRegistration? = null

    /**
     * Starts listening for changes in the patients and doctors collections in Firestore.
     * Updates the users flow with the latest data.
     */
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

    /**
     * Updates the users list and emits the new list to the flow.
     *
     * @param users The list of users to be added.
     * @param isDoctors Boolean indicating if the users are doctors or patients.
     */
    private fun updateUsersList(users: List<User>, isDoctors: Boolean) {
        synchronized(allUsers) {
            allUsers.removeAll {
                if (isDoctors) it is Doctor else it is Patient
            }
            allUsers.addAll(users)
            users_flow.value = allUsers.toList()
        }
    }

    /**
     * Loads all users from Firestore and updates the users flow.
     */
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

    /**
     * Changes the user type from Doctor to Patient or vice versa.
     *
     * @param user The User object to be changed.
     */
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

    /**
     * Clears the listeners when the ViewModel is cleared.
     */
    override fun onCleared() {
        patientListener?.remove()
        doctorListener?.remove()
        super.onCleared()
    }
}