package com.example.healme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healme.data.models.Prescription
import com.example.healme.data.models.user.Patient
import com.example.healme.data.network.FirestoreClass
import kotlinx.coroutines.launch

class PatientViewModel : ViewModel() {
    private val fs = FirestoreClass()

    /**
     * Fetches a patient by their ID from Firestore.
     *
     * @param patientId The ID of the patient to fetch.
     * @param onResult Callback function that is invoked with the fetched Patient object or null if not found or an error occurs.
     */
    fun getPatientById(patientId: String, onResult: (Patient?) -> Unit) {
        viewModelScope.launch {
            try {
                val patientMap = fs.loadUser(patientId)
                if (patientMap != null) {

                    if (patientMap["specialization"] == null) {
                        val patient = Patient.fromMap(patientMap)
                        onResult(patient)
                    } else {
                        onResult(null)
                    }
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    /**
     * Fetches prescriptions for a given patient ID from Firestore.
     *
     * @param patientId The ID of the patient whose prescriptions to fetch.
     * @param onResult Callback function that is invoked with the list of Prescription objects or null if an error occurs.
     */
    fun getPrescriptionsForPatient(patientId: String, onResult: (List<Prescription>?) -> Unit) {
        viewModelScope.launch {
            try {
                val prescriptions = fs.getPrescriptionsForPatient(patientId)
                onResult(prescriptions)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    /**
     * Updates the status of a prescription.
     *
     * @param prescriptionId The ID of the prescription to update.
     * @param newStatus The new status for the prescription.
     * @param onResult Callback function invoked with the result of the operation.
     */
    fun updatePrescriptionStatus(
        prescriptionId: String,
        newStatus: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                fs.updatePrescriptionStatus(prescriptionId, newStatus)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}