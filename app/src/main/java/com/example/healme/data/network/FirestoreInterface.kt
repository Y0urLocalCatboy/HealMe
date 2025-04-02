package com.example.healme.data.network

import com.example.healme.data.models.user.User

interface FirestoreInterface {
    suspend fun registerUser(user: User)
    suspend fun loadUser(id: String): Map<String, Any?>?
    fun updateUser(user: User, data: Map<String, Any?>)
    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit)
    suspend fun patientToDoctor(patientId: String)
}