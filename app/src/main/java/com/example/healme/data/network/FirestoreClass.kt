package com.example.healme.data.network

import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreInterface
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class FirestoreClass: FirestoreInterface {

    private val db = Firebase.firestore
    private val fs = FirebaseFirestore.getInstance()

    override suspend fun registerUser(user: User) {
        try {
            val userMap = hashMapOf(
                "id" to user.id,
                "email" to user.email,
                "name" to user.name,
                "surname" to user.surname,
                "dateOfBirth" to user.dateOfBirth
            )
            db.collection(user.getCollectionName()).document(user.id).set(userMap)
        } catch (e: Exception) {
            throw e
        }
    }

    override fun loadUser(id: String) {
        TODO("Not yet implemented")
    }

    override fun updateUser(user: User, data: Map<String, Any?>) {
        TODO("Not yet implemented")
    }

    override fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Login successful!")
                } else {
                    onResult(false, task.exception?.message ?: "Unknown error occurred")
                }
            }
    }
}
