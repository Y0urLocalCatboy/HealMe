package com.example.healme.ui.components.popups

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

/**
 * Displays a toast message.
 *
 * @param message The message to be displayed in the toast.
 */
@Composable
fun showToast(message: String) {
    val context = LocalContext.current
    LaunchedEffect(message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

/**
 * Preview function for the showToast composable.
 * This function is used to display a preview of the showToast composable in the Android Studio preview.
 */
@Preview(showBackground = true)
@Composable
fun ToastHelperPreview() {
    showToast("This is a preview toast message")
}