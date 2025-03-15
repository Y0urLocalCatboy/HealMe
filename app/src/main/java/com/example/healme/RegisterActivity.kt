package com.example.healme

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.example.healme.firebase.FirestoreClass
import com.example.healme.firebase.user.Doctor
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {

    private var registerButton: Button? = null

    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var inputName: EditText? = null
    private var inputSurname: EditText? = null
    private var inputYear: EditText? = null
    private val firestore = FirestoreClass()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        var Greg = Doctor("Doomslayer", "123", "jaja@wp.pl", "Gregory", "House", "2011")
        registerButton = findViewById(R.id.registerButton)
        registerButton?.setOnClickListener {
            lifecycleScope.launch {
                firestore.registerUser(Greg)
                showToast("Attempting to register: ${Greg.name} ${Greg.surname}")
            }
        }
    }
}