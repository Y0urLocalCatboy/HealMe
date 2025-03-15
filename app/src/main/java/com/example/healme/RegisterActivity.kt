package com.example.healme.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.healme.BaseActivity
import com.example.healme.LoginActivity
import com.example.healme.R
import com.example.healme.firebase.user.Patient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity responsible for handling the registration process of a new user (patient).
 * This includes validating input fields, creating a new user in Firebase Authentication,
 * and saving the user information in Firebase Firestore.
 */
class RegisterActivity : BaseActivity() {

    // Firebase Authentication and Firestore instances
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    /**
     * Called when the activity is created. Initializes Firebase Authentication, Firestore,
     * and sets up UI elements.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Define UI elements
        val nameInput = findViewById<EditText>(R.id.editTextName)
        val surnameInput = findViewById<EditText>(R.id.editTextSurname)
        val emailInput = findViewById<EditText>(R.id.editTextEmail)
        val passwordInput = findViewById<EditText>(R.id.editTextPassword)
        val secondPasswordInput = findViewById<EditText>(R.id.editTextPassword2)
        val dateOfBirthInput = findViewById<EditText>(R.id.editTextDate)
        val registerButton = findViewById<Button>(R.id.registerButton)

        /**
         * Handles the click event of the register button. It validates the input fields,
         * checks if passwords match, and creates a new user in Firebase Authentication and Firestore.
         */
        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val surname = surnameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = secondPasswordInput.text.toString().trim()
            val dateOfBirth = dateOfBirthInput.text.toString().trim()

            // Validate input fields
            val nameError = nameValidity(name)
            val surnameError = surnameValidity(surname)
            val ageError = ageValidity(dateOfBirth)

            if (nameError != null) {
                showToast(nameError)
                return@setOnClickListener
            }
            if (surnameError != null) {
                showToast(surnameError)
                return@setOnClickListener
            }
            if (ageError != null) {
                showToast(ageError)
                return@setOnClickListener
            }

            // Check if passwords match
            if (password != confirmPassword) {
                secondPasswordInput.error = "Passwords do not match"
                return@setOnClickListener
            }

            // Create a new user with the provided email and password
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User creation was successful
                    val userId = auth.currentUser?.uid ?: ""

                    // Create a new Patient object with the provided details
                    val newPatient = Patient(
                        medicalHistory = mutableListOf(),  // Empty list initially
                        messages = mutableListOf(),       // Empty list initially
                        id = userId,
                        email = email,
                        name = name,
                        surname = surname,
                        dateOfBirth = dateOfBirth
                    )

                    // Save the patient's data in Firestore
                    db.collection("patients").document(userId).set(newPatient)
                        .addOnSuccessListener {
                            // Success: Show confirmation and navigate to LoginActivity
                            showToast("Registration successful!")
                            openActivity(LoginActivity::class.java)
                        }
                        .addOnFailureListener { e ->
                            // Error occurred while saving data
                            showToast("Error saving user data: ${e.message}")
                        }
                } else {
                    // Error occurred while creating the user
                    showToast("Registration failed: ${task.exception?.message}")
                }
            }
        }
    }
}
