package com.example.healme.viewmodel

import androidx.compose.runtime.Composable
import com.example.healme.R
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
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
    fun formatTimestamp(timestamp: String): String {
        return try {
            val millis = timestamp.toLong()
            val date = Date(millis)
            val timeFormat = SimpleDateFormat("HH:mm", Locale("pl"))
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("pl"))

            val now = Calendar.getInstance()
            val messageTime = Calendar.getInstance().apply { timeInMillis = millis }

            when {
                now.get(Calendar.DATE) == messageTime.get(Calendar.DATE) ->
                    "Today ${timeFormat.format(date)}"

                now.get(Calendar.DATE) - messageTime.get(Calendar.DATE) == 1 ->
                    "Yesteday ${timeFormat.format(date)}"
                    //DODAĆ DODAĆ DODAĆ STRING RESOURCES DODAC DODAĆ TODO TODO TODO TODO
                else -> "${dateFormat.format(date)} ${timeFormat.format(date)}"
            }
        } catch (e: Exception) {
            "Date Error"
        }
    }
}