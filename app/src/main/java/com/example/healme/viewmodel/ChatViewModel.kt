package com.example.healme.viewmodel

import android.net.Uri
import androidx.compose.runtime.Composable
import com.example.healme.R
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import com.example.healme.data.models.Message
import com.example.healme.data.models.Message.MessageType
import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreClass
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.text.format

/**
 * ViewModel for handling chat-related logic.
 *
 * @function messageValidity Validates the message input.
 * @function formatTimestamp Formats a timestamp string into a human-readable date format.
 */
class ChatViewModel : ViewModel() {

    /**
     * Validates the message input.
     *
     * @param message The message to be validated.
     * @return A string indicating the validation result. Returns null if the message is valid.
     */
    @Composable
    fun messageValidity(message: String) : String? {
        val minLength = 1
        val maxLength = 200
        return when{
            message.length < minLength -> stringResource(R.string.chat_message_short)
            message.length > maxLength -> stringResource(R.string.chat_message_long)
            else -> return null
        }
    }

    /**
     * Formats a timestamp string into a human-readable date format.
     *
     * @param timestamp The timestamp string to be formatted.
     * @return A formatted date string.
     */
    fun formatTimestamp(timestamp: String, yesterday: String, today: String): String {
        return try {
            val millis = timestamp.toLong()
            val date = Date(millis)
            val timeFormat = SimpleDateFormat("HH:mm", Locale("pl"))
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("pl"))

            val now = Calendar.getInstance()
            val messageTime = Calendar.getInstance().apply { timeInMillis = millis }

            when {
                now.get(Calendar.DATE) == messageTime.get(Calendar.DATE) ->
                    "${today} ${timeFormat.format(date)}"

                now.get(Calendar.DATE) - messageTime.get(Calendar.DATE) == 1 ->
                    "${yesterday} ${timeFormat.format(date)}"
                else -> "${dateFormat.format(date)} ${timeFormat.format(date)}"
            }
        } catch (e: Exception) {
            "Date Error"
        }
    }

    /**
     * Uploads an image and sends it as a message.
     *
     * @param uri The URI of the image to be uploaded.
     * @param currentUser The current Firebase user.
     * @param chosenContact The contact to whom the message is sent.
     * @param fs An instance of FirestoreClass for database operations.
     * @param onError Callback function to handle errors during the upload or message saving process.
     */
    fun uploadAndSendImage(
        uri: Uri,
        currentUser: FirebaseUser?,
        chosenContact: User?,
        fs: FirestoreClass,
        onError: (String) -> Unit
    ) {
        fs.uploadImage(
            uri,
            onSuccess = { imageUrl ->
                fs.saveMessage(
                    Message(
                        content = "",
                        senderId = currentUser?.uid ?: "",
                        receiverId = chosenContact?.id ?: "",
                        timestamp = System.currentTimeMillis().toString(),
                        imageUrl = imageUrl,
                        type = MessageType.IMAGE
                    ),
                    onResult = { success, errorMsg ->
                        if (!success) {
                            onError(errorMsg)
                        }
                    }
                )
            },
            onFailure = { exception ->
                onError(exception.message ?: "Image error")
            }
        )
    }
}