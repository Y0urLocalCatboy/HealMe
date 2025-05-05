package com.example.healme.data.network

import androidx.compose.ui.res.stringResource
import com.example.healme.data.models.Message
import com.example.healme.data.models.user.Doctor
import com.example.healme.data.models.user.Patient
import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreInterface
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import kotlin.text.get

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
                loadUser(patientId) ?: throw IllegalStateException("User data not found")
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
                "specialization" to "placeholder",    //ZAPAMIETAC ZE TO BAZOWO PLACEHOLDER
                "patients" to mutableListOf<String?>()
            )
            db.runTransaction { transaction ->
                val patientDocRef = db.collection("patients").document(user.id)
                transaction.delete(patientDocRef)
                val doctorDocRef = db.collection("doctors").document(user.id)
                transaction.set(doctorDocRef, doctorData)
            }.await()
        } catch (e: Exception) {
            throw Exception("patientToDoctor: ${e.message}")
        }
    }

    override suspend fun doctorToPatient(doctorId: String) {
        try {
            val userData =
                loadUser(doctorId) ?: throw IllegalStateException("User data not found")
            val filtered = userData.filterValues { value ->
                value != null && !(value is String && value.isBlank())
            } as Map<String, Any>

            val user = User.fromMap(filtered)
            val patientData = hashMapOf(
                "id" to user.id,
                "email" to user.email,
                "name" to user.name,
                "surname" to user.surname,
                "dateOfBirth" to user.dateOfBirth
            )
            db.runTransaction { transaction ->
                val doctorDocRef = db.collection("doctors").document(user.id)
                transaction.delete(doctorDocRef)
                val patientDocRef = db.collection("patients").document(user.id)
                transaction.set(patientDocRef, patientData)
            }.await()
        } catch (e: Exception) {
            throw Exception("doctorToPatient: ${e.message}")
        }
    }

    override fun saveMessage(
        message: Message,
        onResult: (Boolean, String) -> Unit
    ) {
        val messageData = hashMapOf(
            "content" to message.content,
            "timestamp" to message.timestamp,
        )

        val messageDate = Date(message.timestamp.toLong())
        val calendar = Calendar.getInstance().apply {
            time = messageDate
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startDate = calendar.time.time.toString()

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
                            onResult(true, "Message sent successfully!")
                        }
                        .addOnFailureListener { e ->
                            onResult(false, e.message ?: "Unknown error updating message")
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
            .whereIn("senderId", listOf(senderId, receiverId))
            .whereIn("receiverId", listOf(senderId, receiverId))
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
                                senderId = document.get("senderId") as String,
                                receiverId = document.get("receiverId") as String
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

    override suspend fun patientsFromDoctor(id: String): MutableList<Patient>? {
        return try {
            val doctorSnapshot = db.collection("doctors").document(id).get().await()
            val patientIds = doctorSnapshot.get("patients") as? List<String> ?: return null
            val patients = mutableListOf<Patient>()
            for (patientId in patientIds) {
                val patientSnapshot = db.collection("patients").document(patientId).get().await()
                patientSnapshot.toObject(Patient::class.java)?.let { patients.add(it) }
            }
            patients
        } catch (e: Exception) {
            null
        }
    }

    override fun listenForMessages(
        senderId: String,
        receiverId: String,
        onUpdate: (List<Message>) -> Unit
    ): ListenerRegistration {
        return db.collection("messages")
            .whereIn("senderId", listOf(senderId, receiverId))
            .whereIn("receiverId", listOf(senderId, receiverId))
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = mutableListOf<Message>()
                    for (document in snapshot.documents) {
                        for (messageData in document.get("weeklymessages") as? List<Map<String, Any>>
                            ?: emptyList()) {
                            val message = Message(
                                content = messageData["content"] as String,
                                timestamp = messageData["timestamp"] as String,
                                senderId = document.get("senderId") as String,
                                receiverId = document.get("receiverId") as String
                            )
                            messages.add(message)
                        }
                    }
                    onUpdate(messages)
                } else {
                    onUpdate(emptyList())
                }
            }
    }


    override suspend fun isAdmin(email: String): Boolean {
        return try {
            val snapshot = db.collection("admins").get().await()
            snapshot.documents.any { it.getString("email") == email }
        } catch (e: Exception) {
            false
        }
    }
    override suspend fun isDoctor(email: String): Boolean {
        return try {
            val snapshot = db.collection("doctors").get().await()
            snapshot.documents.any { it.getString("email") == email }
        } catch (e: Exception) {
            false
        }
    }
    override suspend fun changeToAdmin(id: String, onResult: (Boolean, String) -> Unit) {
        try {

            val userData = loadUser(id) ?: throw IllegalStateException("User data not found")
            val email = userData["email"] as? String ?: throw IllegalStateException("Email not found for user")

            val existingAdmin = db.collection("admins")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!existingAdmin.isEmpty) {
                onResult(false, "User is already an admin")
                return
            }

            val adminData = hashMapOf<String, Any>("email" to email)

            db.runTransaction { transaction ->
                if(userData["specialization"] != null) {
                    val doctorDocRef = db.collection("doctors").document(id)
                    transaction.delete(doctorDocRef)
                    transaction.set(db.collection("admins").document(id), adminData)
                } else {
                    val patientDocRef = db.collection("patients").document(id)
                    transaction.delete(patientDocRef)
                    transaction.set(db.collection("admins").document(id), adminData)
                }
            }.await()

            onResult(true, "Successfully changed user to admin")
        } catch (e: Exception) {
            onResult(false, "user to admin: ${e.message}")
        }
    }

    override suspend fun getAllPatients(): List<Patient>? {
        return try {
            val snapshot = db.collection("patients").get().await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) Patient.fromMap(data) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAllDoctors(): List<Doctor>? {
        return try {
            val snapshot = db.collection("doctors").get().await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) Doctor.fromMap(data) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun listenForPatients(onUpdate: (List<Patient>) -> Unit): ListenerRegistration {
        return db.collection("patients")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val patients = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val userData = doc.data?.toMutableMap()
                        if (userData != null) {
                            userData["id"] = doc.id
                            Patient.fromMap(userData)
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                onUpdate(patients)
            }
    }

    override fun listenForDoctors(onUpdate: (List<Doctor>) -> Unit): ListenerRegistration {
        return db.collection("doctors")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val doctors = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val userData = doc.data?.toMutableMap()
                        if (userData != null) {
                            userData["id"] = doc.id
                            Doctor.fromMap(userData)
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                onUpdate(doctors)
            }
    }


    override suspend fun updateDoctorAvailability(doctorId: String, updateMap: Map<String, Any?>) {
        try {
            fs.collection("doctors").document(doctorId).update(updateMap).await()
        } catch (e: Exception) {
            throw Exception("updateDoctorAvailability: ${e.message}")
        }
    }

    override suspend fun getDoctorAvailability(doctorId: String): Map<Long, String> {
        return try {
            val snapshot = fs.collection("doctors").document(doctorId).get().await()
            val availabilityMap = mutableMapOf<Long, String>()

            val weeklyAvailability = snapshot.get("weeklyAvailability") as? Map<String, Map<String, Any>>
            weeklyAvailability?.forEach { (key, value) ->
                val status = value["status"] as? String
                val timestampKey = key.toLongOrNull()
                if (timestampKey != null && status != null) {
                    availabilityMap[timestampKey] = status
                }
            }
            availabilityMap
        } catch (e: Exception) {
            emptyMap()
        }
    }
}