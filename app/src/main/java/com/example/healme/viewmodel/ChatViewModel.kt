package com.example.healme.viewmodel

import androidx.compose.runtime.Composable
import com.example.healme.R
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
            message.length < minLength -> stringResource(R.string.message_short)
            message.length > maxLength -> stringResource(R.string.message_long)
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
            val date = Date(timestamp.toLong())
            SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            timestamp
        }
    }
}