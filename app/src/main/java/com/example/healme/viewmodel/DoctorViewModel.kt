package com.example.healme.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.healme.data.models.MedicalHistory
import com.example.healme.data.models.Prescription
import com.example.healme.data.models.user.Doctor
import com.example.healme.data.models.user.Patient
import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreClass

class DoctorViewModel : ViewModel() {
    val fs = FirestoreClass()

    /**
     * Fetches the doctor's appointments from Firestore.
     *
     * @param doctorId The ID of the doctor whose appointments are to be fetched.
     *
     * @return A map containing appointment details or null if no appointments are found.
     */
    suspend fun getDoctorById(id: String): Map<String, Any?>? {
        return fs.loadUser(id)
    }

    /**
     * Fetches the list of patients associated with a doctor.
     *
     * @param id The ID of the doctor whose patients are to be fetched.
     *
     * @return A list of Patient objects or null if no patients are found.
     */
    suspend fun getDoctorsPatients(id: String): List<Patient>? {
        return fs.patientsFromDoctor(id)
    }

    /**
     * Fetches the list of doctors associated with a patient.
     *
     * @param id The ID of the patient whose doctors are to be fetched.
     *
     * @return A list of Doctor objects or null if no doctors are found.
     */
    fun savePrescription(prescription: Prescription, onResult: (Boolean, String) -> Unit) {
        fs.savePrescription(prescription, onResult)
    }

    /**
     * Loads prescriptions for a specific patient.
     *
     * @param patientId The ID of the patient whose prescriptions are to be loaded.
     *
     * @return A list of Prescription objects or null if no prescriptions are found.
     */
    suspend fun loadPrescriptions(patientId: String): List<Prescription>? {
        return fs.getPrescriptionsForPatient(patientId)
    }

    /**
     * Fetches the medical history of a patient.
     *
     * @param patientId The ID of the patient whose medical history is to be fetched.
     *
     * @return A list of MedicalHistory objects or null if no medical history is found.
     */
    suspend fun getPatientMedicalHistory(patientId: String): List<MedicalHistory>? {
        return fs.getPatientMedicalHistory(patientId)
    }
}