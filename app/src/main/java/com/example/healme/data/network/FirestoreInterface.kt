package com.example.healme.data.network

import com.example.healme.data.models.Message
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
}