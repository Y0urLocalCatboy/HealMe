package com.example.healme.data.models

/**
 * Data class representing a visit in the healthcare system.
 *
 * @property id: Unique identifier for the prescription.
 * @property patientId: ID of the patient to whom the prescription is issued.
 * @property medicationName: Name of the medication prescribed.
 * @property dosage: Dosage of the medication prescribed.
 * @property instructions: Additional instructions for the patient regarding the medication.
 * @property dateIssued: Date when the prescription was issued.
 * @property doctorName: Name of the doctor who issued the prescription.
 * @property status: Status of the prescription (e.g., active, completed, cancelled).
 *
 */
data class Prescription(
    val id: String,
    val patientId: String,
    val medicationName: String,
    val dosage: String,
    val instructions: String,
    val dateIssued: String,
    val doctorName: String,
    val status: String
)