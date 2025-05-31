package com.example.healme.data.network

import com.example.healme.data.models.MedicalHistory
import com.example.healme.data.models.Message
import com.example.healme.data.models.Prescription
import com.example.healme.data.models.Visit
import com.example.healme.data.models.user.*
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

interface FirestoreInterface {

    /**
     * Registers a new user in Firestore.
     *
     * @param user The user object containing registration information.
     */
    suspend fun registerUser(user: User)

    /**
     * Loads user data from Firestore.
     *
     * @param id The user ID to retrieve.
     * @return Map containing user data if found, null otherwise.
     */
    suspend fun loadUser(id: String): Map<String, Any?>?

    /**
     * Updates existing user data in Firestore.
     *
     * @param user The user object to update.
     * @param data Map of fields to update.
     */
    suspend fun updateUser(user: User,
                           data: Map<String, Any?>)

    /**
     * Authenticates a user with Firebase Authentication.
     *
     * @param email User's email address.
     * @param password User's password.
     * @param onResult Callback function with result (success, message).
     */
    fun loginUser(email: String,
                  password: String,
                  onResult: (Boolean, String) -> Unit)

    /**
     * Converts a patient account to a doctor account.
     *
     * @param patientId ID of the patient to convert.
     */
    suspend fun patientToDoctor(patientId: String)

    suspend fun doctorToPatient(doctorId: String)

    /**
     * Saves a message to Firestore.
     *
     * @param message The message content.
     * @param senderId The ID of the sender.
     * @param receiverId The ID of the receiver.
     * @param onResult Callback function with result (success, message).
     */
    fun saveMessage(
        message: Message,
        onResult: (Boolean, String) -> Unit
    )

    /**
     * Retrieves ALL messages between two users.
     * Might be incredibly time consuming if there are a lot of messages.
     *
     * @param senderId The ID of the sender.
     * @param receiverId The ID of the receiver.
     * @param onResult Callback function with result (success, list of messages).
     */
    fun getAllMessages(
        senderId: String,
        receiverId: String,
        onResult: (Boolean, List<Message>) -> Unit
    )

    /**
     * Gets all the doctors associated with a patient.
     *
     * @param id The id of the patient.
     * @return The doctors associated with the patient, or null if no doctors are found.
     */
    suspend fun doctorsFromPatient(id: String): MutableList<Doctor>?

    /**
     * Gets all the patients associated with a doctor.
     *
     * @param id The id of the doctor.
     * @return The patients associated with the doctor, or null if no patients are found.
     */
    suspend fun patientsFromDoctor(id: String): MutableList<Patient>?

    /**
     * Listens for messages between two users.
     *
     * @param senderId The ID of the sender.
     * @param receiverId The ID of the receiver.
     * @param onUpdate Callback function with updated list of messages.
     * @return ListenerRegistration object to manage the listener.
     */
    fun listenForMessages(
        senderId: String,
        receiverId: String,
        onUpdate: (List<Message>) -> Unit
    ): ListenerRegistration

    /**
     * Checks if a user is an admin.
     *
     * @param id The ID of the user to check.
     */
    suspend fun isAdmin(id: String): Boolean

    /**
     * Changes a user's role to admin.
     *
     * @param id The ID of the user to convert.
     * @param onResult Callback function with result (success, message).
     */
    suspend fun isDoctor(id: String): Boolean

    /**
     * Updates availability data for a doctor.
     * @param doctorId ID of the doctor.
     * @param availabilityMap Availability data to update.
     */
    suspend fun updateDoctorAvailability(doctorId: String, availabilityMap: Map<String, Any?>)

    /**
     * Retrieves availability data for a doctor.
     * @param doctorId ID of the doctor.
     * @return Map of timestamps to availability status ("available"/"unavailable").
     */
    suspend fun getDoctorAvailability(doctorId: String): Map<Long, String>

    suspend fun changeToAdmin(id: String, onResult: (Boolean, String) -> Unit)

    /**
     * Retrieves all doctors from the database.
     *
     * @return List of all doctors or null if an error occurs.
     */
    suspend fun getAllDoctors(): List<Doctor>?

    /**
     * Retrieves all patients from the database.
     *
     * @return List of all patients or null if an error occurs.
     */
    suspend fun getAllPatients(): List<Patient>?

    /**
     * Sets up a real-time listener for changes to patient data.
     *
     * @param onUpdate Callback function invoked when patient data changes.
     * @return ListenerRegistration object to manage the listener.
     */
    fun listenForPatients(onUpdate: (List<Patient>) -> Unit): ListenerRegistration

    /**
     * Sets up a real-time listener for changes to doctor data.
     *
     * @param onUpdate Callback function invoked when doctor data changes.
     * @return ListenerRegistration object to manage the listener.
     */
    fun listenForDoctors(onUpdate: (List<Doctor>) -> Unit): ListenerRegistration

    /**
     * Saves a prescription to the database.
     *
     * @param prescription The prescription object to save.
     * @param onResult Callback function with result (success, message).
     */
    fun savePrescription(
        prescription: Prescription,
        onResult: (Boolean, String) -> Unit
    )

    /**
     * Retrieves all prescriptions for a specific patient.
     *
     * @param patientId The ID of the patient.
     * @return List of prescriptions for the specified patient.
     */
    suspend fun getPrescriptionsForPatient(patientId: String): List<Prescription>

    suspend fun getPatientVisits(patientId: String): List<Pair<Long, String>>

    suspend fun bookVisit(doctorId: String, patientId: String, timestamp: Long)

    suspend fun getBookedTimestampsForDoctor(doctorId: String): List<Long>

    suspend fun addMedicalRecord(patientId: String, doctorId: String, timestamp: Long)

    suspend fun getPatientMedicalHistory(patientId: String): List<MedicalHistory>

    suspend fun getUpcomingVisitForPatient(patientId: String): Pair<Visit, Doctor>?

    suspend fun cleanUpPastVisits(patientId: String)

    /**
     * Updates the FCM token for a given user.
     *
     * @param userId The ID of the user.
     * @param userType The type of the user ("patient", "doctor", or "admin").
     * @param token The new FCM token.
     */
    suspend fun updateUserFcmToken(userId: String, userType: String, token: String)

    /**
     * Retrieves the FCM token for a given user.
     *
     * @param userId The ID of the user.
     * @param userType The type of the user ("patient", "doctor", or "admin").
     * @return The FCM token, or null if not found.
     */
    suspend fun getUserFcmToken(userId: String, userType: String): String?



}