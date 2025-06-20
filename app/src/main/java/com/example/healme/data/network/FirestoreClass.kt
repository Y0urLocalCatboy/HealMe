package com.example.healme.data.network

import android.net.Uri
import android.util.Log
import androidx.compose.ui.res.stringResource
import com.example.healme.data.models.MedicalHistory
import com.example.healme.data.models.Message
import com.example.healme.data.models.Message.MessageType
import com.example.healme.data.models.Prescription
import com.example.healme.data.models.Visit
import com.example.healme.data.models.user.Doctor
import com.example.healme.data.models.user.Patient
import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreInterface
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.text.get
import kotlin.text.set
import kotlin.toString


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
        if (id.isBlank()) throw Exception("loadUser: Invalid ID")

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
            "senderId" to message.senderId,
            "timestamp" to message.timestamp,
            "type" to message.type.toString(),
            "imageUrl" to (message.imageUrl ?: ""),
            "fileUrl" to (message.fileUrl ?: ""),
            "fileName" to (message.fileName ?: "")
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

        val participants = listOf(message.senderId, message.receiverId)
        db.collection("messages")
            .whereIn("senderId", participants)
            .whereIn("receiverId", participants)
            .whereEqualTo("startDate", startDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val correctDoc = querySnapshot.documents.find { doc ->
                    val p1 = doc.getString("senderId")
                    val p2 = doc.getString("receiverId")
                    (p1 == message.senderId && p2 == message.receiverId) || (p1 == message.receiverId && p2 == message.senderId)
                }

                if (correctDoc == null) {
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
                    correctDoc.reference.update(
                        "weeklymessages",
                        FieldValue.arrayUnion(messageData)
                    )
                        .addOnSuccessListener {
                            onResult(true, "Message sent successfully!")
                        }
                        .addOnFailureListener { e ->
                            onResult(false, e.message ?: "Unknown error updating message")
                        }
                }
            }
            .addOnFailureListener { e ->
                onResult(false, "Error finding chat document: ${e.message}")
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
                    onResult(true, emptyList())
                } else {
                    val messages = mutableListOf<Message>()
                    for (document in querySnapshot.documents) {
                        val docP1 = document.getString("senderId") ?: ""
                        val docP2 = document.getString("receiverId") ?: ""
                        val weeklyMessages =
                            document.get("weeklymessages") as? List<Map<String, Any>> ?: continue

                        for (messageData in weeklyMessages) {
                            val msgSenderId = messageData["senderId"] as? String ?: ""
                            val msgReceiverId =
                                if (msgSenderId.isNotBlank() && msgSenderId == docP1) docP2 else docP1

                            val message = Message(
                                content = messageData["content"] as? String ?: "",
                                timestamp = messageData["timestamp"] as? String ?: "",
                                senderId = msgSenderId,
                                receiverId = msgReceiverId,
                                imageUrl = (messageData["imageUrl"] as? String)?.takeIf { it.isNotEmpty() },
                                type = try {
                                    (messageData["type"] as? String)?.let { MessageType.valueOf(it) }
                                        ?: MessageType.TEXT
                                } catch (e: Exception) {
                                    MessageType.TEXT
                                }
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
                        val docP1 = document.getString("senderId") ?: ""
                        val docP2 = document.getString("receiverId") ?: ""
                        val weeklyMessages =
                            document.get("weeklymessages") as? List<Map<String, Any>> ?: continue

                        for (messageData in weeklyMessages) {
                            val msgSenderId = messageData["senderId"] as? String ?: ""
                            val msgReceiverId =
                                if (msgSenderId.isNotBlank() && msgSenderId == docP1) docP2 else docP1

                            val message = Message(
                                content = messageData["content"] as? String ?: "",
                                timestamp = messageData["timestamp"] as? String ?: "",
                                senderId = msgSenderId,
                                receiverId = msgReceiverId,
                                imageUrl = (messageData["imageUrl"] as? String)?.takeIf { it.isNotEmpty() },
                                fileUrl = (messageData["fileUrl"] as? String)?.takeIf { it.isNotEmpty() },
                                fileName = (messageData["fileName"] as? String)?.takeIf { it.isNotEmpty() },
                                type = try {
                                    (messageData["type"] as? String)?.let { MessageType.valueOf(it) }
                                        ?: MessageType.TEXT
                                } catch (e: Exception) {
                                    MessageType.TEXT
                                }
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
            val email = userData["email"] as? String
                ?: throw IllegalStateException("Email not found for user")

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
                if (userData["specialization"] != null) {
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
            fs.collection("availability").document(doctorId).update(updateMap).await()
        } catch (e: Exception) {
            throw Exception("updateDoctorAvailability: ${e.message}")
        }
    }


    override fun savePrescription(
        prescription: Prescription,
        onResult: (Boolean, String) -> Unit
    ) {
        val prescriptionData = hashMapOf(
            "patientId" to prescription.patientId,
            "medicationName" to prescription.medicationName,
            "dosage" to prescription.dosage,
            "instructions" to prescription.instructions,
            "dateIssued" to prescription.dateIssued,
            "doctorName" to prescription.doctorName,
            "status" to prescription.status
        )

        db.collection("prescriptions")
            .add(prescriptionData)
            .addOnSuccessListener {
                onResult(true, "Prescription saved successfully!")
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "savePrescription firestore")
            }
    }


    override suspend fun getPrescriptionsForPatient(patientId: String): List<Prescription> {
        return try {
            val querySnapshot = db.collection("prescriptions")
                .whereEqualTo("patientId", patientId)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data
                    if (data != null) {
                        Prescription(
                            id = document.id,
                            patientId = data["patientId"] as String,
                            medicationName = data["medicationName"] as String,
                            dosage = data["dosage"] as String,
                            instructions = data["instructions"] as String,
                            dateIssued = data["dateIssued"] as String,
                            doctorName = data["doctorName"] as String,
                            status = data["status"] as String
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    override suspend fun getDoctorAvailability(doctorId: String): Map<Long, String> {
        return try {
            val snapshot = fs.collection("availability").document(doctorId).get().await()
            val availabilityMap = mutableMapOf<Long, String>()

            val weeklyAvailability =
                snapshot.get("weeklyAvailability") as? Map<String, Map<String, Any>>
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

    override suspend fun getPatientVisits(patientId: String): List<Pair<Long, String>> {
        return try {
            val documentSnapshot = fs.collection("visits")
                .document(patientId)
                .get()
                .await()

            if (!documentSnapshot.exists()) return emptyList()

            val visitsMap = documentSnapshot.get("visits") as? Map<String, Map<String, Any>>
                ?: return emptyList()

            val visitList = mutableListOf<Pair<Long, String>>()

            for ((_, visitData) in visitsMap) {
                val timestamp = (visitData["timestamp"] as? Long) ?: continue
                val doctorId = visitData["doctorId"] as? String ?: continue

                val doctorSnapshot = fs.collection("doctors").document(doctorId).get().await()
                val doctorName =
                    "${doctorSnapshot.getString("name") ?: ""} ${doctorSnapshot.getString("surname") ?: ""}".trim()

                visitList.add(Pair(timestamp, doctorName))
            }

            visitList
        } catch (e: Exception) {
            Log.e("FirestoreClass", "Error fetching patient visits", e)
            emptyList()
        }
    }


    override suspend fun bookVisit(doctorId: String, patientId: String, timestamp: Long) {
        try {
            val visitData = mapOf(
                "doctorId" to doctorId,
                "timestamp" to timestamp,
                "status" to "booked"
            )

            val visitsDocRef = db.collection("visits").document(patientId)

            val snapshot = visitsDocRef.get().await()
            val existingVisits = snapshot.get("visits") as? Map<*, *> ?: emptyMap<Any, Any>()
            val visitIndex = existingVisits.size + 1
            val visitKey = "visits.visit$visitIndex"

            val updateMap = mapOf(
                visitKey to visitData,
                "patientId" to patientId
            )

            visitsDocRef.update(updateMap).await()

            val availabilityUpdateMap = mapOf(
                "weeklyAvailability.$timestamp.status" to "booked",
                "weeklyAvailability.$timestamp.timestamp" to timestamp
            )

            fs.collection("availability")
                .document(doctorId)
                .update(availabilityUpdateMap)
                .await()

            addMedicalRecord(patientId = patientId, doctorId = doctorId, timestamp = timestamp)

        } catch (e: Exception) {
            Log.e("FirestoreClass", "Error booking visit or updating availability", e)
        }
    }


    override suspend fun getBookedTimestampsForDoctor(doctorId: String): List<Long> {
        return try {
            val snapshot = db.collection("visits")
                .whereEqualTo("doctorId", doctorId)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.getLong("timestamp") }
        } catch (e: Exception) {
            Log.e("FirestoreClass", "Error fetching booked timestamps", e)
            emptyList()
        }
    }

    override suspend fun addMedicalRecord(patientId: String, doctorId: String, timestamp: Long) {
        try {
            val docRef = db.collection("medicalHistory").document(patientId)
            val uniqueKey = UUID.randomUUID().toString()

            val recordData = mapOf(
                "doctorId" to doctorId,
                "timestamp" to timestamp,
                "content" to ""
            )

            val updateMap = mapOf(
                "records.$uniqueKey" to recordData,
                "patientId" to patientId
            )

            docRef.update(updateMap).await()

        } catch (e: Exception) {
            val fallbackMap = mapOf(
                "patientId" to patientId,
                "records" to mapOf(
                    UUID.randomUUID().toString() to mapOf(
                        "doctorId" to doctorId,
                        "timestamp" to timestamp,
                        "content" to ""
                    )
                )
            )
            db.collection("medicalHistory").document(patientId)
                .set(fallbackMap)
                .await()

            Log.e("FirestoreClass", "Doc didn't exist, created new medicalHistory", e)
        }
    }

    override suspend fun getPatientMedicalHistory(patientId: String): List<MedicalHistory> {
        return try {
            val snapshot = fs.collection("medicalHistory").document(patientId).get().await()
            if (!snapshot.exists()) return emptyList()

            val records =
                snapshot.get("records") as? Map<String, Map<String, Any>> ?: return emptyList()
            val result = mutableListOf<MedicalHistory>()

            for ((recordId, data) in records) {
                val doctorId = data["doctorId"] as? String
                if (doctorId.isNullOrBlank()) {
                    Log.w("FirestoreClass", "Skipping record $recordId due to missing doctorId")
                    continue
                }

                val doctorSnapshot = fs.collection("doctors").document(doctorId).get().await()
                val doctorName =
                    "${doctorSnapshot.getString("name") ?: ""} ${doctorSnapshot.getString("surname") ?: ""}".trim()

                val recordWithId = data.toMutableMap().apply {
                    put("id", recordId)
                    put("patientId", patientId)
                    put("doctorName", doctorName)
                }

                result.add(MedicalHistory.fromMap(recordWithId))
            }

            result
        } catch (e: Exception) {
            Log.e("FirestoreClass", "Error fetching medical history", e)
            emptyList()
        }
    }

    override suspend fun getUpcomingVisitForPatient(patientId: String): Pair<Visit, Doctor>? {
        return try {
            val docSnapshot = fs.collection("visits").document(patientId).get().await()
            if (!docSnapshot.exists()) return null

            val visitsMap =
                docSnapshot.get("visits") as? Map<String, Map<String, Any>> ?: return null

            val now = System.currentTimeMillis() / 1000

            val upcomingVisitEntry = visitsMap.mapNotNull { (_, data) ->
                val timestamp = data["timestamp"] as? Long ?: return@mapNotNull null
                val doctorId = data["doctorId"] as? String ?: return@mapNotNull null

                if (timestamp > now) {
                    Visit(doctorId = doctorId, patientId = patientId, timestamp = timestamp)
                } else null
            }.minByOrNull { it.timestamp } ?: return null

            val doctorSnapshot =
                fs.collection("doctors").document(upcomingVisitEntry.doctorId).get().await()
            val doctor = doctorSnapshot.toObject(Doctor::class.java) ?: return null

            Pair(upcomingVisitEntry, doctor)
        } catch (e: Exception) {
            null
        }
    }

    //TODO zmiana z czasu systemowego na firebasowy jak zrobisz to w czacie
    override suspend fun cleanUpPastVisits(patientId: String) {
        try {
            val visitDoc = fs.collection("visits").document(patientId).get().await()
            if (!visitDoc.exists()) return

            val visitsMap = visitDoc.get("visits") as? Map<String, Map<String, Any>> ?: return

            val currentTimestamp = System.currentTimeMillis() / 1000

            val historyDoc = fs.collection("medicalHistory").document(patientId).get().await()
            val medicalRecords =
                historyDoc.get("records") as? Map<String, Map<String, Any>> ?: emptyMap()

            val medicalTimestamps = medicalRecords.values.mapNotNull {
                it["timestamp"] as? Long
            }.toSet()

            val visitsToDelete = visitsMap.filter { (_, visitData) ->
                val timestamp = visitData["timestamp"] as? Long ?: return@filter false
                timestamp < currentTimestamp && timestamp in medicalTimestamps
            }.keys

            if (visitsToDelete.isNotEmpty()) {
                val updates =
                    visitsToDelete.associate { "visits.$it" to com.google.firebase.firestore.FieldValue.delete() }
                fs.collection("visits").document(patientId).update(updates).await()
            }

        } catch (e: Exception) {
            println("Error during visit cleanup for patient $patientId: ${e.message}")
        }
    }

    override suspend fun updateUserFcmToken(userId: String, userType: String, token: String) {
        try {
            val collection = when (userType) {
                "patients" -> "patients"
                "doctors" -> "doctors"
                else -> {
                    println("Skipping FCM update for unsupported type: $userType")
                    return
                }
            }

            fs.collection(collection)
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    println("FCM token updated successfully for $userId")
                }
                .addOnFailureListener {
                    println("Failed to update FCM token: ${it.message}")
                }
        } catch (e: Exception) {
            println("Exception in updateUserFcmToken: ${e.message}")
        }
    }


    override suspend fun getUserFcmToken(userId: String, userType: String): String? {
        return try {
            val collection = when (userType) {
                "patient" -> "patients"
                "doctor" -> "doctors"
                "admin" -> "admins"
                else -> throw IllegalArgumentException("Unknown user type: $userType")
            }

            val snapshot = fs.collection(collection).document(userId).get().await()
            snapshot.getString("fcmToken")
        } catch (e: Exception) {
            println("🔥 Failed to fetch FCM token: ${e.message}")
            null
        }
    }

    override suspend fun sendNotificationToToken(token: String, title: String, message: String) {
        val json = JSONObject()
        val notification = JSONObject()

        notification.put("title", title)
        notification.put("body", message)
        json.put("to", token)
        json.put("notification", notification)

        val requestBody = json.toString()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send")
            .addHeader("Authorization", "key=YOUR_SERVER_KEY_HERE") // Replace this
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(mediaType, requestBody))
            .build()

        val response = client.newCall(request).execute()
        println("Notification sent. Response: ${response.body?.string()}")
    }

    override fun uploadImage(
        uri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storageRef = FirebaseStorage.getInstance().reference
        val timestamp = System.currentTimeMillis()
        val imageFileName = "chat_images/${timestamp}_${UUID.randomUUID()}.jpg"
        val fileRef = storageRef.child(imageFileName)

        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        fileRef.putFile(uri, metadata)
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        onSuccess(downloadUri.toString())
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    override suspend fun saveDoctorAppointment(
        doctorId: String,
        patientName: String,
        timestamp: Long,
        onComplete: () -> Unit
    ) {
        val appointmentData = mapOf(
            "patientId" to patientName,
            "timestamp" to timestamp
        )

        val docRef = db.collection("appointments").document(doctorId)

        docRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                docRef.set(mapOf("appointments" to emptyMap<String, Any>())).addOnSuccessListener {
                    Log.d("Firestore", "Created base appointment document for doctorId=$doctorId")
                    runTransactionToAddAppointment(docRef, appointmentData, onComplete)
                }.addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to create base document: ${e.message}", e)
                }
            } else {
                runTransactionToAddAppointment(docRef, appointmentData, onComplete)
            }
        }.addOnFailureListener {
            Log.e("Firestore", "Failed to fetch doc to check existence: ${it.message}", it)
        }
    }

    private fun runTransactionToAddAppointment(
        docRef: DocumentReference,
        appointmentData: Map<String, Any>,
        onComplete: () -> Unit
    ) {
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentAppointments =
                snapshot.get("appointments") as? Map<String, Any?> ?: emptyMap()
            val newId = "appointment${currentAppointments.size + 1}"

            Log.d("Firestore", "Current appointments count: ${currentAppointments.size}")
            Log.d("Firestore", "New appointment ID: $newId")
            Log.d("Firestore", "Appointment data: $appointmentData")

            val updatedAppointments = currentAppointments.toMutableMap()
            updatedAppointments[newId] = appointmentData

            transaction.update(docRef, "appointments", updatedAppointments)
        }.addOnSuccessListener {
            Log.d("Firestore", "Appointment successfully saved for doctorId=${docRef.id}")
            onComplete()
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Failed to save appointment: ${e.message}", e)
        }
    }

    override suspend fun getCurrentPatientName(userId: String): String? {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("patients")
                .document(userId)
                .get()
                .await()

            val name = snapshot.getString("name")
            val surname = snapshot.getString("surname")
            if (name != null && surname != null) "$name $surname" else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getDoctorAppointments(doctorId: String): Map<String, Map<String, Any>>? {
        return try {
            val snapshot = db.collection("appointments").document(doctorId).get().await()
            snapshot.get("appointments") as? Map<String, Map<String, Any>>
        } catch (e: Exception) {
            null
        }
    }

    override fun uploadFile(
        uri: Uri,
        fileName: String,
        senderId: String,
        receiverId: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileRef =
            FirebaseStorage.getInstance().reference.child("files/${UUID.randomUUID()}_$fileName")

        val metadata = StorageMetadata.Builder()
            .setCustomMetadata("senderId", senderId)
            .setCustomMetadata("receiverId", receiverId)
            .build()

        fileRef.putFile(uri, metadata)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)

            }
    }
}
