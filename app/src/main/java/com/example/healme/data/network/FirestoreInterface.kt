package com.example.healme.data.network

import com.example.healme.data.models.user.User

interface FirestoreInterface {

    /**
     * Registers a new user in Firestore.
     * @param user The user object containing registration information.
     */
    suspend fun registerUser(user: User)

    /**
     * Loads user data from Firestore.
     * @param id The user ID to retrieve.
     * @return Map containing user data if found, null otherwise.
     */
    suspend fun loadUser(id: String): Map<String, Any?>?

    /**
     * Updates existing user data in Firestore.
     * @param user The user object to update.
     * @param data Map of fields to update.
     */
    suspend fun updateUser(user: User, data: Map<String, Any?>)

    /**
     * Authenticates a user with Firebase Authentication.
     * @param email User's email address.
     * @param password User's password.
     * @param onResult Callback function with result (success, message).
     */
    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit)

    /**
     * Converts a patient account to a doctor account.
     * @param patientId ID of the patient to convert.
     */
    suspend fun patientToDoctor(patientId: String)
}