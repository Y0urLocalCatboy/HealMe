package com.example.healme.data.models

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