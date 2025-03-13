package com.example.healme.firebase

import com.example.healme.firebase.user.User

interface FirestoreInterface {
    suspend fun registerUser(user: User)
    fun loadUser(id: String)
    fun updateUser(user: User, data: Map<String, Any?>)

}