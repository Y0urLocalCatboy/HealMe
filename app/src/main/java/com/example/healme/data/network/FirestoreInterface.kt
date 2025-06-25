package com.example.healme.data.network

import android.net.Uri
import com.example.healme.data.models.MedicalHistory
import com.example.healme.data.models.Message
import com.example.healme.data.models.Prescription
import com.example.healme.data.models.Visit
import com.example.healme.data.models.user.*
import com.google.firebase.firestore.ListenerRegistration

/**
 * Interface defining methods for interacting with Firestore and Firebase services.
 * This includes user management, messaging, appointments, and medical records.
 */
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
     * Converts a doctor account to a patient account.
     *
     * @param doctorId ID of the doctor to convert.
     */
    suspend fun doctorToPatient(doctorId: String)

    /**
     * Saves a message to Firestore.
     *
     * @param message The message content.
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
    suspend fun updateDoctorAvailability(doctorId: String,
                                         availabilityMap: Map<String, Any?>)

    /**
     * Retrieves availability data for a doctor.
     * @param doctorId ID of the doctor.
     * @return Map of timestamps to availability status ("available"/"unavailable").
     */
    suspend fun getDoctorAvailability(doctorId: String): Map<Long, String>

    /**
     * Changes a user's role to admin.
     *
     * @param id The ID of the user to convert.
     * @param onResult Callback function with result (success, message).
     */
    suspend fun changeToAdmin(id: String,
                              onResult: (Boolean, String) -> Unit)

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
     * Retrieves all prescriptions assigned to a specific patient.
     *
     * @param patientId The ID of the patient whose prescriptions are being fetched.
     * @return A list of Prescription objects for the specified patient.
     */
    suspend fun getPrescriptionsForPatient(patientId: String): List<Prescription>

    /**
     * Retrieves all past and future visits for a specific patient.
     *
     * @param patientId The ID of the patient whose visit records are being retrieved.
     * @return A list of pairs where each pair contains the timestamp of the visit and the associated doctor ID.
     */
    suspend fun getPatientVisits(patientId: String): List<Pair<Long, String>>

    /**
     * Books a new visit for a patient with a specific doctor at a specified time.
     *
     * @param doctorId The ID of the doctor for the visit.
     * @param patientId The ID of the patient who is booking the visit.
     * @param timestamp The time (in UTC seconds) at which the visit is scheduled.
     */
    suspend fun bookVisit(doctorId: String,
                          patientId: String,
                          timestamp: Long
    )

    /**
     * Retrieves all timestamps for future visits booked with a given doctor.
     *
     * @param doctorId The ID of the doctor whose appointments are being fetched.
     * @return A list of timestamps representing booked appointment times.
     */
    suspend fun getBookedTimestampsForDoctor(doctorId: String): List<Long>

    /**
     * Adds a medical record to the patient's medical history after a completed visit.
     *
     * @param patientId The ID of the patient.
     * @param doctorId The ID of the doctor who conducted the visit.
     * @param timestamp The timestamp of the visit that resulted in this medical record.
     */
    suspend fun addMedicalRecord(patientId: String,
                                 doctorId: String,
                                 timestamp: Long
    )

    /**
     * Retrieves the entire medical history for a specific patient.
     *
     * @param patientId The ID of the patient whose medical history is being fetched.
     * @return A list of MedicalHistory entries associated with the patient.
     */
    suspend fun getPatientMedicalHistory(patientId: String): List<MedicalHistory>

    /**
     * Retrieves the next upcoming visit scheduled for a patient.
     *
     * @param patientId The ID of the patient.
     * @return A Pair containing the Visit object and the associated Doctor, or null if no future visit is scheduled.
     */
    suspend fun getUpcomingVisitForPatient(patientId: String): Pair<Visit, Doctor>?

    /**
     * Cleans up past visits for a patient by deleting entries whose timestamps have passed
     * and are already saved in the medical history.
     *
     * @param patientId The ID of the patient whose past visits should be cleaned.
     */
    suspend fun cleanUpPastVisits(patientId: String)


    /**
     * Updates the FCM token for a given user.
     *
     * @param userId The ID of the user.
     * @param userType The type of the user ("patient", "doctor", or "admin").
     * @param token The new FCM token.
     */
    suspend fun updateUserFcmToken(userId: String,
                                   userType: String,
                                   token: String
    )

    /**
     * Retrieves the FCM token for a given user.
     *
     * @param userId The ID of the user.
     * @param userType The type of the user ("patient", "doctor", or "admin").
     * @return The FCM token, or null if not found.
     */
    suspend fun getUserFcmToken(userId: String,
                                userType: String
    ): String?

    /**
     * Sends a push notification using Firebase Cloud Messaging (FCM) to a specific device token.
     *
     * @param token The FCM token of the target device.
     * @param title The title of the notification message.
     * @param message The body content of the notification.
     */
    suspend fun sendNotificationToToken(token: String,
                                        title: String,
                                        message: String
    )

    /**
     * Uploads an image to Firebase Storage and returns the download URL.
     *
     * @param uri The URI of the image to upload.
     * @param onSuccess Callback function invoked with the download URL on success.
     * @param onFailure Callback function invoked with an exception on failure.
     */
    fun uploadImage(uri: Uri,
                    onSuccess: (String) -> Unit,
                    onFailure: (Exception) -> Unit
    )

    /**
     * Saves an appointment for a specific doctor to Firestore.
     *
     * @param doctorId The ID of the doctor.
     * @param patientName The full name of the patient.
     * @param timestamp The Unix timestamp of the appointment in seconds.
     * @param onComplete Callback invoked with true if successful, false otherwise.
     */
    suspend fun saveDoctorAppointment(
        doctorId: String,
        patientName: String,
        timestamp: Long,
        onComplete: (() -> Unit)
    )

    /**
     * Retrieves the full name (name + surname) of a patient from Firestore.
     *
     * @param userId The ID of the patient.
     * @return The full name as a String, or null if not found.
     */
    suspend fun getCurrentPatientName(userId: String): String?

    /**
     * Retrieves all appointments for a given doctor from Firestore.
     *
     * @param doctorId The ID of the doctor whose appointments should be fetched.
     * @return A map of appointment IDs to their data, or null if the retrieval fails.
     */
    suspend fun getDoctorAppointments(doctorId: String): Map<String, Map<String, Any>>?

    /**
     * Uploads a file to Firebase Storage and returns the download URL.
     *
     * @param uri The URI of the file to upload.
     * @param fileName The name of the file to be saved in Firebase Storage.
     * @param onSuccess Callback function invoked with the download URL on success.
     * @param onFailure Callback function invoked with an exception on failure.
     */
    fun uploadFile(
        uri: Uri,
        fileName: String,
        senderId: String,
        receiverId: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    )

    /**
     * Cleans up past doctor appointments for a given doctor.
     * Saves them to `pastappointments` collection before deletion.
     *
     * @param doctorId The ID of the doctor.
     */
    suspend fun cleanUpPastAppointments(doctorId: String)

    /**
     * Retrieves past appointments for a specific doctor from the Firestore `pastappointments` collection.
     *
     * Each document in the collection represents a doctor and contains a nested `appointments` map
     * with appointment identifiers as keys and appointment data (e.g., timestamp, patientId) as values.
     *
     * @param doctorId The unique ID of the doctor whose past appointments are being fetched.
     * @return A map of past appointment entries where the key is the appointment ID and the value is a map of appointment details,
     *         or null if the document does not exist or fetching fails.
     */
    suspend fun getPastAppointments(doctorId: String): Map<String, Map<String, Any>>?

    /**
     * Updates the status of a specific prescription in Firestore.
     *
     * @param prescriptionId The ID of the prescription to update.
     * @param newStatus The new status to set for the prescription.
     */
    suspend fun updatePrescriptionStatus(prescriptionId: String,
                                         newStatus: String
    )

    /**
     * Adds a patient to a doctor's list of patients in Firestore.
     *
     * @param doctorId The ID of the doctor to whom the patient is being added.
     * @param patientId The ID of the patient being added.
     * @param onResult Callback function with result (success, message).
     */
    suspend fun addPatientToDoctor(
        doctorId: String,
        patientId: String,
        onResult: (Boolean, String) -> Unit
    )

    /**
     * Removes a patient from a doctor's list of patients in Firestore.
     *
     * @param doctorId The ID of the doctor from whom the patient is being removed.
     * @param patientId The ID of the patient being removed.
     * @param onResult Callback function with result (success, message).
     */
    suspend fun removePatientFromDoctor(
        doctorId: String,
        patientId: String,
        onResult: (Boolean, String) -> Unit
    )
}