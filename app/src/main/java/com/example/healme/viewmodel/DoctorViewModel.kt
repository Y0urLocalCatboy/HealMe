package com.example.healme.viewmodel

import com.example.healme.data.models.user.Doctor
import com.example.healme.data.network.FirestoreClass

class DoctorViewModel {
    val fs = FirestoreClass()

    suspend fun getDoctorById(id: String): Map<String, Any?>? {
        return fs.loadUser(id)
    }
}