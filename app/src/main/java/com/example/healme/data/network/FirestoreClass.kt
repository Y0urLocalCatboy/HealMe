package com.example.healme.data.network

import com.example.healme.data.models.Message
import com.example.healme.data.models.user.Doctor
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
            val userData =
                loadUser(auth.uid ?: "") ?: throw IllegalStateException("User data not found")
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
                "speciality" to "placeholder",    //ZAPAMIETAC ZE TO BAZOWO PLACEHOLDER
                "patients" to mutableListOf<String?>()
            )
            db.runTransaction { transaction ->
                val patientDocRef = db.collection("patient").document(user.id)
                transaction.delete(patientDocRef)
                val doctorDocRef = db.collection("doctor").document(user.id)
                transaction.set(doctorDocRef, doctorData)
            }.await()
        } catch (e: Exception) {
            throw Exception("patientToDoctor: ${e.message}")
        }
    }

    override fun saveMessage(
        message: Message,
        startDate: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val messageData = hashMapOf(
            "content" to message.content,
            "timestamp" to message.timestamp,
        )

        db.collection("messages")
            .whereEqualTo("senderId", message.senderId)
            .whereEqualTo("receiverId", message.receiverId)
            .whereEqualTo("startDate", startDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    val newMessageData = hashMapOf(
                        "senderId" to message.senderId,
                        "receiverId" to message.receiverId,
                        "startDate" to startDate,
                        "weeklymessages" to arrayListOf(messageData)
                    )
                    db.collection("messages").add(newMessageData)
                        .addOnSuccessListener {
                            onResult(true, "Message sent successfully!")
                        }
                        .addOnFailureListener { e ->
                            onResult(false, e.message ?: "Unknown error sending message")
                        }
                } else {
                    val docRef = querySnapshot.documents[0].reference
                    docRef.update(
                        "weeklymessages",
                        com.google.firebase.firestore.FieldValue.arrayUnion(messageData)
                    )
                        .addOnSuccessListener {
                        }
                }
            }
    }

    override fun getAllMessages(
        senderId: String,
        receiverId: String,
        onResult: (Boolean, List<Message>) -> Unit
    ) {
        db.collection("messages")
            .whereEqualTo("senderId", senderId)
            .whereEqualTo("receiverId", receiverId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    onResult(false, emptyList())
                } else {
                    val messages = mutableListOf<Message>()
                    for (document in querySnapshot.documents) {
                        for (messageData in document.get("weeklymessages") as List<Map<String, Any>>) {
                            val message = Message(
                                content = messageData["content"] as String,
                                timestamp = messageData["timestamp"] as String,
                                senderId = senderId,
                                receiverId = receiverId
                            )
                            messages.add(message)
                        }
                    }
                    onResult(true, messages)
                }
            }
            .addOnFailureListener {
                onResult(false, emptyList())
            }
    }

    override suspend fun doctorsFromPatient(id: String): MutableList<Doctor>? {
        return try {
            val snapshot = db.collection("doctors").get().await()
            snapshot.documents.mapNotNull { it.toObject(Doctor::class.java) }
                .filter { it.patients.contains(id) }
                .toMutableList()
        } catch (e: Exception) {
            null
        }
    }
}
