package com.example.healme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healme.data.models.MedicalHistory
import com.example.healme.data.models.Prescription
import com.example.healme.data.models.user.Patient
import com.example.healme.data.network.FirestoreClass
import kotlinx.coroutines.launch

class DoctorViewModel : ViewModel() {
    val fs = FirestoreClass()

    /**
     * Fetches the doctor's data from Firestore.
     *
     * @param id The ID of the doctor.
     * @return A map containing the doctor's details, or null if not found.
     */
    suspend fun getDoctorById(id: String): Map<String, Any?>? {
        return fs.loadUser(id)
    }

    /**
     * Fetches the list of patients associated with a doctor.
     *
     * @param id The ID of the doctor.
     * @return A list of Patient objects or null if no patients are found.
     */
    suspend fun getDoctorsPatients(id: String): List<Patient>? {
        return fs.patientsFromDoctor(id)
    }

    /**
     * Saves a prescription for a patient.
     *
     * @param prescription The Prescription object to be saved.
     * @param onResult Callback to report success or failure.
     */
    fun savePrescription(prescription: Prescription, onResult: (Boolean, String) -> Unit) {
        fs.savePrescription(prescription, onResult)
    }

    /**
     * Loads prescriptions for a specific patient.
     *
     * @param patientId The ID of the patient.
     * @return A list of Prescription objects or null if none found.
     */
    suspend fun loadPrescriptions(patientId: String): List<Prescription>? {
        return fs.getPrescriptionsForPatient(patientId)
    }

    /**
     * Fetches the medical history of a patient.
     *
     * @param patientId The ID of the patient.
     * @return A list of MedicalHistory objects or null if none found.
     */
    suspend fun getPatientMedicalHistory(patientId: String): List<MedicalHistory>? {
        return fs.getPatientMedicalHistory(patientId)
    }

    /**
     * Cleans up past appointments for a doctor.
     * Moves them to `pastappointments` collection and deletes from `appointments`.
     *
     * @param doctorId The ID of the doctor.
     */
    fun cleanUpPastAppointments(doctorId: String) {
        viewModelScope.launch {
            fs.cleanUpPastAppointments(doctorId)
        }
    }

    /**
     * Calls the FirestoreClass to retrieve past appointments for the specified doctor.
     *
     * This is a suspend function and should be called within a coroutine context.
     * It delegates to Firestore to fetch data stored in the `pastappointments/{doctorId}` document.
     *
     * @param doctorId The unique ID of the doctor whose past appointments are being retrieved.
     * @return A map of appointment data where each entry contains the appointment ID as the key
     *         and a map of appointment details as the value, or null if no data is found.
     */
    suspend fun getPastAppointments(doctorId: String): Map<String, Map<String, Any>>? {
        return fs.getPastAppointments(doctorId)
    }


}
