package com.example.healme.firebase

import com.example.healme.firebase.user.User
import com.google.firebase.Firebase
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
}