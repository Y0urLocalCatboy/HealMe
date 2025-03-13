package com.example.healme

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.healme.firebase.FirestoreClass
import com.example.healme.firebase.user.Doctor
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {

    private var registerButton: Button? = null
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