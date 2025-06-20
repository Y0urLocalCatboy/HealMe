package com.example.healme.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.healme.data.network.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for handling authentication and user role retrieval.
 * This ViewModel interacts with Firebase Authentication and Firestore to determine the start destination
 * based on the user's role.
 */
class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val fs = FirestoreClass()

    /**
     * Retrieves the start destination based on the current user's role.
     * If the user is not authenticated, returns "login".
     * If the user is authenticated, checks their role in Firestore and returns the corresponding destination.
     *
     * @return The start destination as a String.
     */
    suspend fun getStartDestination(): String {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return "login"
        }

        return try {
            val adminDoc = db.collection("admins").document(currentUser.uid).get().await()
            if (adminDoc.exists()) return "admin"

            val doctorDoc = db.collection("doctors").document(currentUser.uid).get().await()
            if (doctorDoc.exists()) return "doctor"

            val patientDoc = db.collection("patients").document(currentUser.uid).get().await()
            if (patientDoc.exists()) return "patient"

            "login"
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error fetching user role", e)
            "login"
        }
    }
}