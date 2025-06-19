package com.example.healme.ui.screens.patient

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.healme.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PatientNewsletterScreen(navController: NavHostController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    var optedIn by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        uid?.let {
            db.collection("patients").document(it).get().addOnSuccessListener { doc ->
                optedIn = doc.getBoolean("newsletterOptIn") == true
                loaded = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.newsletter_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_desc)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!loaded) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.newsletter_subscription_heading),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = optedIn, onCheckedChange = { optedIn = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.newsletter_optin_label))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        uid?.let {
                            db.collection("patients").document(it)
                                .update("newsletterOptIn", optedIn)
                                .addOnSuccessListener {
                                    message = stringResource(R.string.newsletter_save_success)
                                }
                                .addOnFailureListener {
                                    message = stringResource(R.string.newsletter_save_failure)
                                }
                        }
                    }
                ) {
                    Text(stringResource(R.string.save_button))
                }

                Spacer(modifier = Modifier.height(12.dp))

                message?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
