//package com.example.healme.ui.screens
//
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import com.example.healme.R
//import com.google.firebase.auth.FirebaseAuth
//
///**
// * Activity responsible for handling user login functionality.
// * It includes validating input fields, signing in the user using Firebase Authentication,
// * and navigating to the home screen or showing an error message on failure.
// */
//class LoginScreen : BaseActivity() {
//
//    // Firebase Authentication instance for user authentication
//    private lateinit var auth: FirebaseAuth
//
//    /**
//     * Called when the activity is created. Initializes Firebase Authentication,
//     * sets up UI elements, and handles the login button and register link actions.
//     */
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
//
//        // Initialize Firebase Authentication instance
//        auth = FirebaseAuth.getInstance()
//
//        // Define UI elements
//        val emailInput = findViewById<EditText>(R.id.EmailId)
//        val passwordInput = findViewById<EditText>(R.id.PasswordId)
//        val loginButton = findViewById<Button>(R.id.LogInButtonId)
//        val registerText = findViewById<TextView>(R.id.RegisterClickableId)
//
//        /**
//         * Handles the click event of the login button. It validates the input fields,
//         * signs the user in using Firebase Authentication, and navigates to the home activity on success.
//         */
//        loginButton.setOnClickListener {
//            val email = emailInput.text.toString().trim()
//            val password = passwordInput.text.toString().trim()
//
//            // Validate input fields
//            if (email.isEmpty() || password.isEmpty()) {
//                showToast("Please enter email and password")
//                return@setOnClickListener
//            }
//
//            // Sign in the user with email and password
//            auth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        // If login is successful, navigate to PatientHomeActivity
//                        showToast("Login successful!")
//                        openActivity(PatientHomeActivity::class.java)
//                    } else {
//                        // If login fails, show error message
//                        showToast("Login failed: ${task.exception?.message}")
//                    }
//                }
//        }
//
//        /**
//         * Handles the click event of the register text. It navigates to the RegisterActivity
//         * where a new user can register.
//         */
////        registerText.setOnClickListener {
////            openActivity(RegisterActivity::class.java)
////        }
//    }
//}