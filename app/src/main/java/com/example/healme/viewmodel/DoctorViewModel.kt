package com.example.healme.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.healme.data.models.Prescription
import com.example.healme.data.models.user.Doctor
import com.example.healme.data.models.user.Patient
import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreClass

class DoctorViewModel : ViewModel() {
    val fs = FirestoreClass()

    suspend fun getDoctorById(id: String): Map<String, Any?>? {
        return fs.loadUser(id)
    }
    suspend fun getDoctorsPatients(id: String): List<Patient>? {
        return fs.patientsFromDoctor(id)
    }

    suspend fun savePrescription(prescription: Prescription) {
        TODO()
    }

    suspend fun loadPrescriptions() {
        TODO()
    }

}