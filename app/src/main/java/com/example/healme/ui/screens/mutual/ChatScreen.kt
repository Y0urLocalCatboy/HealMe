package com.example.healme.ui.screens.mutual

import android.annotation.SuppressLint
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healme.R
import com.google.firebase.firestore.ListenerRegistration


@Composable
fun ChatScreen(navController: NavController,
               viewModel: ChatViewModel = viewModel(),
) {
    val fs = FirestoreClass()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var message by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var contacts by remember { mutableStateOf(listOf<User?>()) }
    var user by remember { mutableStateOf<Map<String, Any?>?>(null) }
    var isDoctor by remember { mutableStateOf(user?.get("specialization") != null) }
    var chosenContactIndex by remember { mutableStateOf(0) }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var messageListener by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(currentUser?.uid) {
        user = fs.loadUser(currentUser?.uid ?: "")
        isDoctor = user?.get("specialization") != null


        if (isDoctor) {
            contacts = fs.patientsFromDoctor(user?.get("id") as? String ?: "") ?: emptyList()
        } else {
            contacts = fs.doctorsFromPatient(user?.get("id") as? String ?: "") ?: emptyList()
        }
    }


    val chosenContact = if (contacts.isNotEmpty() && chosenContactIndex < contacts.size) {
        contacts[chosenContactIndex]
    } else null

    LaunchedEffect(chosenContact) {
        (messageListener as? ListenerRegistration)?.remove()

        chosenContact?.let { contact ->
            val userId = user?.get("id") as? String ?: ""

            messageListener = fs.listenForMessages(userId, contact.id) { newMessages ->
                messages = newMessages
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            (messageListener as? ListenerRegistration)?.remove()
        }
    }

    LaunchedEffect(chosenContact) {
        chosenContact?.let { contact ->
            val userId = user?.get("id") as? String ?: ""
            fs.getAllMessages(userId, contact.id) { success, retrievedMessages ->
                if (success) {
                    messages = retrievedMessages
                }
            }
        }
    }

    ChatContent(
        user = if (user != null) {
            User.fromMap(user!! as Map<String, Any>)

        } else {
            null
        } ?: return@ChatScreen,
        errorMessage = if (showError) viewModel.messageValidity(message) else null,
        contacts = contacts.filterNotNull(),
        chosenContactIndex = chosenContactIndex,
        onContactIndexChange = { chosenContactIndex = it },
        messages = messages,
        message = message,
        onMessageChange = { message = it },
        onSendMessage = {
            if (errorMessage == null && message.isNotEmpty() && chosenContact != null) {
                val userId = user?.get("id") as? String ?: ""
                fs.saveMessage(
                    Message(
                        content = message,
                        senderId = userId,
                        receiverId = chosenContact.id,
                        timestamp = System.currentTimeMillis().toString()
                    ),
                    onResult = { success, msg ->
                        if (success) {
                            message = ""
                            fs.getAllMessages(userId, chosenContact.id) { success, retrievedMessages ->
                                if (success) {
                                    messages = retrievedMessages
                                }
                            }
                        } else  {
                            showError = true
                            errorMessage = msg ?: "Couldn't send message"
                        }
                    }
                )
            }
        },
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    user: User,
    errorMessage: String?,
    contacts: List<User>,
    chosenContactIndex: Int,
    onContactIndexChange: (Int) -> Unit,
    messages: List<Message>,
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
){
    var showError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    val chosenContact = if (contacts.isNotEmpty() && chosenContactIndex < contacts.size) {
        contacts[chosenContactIndex]
    } else null

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
                                if (contacts.isNotEmpty() && chosenContactIndex > 0) {
                                    onContactIndexChange(chosenContactIndex - 1)
                                }
                            },
                            enabled = chosenContactIndex > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(
                                    if (user is Doctor) R.string.chat_pat_previous else R.string.chat_dr_previous
                                )
                            )
                        }

                        Text(
                            text = chosenContact?.let { "${it.name} ${it.surname}" } ?: stringResource(R.string.chat_no_contacts),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        IconButton(
                            onClick = {
                                if (contacts.isNotEmpty() && chosenContactIndex < contacts.size - 1) {
                                    onContactIndexChange(chosenContactIndex + 1)
                                }
                            },
                            enabled = chosenContactIndex < contacts.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = stringResource(
                                    if (user is Doctor) R.string.chat_pat_next else R.string.chat_dr_next
                                )
                            )
                        }
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
                        onValueChange = {
                            onMessageChange(it)
                            if (showError) {
                                showError = false
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Type your message here...") },
                        isError = errorMessage != null
                    )

                    Button(
                        onClick = onSendMessage,
                        enabled = chosenContact != null
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
                                text = ChatViewModel().formatTimestamp(message.timestamp),
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

    val patient = Patient(id = "321312",
        email = "patient@wp.pl",
        name = "Jan",
        surname = "Kowalski"
    )

    ChatContent(
        user = patient,
        errorMessage = null,
        contacts = listOf(doctor1, doctor2),
        chosenContactIndex = 0,
        onContactIndexChange = { },
        messages = listOf(
            Message(
                content = "To wassyp",
                senderId = "654321",
                receiverId = "321312",
                timestamp = System.currentTimeMillis().toString()
            ),
            Message(
                content = "Good lmao",
                senderId = "321312",
                receiverId = "654321",
                timestamp = (System.currentTimeMillis() - 3600000).toString()
            )
        ),
        message = "",
        onMessageChange = { },
        onSendMessage = {},
    )
}