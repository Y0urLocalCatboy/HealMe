package com.example.healme.ui.screens.mutual

import android.R.attr.navigationIcon
import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.healme.R
import com.example.healme.data.models.Message.MessageType
import com.google.firebase.firestore.ListenerRegistration
import com.instacart.library.truetime.TrueTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * ChatScreen is a Composable function that displays the chat interface.
 *
 * @param viewModel The ChatViewModel instance for managing chat-related data.
 */
@Composable
fun ChatScreen(
    navController: NavController,
    chatViewModel: ChatViewModel = viewModel(),
) {
    val fs = FirestoreClass()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var messageText by remember { mutableStateOf("") }
    var showInputError by remember { mutableStateOf(false) }
    var sendErrorMessage by remember { mutableStateOf<String?>(null) }

    var allContacts by remember { mutableStateOf(emptyList<User>()) }
    var appUser by remember { mutableStateOf<User?>(null) }
    var isDoctor by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var chosenContact by remember { mutableStateOf<User?>(null) }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var messageListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    var showImagePreviewDialog by remember { mutableStateOf(false) }
    var imagePreviewUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (chosenContact != null) {
                imagePreviewUri = it
                showImagePreviewDialog = true
            } else {
                // Optionally show a message that a contact must be selected first
                sendErrorMessage = context.getString(R.string.chat_select_contact_prompt)
            }
        }
    }

    if (showImagePreviewDialog && imagePreviewUri != null && chosenContact != null) {
        ImagePreviewDialog(
            imageUri = imagePreviewUri!!,
            onDismiss = {
                showImagePreviewDialog = false
                imagePreviewUri = null
            },
            onSend = { dialogMessageText ->
                showImagePreviewDialog = false
                val uriToSend = imagePreviewUri!!
                imagePreviewUri = null

                fs.uploadImage(
                    uriToSend,
                    onSuccess = { imageUrl ->
                        fs.saveMessage(
                            Message(
                                content = dialogMessageText,
                                senderId = currentUser?.uid ?: "",
                                receiverId = chosenContact!!.id,
                                timestamp = try {
                                    TrueTime.now().time.toString()
                                } catch (e: Exception) {
                                    System.currentTimeMillis().toString()
                                },
                                imageUrl = imageUrl,
                                type = MessageType.IMAGE
                            ),
                            onResult = { success, errorMsg ->
                                if (!success) {
                                    sendErrorMessage = errorMsg.takeIf { !it.isNullOrBlank() } ?: context.getString(R.string.chat_send_message_error)
                                }
                            }
                        )
                    },
                    onFailure = { exception ->
                        sendErrorMessage = exception.message ?: context.getString(R.string.chat_image_upload_error)
                    }
                )
            }
        )
    }


    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(sendErrorMessage) {
        sendErrorMessage?.let {
            snackbarHostState.showSnackbar(it)
            sendErrorMessage = null
        }
    }

    LaunchedEffect(currentUser?.uid) {
        val loadedUserMap = fs.loadUser(currentUser?.uid ?: "")
        loadedUserMap?.let {
            appUser = User.fromMap(it as Map<String, Any>)
            isDoctor = appUser is Doctor

            val fetchedContacts = if (isDoctor) {
                fs.patientsFromDoctor(appUser?.id ?: "") ?: emptyList()
            } else {
                fs.doctorsFromPatient(appUser?.id ?: "") ?: emptyList()
            }
            allContacts = fetchedContacts.filterNotNull()
        }
    }

    val filteredContacts = remember(allContacts, searchQuery) {
        if (searchQuery.isBlank()) {
            allContacts
        } else {
            allContacts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.surname.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(chosenContact?.id, appUser?.id) {
        messageListener?.remove()
        messageListener = null
        messages = emptyList()

        val currentAppUserId = appUser?.id
        if (currentAppUserId != null && chosenContact != null) {
            messageListener = fs.listenForMessages(currentAppUserId, chosenContact!!.id) { newMessages ->
                messages = newMessages.sortedByDescending { it.timestamp }
            }
            fs.getAllMessages(currentAppUserId, chosenContact!!.id) { success, retrievedMessages ->
                if (success) {
                    messages = retrievedMessages.sortedByDescending { it.timestamp }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            messageListener?.remove()
        }
    }
     lateinit var appTrueTime: TrueTime

    val messageInputError = if (showInputError) chatViewModel.messageValidity(messageText) else null
    val yesterday = stringResource(R.string.chat_yesterday)
    val today = stringResource(R.string.chat_today)

    appUser?.let { currentUserInstance ->
        ChatContent(
            currentUser = currentUserInstance,
            isCurrentUserDoctor = isDoctor,
            snackbarHostState = snackbarHostState,
            searchQuery = searchQuery,
            onSearchQueryChange = { query -> searchQuery = query },
            filteredContacts = filteredContacts,
            allContactsEmpty = allContacts.isEmpty(),
            chosenContact = chosenContact,
            onContactSelected = { selectedContact ->
                chosenContact = selectedContact
                searchQuery = ""
                messages = emptyList()
            },
            messages = messages,
            messageText = messageText,
            onMessageTextChange = {
                messageText = it
                if (showInputError) showInputError = false
            },
            onSendMessage = {
                val validationError = messageInputError
                if (validationError == null && messageText.isNotEmpty() && chosenContact != null) {
                    showInputError = false
                    fs.saveMessage(
                        Message(
                            content = messageText,
                            senderId = currentUserInstance.id,
                            receiverId = chosenContact!!.id,
                            timestamp = try {
                                TrueTime.now().time.toString()
                            } catch (e: Exception) {
                                System.currentTimeMillis().toString()
                            },
                            type = MessageType.TEXT
                        ),
                        onResult = { success, errorMsg ->
                            if (success) {
                                messageText = ""
                            } else {
                                sendErrorMessage = errorMsg.takeIf { !it.isNullOrBlank() } ?: context.getString(R.string.chat_send_message_error)
                            }
                        }
                    )
                } else {
                    showInputError = true
                    if (validationError != null) {
                        sendErrorMessage = validationError
                    } else if (messageText.isEmpty()) {
                        sendErrorMessage = context.getString(R.string.chat_message_short)
                    } else if (chosenContact == null) {
                        sendErrorMessage = context.getString(R.string.chat_select_contact_prompt)
                    }
                }
            },
            onNavigateBack = {
                navController.popBackStack()
            },
            messageInputError = messageInputError, // Use the validated error
            formatTimestamp = { timestamp -> chatViewModel.formatTimestamp(timestamp, yesterday, today) },
            onPickImage = {
                if (chosenContact != null) {
                    imagePickerLauncher.launch("image/*")
                } else {
                    sendErrorMessage = context.getString(R.string.chat_select_contact_prompt)
                }
            }
        )
    } ?: run {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.chat_loading), style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    currentUser: User,
    isCurrentUserDoctor: Boolean,
    snackbarHostState: SnackbarHostState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredContacts: List<User>,
    allContactsEmpty: Boolean,
    chosenContact: User?,
    onContactSelected: (User) -> Unit,
    messages: List<Message>,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onNavigateBack: () -> Unit,
    messageInputError: String?,
    formatTimestamp: (String) -> String,
    onPickImage: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isCurrentUserDoctor) { // Show back button only for doctors as per original logic
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.chat_back_button_description)
                                )
                            }
                        }

                        var searchExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = searchExpanded,
                            onExpandedChange = {
                                searchExpanded = !searchExpanded
                                if (searchExpanded && filteredContacts.isNotEmpty()) keyboardController?.show() else keyboardController?.hide()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(
                                    start = if (isCurrentUserDoctor) 0.dp else 8.dp,
                                    end = 8.dp
                                )
                        ) {
                            OutlinedTextField(
                                value = if (searchExpanded) searchQuery else chosenContact?.let { "${it.name} ${it.surname}" } ?: searchQuery,
                                onValueChange = { query ->
                                    onSearchQueryChange(query)
                                    if (!searchExpanded) searchExpanded = true
                                },
                                label = {
                                    Text(
                                        chosenContact?.let { "${it.name} ${it.surname}" }
                                            ?: stringResource(R.string.chat_select_contact_prompt)
                                    )
                                },
                                placeholder = { Text(stringResource(R.string.chat_search_placeholder)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = searchExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                singleLine = true,
                                keyboardActions = KeyboardActions(onDone = {
                                    searchExpanded = false
                                    keyboardController?.hide()
                                })
                            )

                            ExposedDropdownMenu(
                                expanded = searchExpanded && (filteredContacts.isNotEmpty() || (searchQuery.isNotEmpty() && filteredContacts.isEmpty()) || allContactsEmpty),
                                onDismissRequest = { searchExpanded = false }
                            ) {
                                if (filteredContacts.isNotEmpty()) {
                                    filteredContacts.forEach { contact ->
                                        DropdownMenuItem(
                                            text = { Text("${contact.name} ${contact.surname}") },
                                            onClick = {
                                                onContactSelected(contact)
                                                searchExpanded = false
                                                keyboardController?.hide()
                                            }
                                        )
                                    }
                                } else if (searchQuery.isNotEmpty() && !allContactsEmpty) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.chat_no_results)) },
                                        onClick = { searchExpanded = false },
                                        enabled = false
                                    )
                                } else if (allContactsEmpty) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.chat_no_contacts_available)) },
                                        onClick = { searchExpanded = false },
                                        enabled = false
                                    )
                                }
                            }
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
                        value = messageText,
                        onValueChange = onMessageTextChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text(stringResource(R.string.chat_message_input_placeholder)) },
                        isError = messageInputError != null,
                        supportingText = { if (messageInputError != null) Text(messageInputError) },
                        enabled = chosenContact != null
                    )
                    IconButton(
                        onClick = onPickImage,
                        enabled = chosenContact != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = stringResource(R.string.chat_pick_image_description)
                        )
                    }
                    Button(
                        onClick = onSendMessage,
                        enabled = chosenContact != null && messageText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = stringResource(R.string.chat_send_message_button_description)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (chosenContact == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (allContactsEmpty) stringResource(R.string.chat_no_contacts_available)
                    else stringResource(R.string.chat_select_contact_prompt),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                items(messages) { msg ->
                    val isUserMessage = msg.senderId == currentUser.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            modifier = Modifier
                                .widthIn(max = 320.dp)
                                .padding(
                                    start = if (isUserMessage) 48.dp else 0.dp,
                                    end = if (isUserMessage) 0.dp else 48.dp
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.imageUrl != null && msg.content.isBlank()) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else if (isUserMessage) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                }
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth()
                            ) {
                                var contentDisplayed = false
                                if (msg.imageUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(msg.imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = if (msg.content.isNotBlank()) stringResource(R.string.chat_message_with_image_description) else stringResource(R.string.chat_image_message_description),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 250.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable { zoomedImageUrl = msg.imageUrl }
                                    )
                                    if (msg.content.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    contentDisplayed = true
                                }

                                if (msg.content.isNotBlank()) {
                                    Text(
                                        text = msg.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isUserMessage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    contentDisplayed = true
                                }

                                if (contentDisplayed) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = formatTimestamp(msg.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (zoomedImageUrl != null) {
            Dialog(onDismissRequest = { zoomedImageUrl = null }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(16.dp)
                        .background(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.shapes.medium
                        )
                ) {
                    AsyncImage(
                        model = zoomedImageUrl,
                        contentDescription = stringResource(R.string.chat_image_message_description),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    )
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        val url = URL(zoomedImageUrl)
                                        val connection = url.openConnection()
                                        connection.connect()
                                        val input = connection.getInputStream()
                                        val file = File(
                                            context.getExternalFilesDir(null),
                                            "downloaded_image_${System.currentTimeMillis()}.jpg"
                                        )
                                        FileOutputStream(file).use { output ->
                                            input.copyTo(output)
                                        }
                                        input.close()
                                    }
                                    Toast.makeText(context, context.getString(R.string.chat_image_message_download) + " " + "saved.", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                                zoomedImageUrl = null
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.chat_image_message_download))
                    }
                }
            }
        }
    }
}

@Composable
fun ImagePreviewDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onSend: (messageText: String) -> Unit
) {
    var messageTextState by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.95f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.chat_image_preview_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.chat_image_preview_close_button_description)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.chat_image_preview_content_description),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = messageTextState,
                    onValueChange = { messageTextState = it },
                    label = { Text(stringResource(R.string.chat_image_preview_message_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onSend(messageTextState) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.chat_image_preview_send_button))
                }
            }
        }
    }
}
