package com.example.healme.data.models

/**
 * Data class representing a visit in the healthcare system.
 *
 * @property doctorId The unique identifier of the doctor associated with the visit.
 * @property patientId The unique identifier of the patient associated with the visit.
 * @property timestamp The timestamp of when the visit occurred, represented as a Long.
 */
data class Visit(
    val doctorId: String = "",
    val patientId: String = "",
    val timestamp: Long = 0L,
)
