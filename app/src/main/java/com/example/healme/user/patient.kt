package com.example.healme.user

class Patient(
    id: String = "",
    email: String = "",
    name: String = "",
    surname: String = "",
    dateOfBirth: String = "",
    val medicalHistory: MutableList<String> = mutableListOf()
) : User(id, email, name, surname, dateOfBirth) {
    companion object {

        fun fromMap(data: Map<String, Any?>): Patient {
            return Patient(

                medicalHistory = (data["medicalHistory"] as? MutableList<String> ?: mutableListOf()),

                id = data["id"] as? String ?: "",

                name = data["name"] as? String ?: "",

                email = data["email"] as? String ?: "",

                surname = data["surname"] as? String ?: "",

                dateOfBirth = data["dateOfBirth"] as? String ?: "",

            )
        }
    }

    override fun getCollectionName(): String {
        return "patients"
    }

    override fun toString(): String {
        return "${super.name} ${super.surname}"
    }
}