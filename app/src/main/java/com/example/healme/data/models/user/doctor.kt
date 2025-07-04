package com.example.healme.data.models.user

/**
 * Class representing a Doctor.
 *
 * @property id The identifier of the Doctor generated by firebase.
 * @property email The email address of the Doctor.
 * @property name The name of the Doctor.
 * @property surname The surname of the Doctor.
 * @property dateOfBirth The date of birth of the Doctor.
 * @property specialization The speciality of the Doctor.
 * @property newsletterOptIn Whether the doctor wants to receive newsletters.
 *
 */
class Doctor(
    val specialization: String = "",
    val patients: MutableList<String?> = mutableListOf(),
    val newsletterOptIn: Boolean = false,
    id: String = "",
    email: String = "",
    name: String = "",
    surname: String = "",
    dateOfBirth: String = "",
    fcmToken: String? = null
) : User(id, email, name, surname, dateOfBirth, fcmToken) {


    companion object {
        /**
         * Creates a Doctor object from firebase.
         *
         * @param data The data belonging to a Doctor.
         * @return A Doctor object.
         */
        fun fromMap(data: Map<String, Any?>): Doctor {
            return Doctor(
                specialization = data["specialization"] as? String ?: "",
                patients = data["patients"] as? MutableList<String?> ?: mutableListOf(),
                newsletterOptIn = data["newsletterOptIn"] as? Boolean ?: false,
                id = data["id"] as? String ?: "",
                email = data["email"] as? String ?: "",
                name = data["name"] as? String ?: "",
                surname = data["surname"] as? String ?: "",
                dateOfBirth = data["dateOfBirth"] as? String ?: "",
                fcmToken = data["fcmToken"] as? String
            )
        }
    }

    /**
     * Returns the name of the collection for Doctor objects in Firebase.
     *
     * @return "doctors"
     */
    override fun getCollectionName(): String {
        return "doctors"
    }

    /**
     * Returns the stringified representation of the Doctor.
     *
     * @return The name and surname of the Doctor as a String.
     */
    override fun toString(): String {
        return "${super.name} ${super.surname}"
    }
}
