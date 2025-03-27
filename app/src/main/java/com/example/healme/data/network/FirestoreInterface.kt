package com.example.healme.data.network

import com.example.healme.data.models.user.User

interface FirestoreInterface {
    suspend fun registerUser(user: User)
    fun loadUser(id: String)
    fun updateUser(user: User, data: Map<String, Any?>)
    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit)

}