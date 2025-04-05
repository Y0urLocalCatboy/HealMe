package com.example.healme.ui.screens.mutual

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.healme.data.models.Message
import com.example.healme.data.models.user.Doctor
import com.example.healme.data.models.user.Patient
import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreClass
import com.example.healme.ui.components.popups.SnackBarEffect
import com.example.healme.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healme.ui.components.popups.showToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(navController: NavController,
               viewModel: ChatViewModel
) {
    val fs = FirestoreClass()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    if (currentUser == null) {
        SnackBarEffect(remember { SnackbarHostState() },
            "A critical error has occured - the user is not logged in",
            true)
    }

    var message by remember { mutableStateOf("") }
    val errorMessage = viewModel.messageValidity(message)
    var doctors by remember { mutableStateOf(listOf<Doctor?>()) }
    var user by remember { mutableStateOf<Map<String, Any?>?>(null) }
    var chosenDoctor by remember { mutableStateOf<Doctor?>(null) }

    LaunchedEffect(currentUser?.uid) {
        user = fs.loadUser(currentUser?.uid ?: "")
        doctors = fs.doctorsFromPatient(user?.get("id") as? String ?: "")?: emptyList()

    }

    ChatContent(
        user = if (user != null) {
            if (user!!["specialization"] != null) {
                Doctor.fromMap(user!!)
            } else {
                Patient.fromMap(user!!)
            }
        } else {
            Patient()
        },
        errorMessage = errorMessage,
        doctors = doctors.filterNotNull(),
        message = message,
        onMessageChange = { message = it },
        onSendMessage = {
            if (errorMessage == null && message.isNotEmpty() && chosenDoctor != null) {
                fs.saveMessage(
                    Message(
                        content = message,
                        senderId = user?.get("id") as? String ?: "",
                        receiverId = chosenDoctor?.id ?: "",
                        timestamp = System.currentTimeMillis().toString()
                    ),
                    startDate = System.currentTimeMillis().toString(),
                    onResult = { success, msg ->
                        if (success) {
                            message = ""
                        } else {}
                    }
                )
            }
        },
        onNextDoctorClick = {},
        onBackDoctorClick = {},
        onBackClick = {}

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    user: User,
    errorMessage: String?,
    doctors: List<Doctor>,
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onNextDoctorClick: () -> Unit,
    onBackDoctorClick: () -> Unit,
    onBackClick: () -> Unit,
){
    val snackbarHostState = remember { SnackbarHostState() }

    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    val chosenDoctorIndex = remember { mutableStateOf(0) }
    val chosenDoctor = if (doctors.isNotEmpty() && chosenDoctorIndex.value < doctors.size) {
        doctors[chosenDoctorIndex.value]
    } else null

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    val fs = FirestoreClass()

    LaunchedEffect(chosenDoctor) {
        chosenDoctor?.let { doctor ->
            fs.getAllMessages(user.id, doctor.id) { success, retrievedMessages ->
                if (success) {
                    messages = retrievedMessages
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                if (doctors.isNotEmpty() && chosenDoctorIndex.value > 0) {
                                    chosenDoctorIndex.value--
                                    onBackDoctorClick()
                                }
                            },
                            enabled = chosenDoctorIndex.value > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Previous doctor"
                            )
                        }

                        Text(
                            text = chosenDoctor?.let { "${it.name} ${it.surname}" } ?: "No doctor selected",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        IconButton(
                            onClick = {
                                if (doctors.isNotEmpty() && chosenDoctorIndex.value < doctors.size - 1) {
                                    chosenDoctorIndex.value++
                                    onNextDoctorClick()
                                }
                            },
                            enabled = chosenDoctorIndex.value < doctors.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Next doctor"
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = message,
                        onValueChange = onMessageChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Type your message here...") },
                        isError = errorMessage != null
                    )

                    Button(
                        onClick = onSendMessage,
                        enabled = message.isNotEmpty() && errorMessage == null && chosenDoctor != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send message"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            reverseLayout = true
        ) {
            items(messages.sortedByDescending { it.timestamp }) { message ->
                val isUserMessage = message.senderId == user.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 280.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUserMessage)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = formatTimestamp(message.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val date = Date(timestamp.toLong())
        SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        timestamp
    }
}


@Preview
@Composable
fun ChatScreenPreview() {
    val doctor1 = Doctor(
        id = "123456",
        specialization = "Diagnostyk",
        email = "grzegorz.dom@wp.pl",
        name = "Grzegorz",
        surname = "Dom"
    )
    val doctor2 = Doctor(
        id = "654321",
        specialization = "Diagnostyk",
        email = "doktor.gonitwa@wp.pl",
        name = "Doktor",
        surname = "Gonitwa"
    )

    val patient = Patient(id = "user_1",
        email = "patient@example.com",
        name = "Jan",
        surname = "Kowalski"
    )

    ChatContent(
        user = patient,
        errorMessage = null,
        doctors = listOf(doctor1, doctor2),
        message = "",
        onMessageChange = { },
        onSendMessage = {},
        onNextDoctorClick = {},
        onBackDoctorClick = {},
        onBackClick = {}
    )
}