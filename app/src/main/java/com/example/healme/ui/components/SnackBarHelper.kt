package com.example.healme.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.healme.ui.theme.*
import kotlinx.coroutines.delay

/**
 * A composable that provides a standardized SnackBar experience.
 * It handles the display of the snackbar with appropriate styling based on type.
 *
 * @param snackbarHostState The SnackbarHostState to control snackbar visibility
 * @param message Message to display in the snackbar (null means no snackbar)
 * @param isError Whether this is an error message (affects styling)
 */
@Composable
fun SnackBarEffect(
    snackbarHostState: SnackbarHostState,
    message: String?,
    isError: Boolean = false
) {
    SnackbarHost(hostState = snackbarHostState) { data ->
        Snackbar(
            snackbarData = data,
            modifier = Modifier.padding(16.dp),
            containerColor = if (data.visuals.actionLabel == "Error") Crimson else DarkTurquoise,
            contentColor = White
        )
    }

    message?.let {
        LaunchedEffect(message, isError) {
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (isError) "Error" else "Info"
            )
        }
    }
}

/**
 * Preview function for the SnackBarEffect composable.
 * This function is used to display a preview of the SnackBarEffect composable in the Android Studio preview.
 *
 * It shows both an info and an error message in the snackbar with 3 seconds delay.
 */
@Preview(showBackground = true)
@Composable
fun SnackBarEffectPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    var message by remember { mutableStateOf<String?>("This is an info message") }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(3000)
            isError = !isError
            message = if (isError) "This is an error message" else "This is an info message"
        }
    }

    MaterialTheme {
        Scaffold(
            snackbarHost = {
                SnackBarEffect(snackbarHostState, message, isError)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {

            }
        }
    }
}