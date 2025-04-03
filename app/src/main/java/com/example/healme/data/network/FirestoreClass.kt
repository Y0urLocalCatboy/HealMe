package com.example.healme.data.network

import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreInterface
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirestoreClass: FirestoreInterface {

    private val db = Firebase.firestore
    private val fs = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

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

    override suspend fun loadUser(id: String): Map<String, Any?>? {
        try {
            val documentSnapshot = fs.collection("patients")
                .document(id)
                .get()
                .await()
            if (documentSnapshot.exists())
                return documentSnapshot.data
            else return fs.collection("doctors")
                .document(id)
                .get()
                .await().data
        } catch (e: Exception) {
            throw Exception("loadUser: ${e.message}")
        }
    }

    override suspend fun updateUser(user: User, data: Map<String, Any?>) {
        try {
            val filtered = data.filterValues { value ->
                value != null && !(value is String && value.isBlank())
            }
            if (filtered.isEmpty()) return

            fs.collection(user.getCollectionName())
                .document(user.id)
                .update(filtered)
                .await()
        } catch (e: Exception) {
            throw Exception("updateUser: ${e.message}")
        }
    }

    override fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Login successful!")
                } else {
                    onResult(false, task.exception?.message ?: "Unknown error loginUser")
                }
            }
    }

    override suspend fun patientToDoctor(patientId: String) {
        try {
            val userData = loadUser(auth.uid ?: "") ?: throw IllegalStateException("User data not found")
            val filtered = userData.filterValues { value ->
                value != null && !(value is String && value.isBlank())
            } as Map<String, Any>

            val user = User.fromMap(filtered)
            val doctorData = hashMapOf(
                "id" to user.id,
                "email" to user.email,
                "name" to user.name,
                "surname" to user.surname,
                "dateOfBirth" to user.dateOfBirth,
                "speciality" to "placeholder"    //ZAPAMIETAC ZE TO BAZOWO PLACEHOLDER
            )

            db.collection("doctors").document(user.id).set(doctorData).await()
            db.collection("patients").document(user.id).delete().await()
        } catch (e: Exception) {
            throw Exception("patientToDoctor: ${e.message}")
        }
    }
}
