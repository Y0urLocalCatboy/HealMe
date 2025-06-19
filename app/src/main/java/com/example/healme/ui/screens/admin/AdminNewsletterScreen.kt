package com.example.healme.ui.screens.admin

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.healme.R
import com.google.firebase.firestore.FirebaseFirestore
import com.instacart.library.truetime.TrueTimeRx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun AdminNewsletterScreen(navController: NavHostController) {
    var message by remember { mutableStateOf("") }
    var sendToPatients by remember { mutableStateOf(true) }
    var sendToDoctors by remember { mutableStateOf(false) }
    var timeStr by remember { mutableStateOf("") }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var infoArg by remember { mutableStateOf<String?>(null) }
    var messageType by remember { mutableStateOf("none") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back to Admin"
                )
            }
        }

        Text(
            text = stringResource(R.string.newsletter_title),
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(stringResource(R.string.newsletter_message)) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = sendToPatients, onCheckedChange = { sendToPatients = it })
            Text(stringResource(R.string.newsletter_patients))
            Spacer(modifier = Modifier.width(16.dp))
            Checkbox(checked = sendToDoctors, onCheckedChange = { sendToDoctors = it })
            Text(stringResource(R.string.newsletter_doctors))
        }

        OutlinedTextField(
            value = timeStr,
            onValueChange = { timeStr = it },
            label = { Text(stringResource(R.string.newsletter_time_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val parts = timeStr.split(":")
                if (parts.size != 2) {
                    messageType = "error"
                    infoMessage = "invalid_time"
                    return@Button
                }

                val hour = parts[0].toIntOrNull()
                val minute = parts[1].toIntOrNull()
                if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
                    messageType = "error"
                    infoMessage = "invalid_time"
                    return@Button
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val nowUtc = TrueTimeRx.now()

                        val utcMillis = nowUtc.time

                        val localOffsetMillis = TimeZone.getDefault().getOffset(utcMillis).toLong()

                        val localTimeMillis = utcMillis + localOffsetMillis
                        val utcUnixTimestamp = localTimeMillis / 1000

                        val targets = listOfNotNull(
                            "patients".takeIf { sendToPatients },
                            "doctors".takeIf { sendToDoctors }
                        )

                        if (message.isBlank() || targets.isEmpty()) {
                            messageType = "error"
                            infoMessage = "fill_all"
                            return@launch
                        }

                        val data = hashMapOf(
                            "message" to message,
                            "timestamp" to utcUnixTimestamp,
                            "targetRoles" to targets
                        )

                        FirebaseFirestore.getInstance()
                            .collection("newsletters")
                            .add(data)
                            .addOnSuccessListener {
                                messageType = "success"
                                infoMessage = "success"
                                infoArg = timeStr
                            }
                            .addOnFailureListener {
                                messageType = "error"
                                infoMessage = "generic"
                                infoArg = it.message ?: ""
                            }
                    } catch (e: Exception) {
                        Log.e("Newsletter", "TrueTime error: ${e.message}")
                        messageType = "error"
                        infoMessage = "generic"
                        infoArg = e.message ?: "Unknown"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(stringResource(R.string.newsletter_schedule))
        }


        infoMessage?.let {
            val text = when (it) {
                "invalid_time" -> stringResource(R.string.newsletter_error_time_format)
                "fill_all" -> stringResource(R.string.newsletter_error_fill_all)
                "success" -> stringResource(R.string.newsletter_success, infoArg ?: "")
                "generic" -> stringResource(R.string.newsletter_error_generic, infoArg ?: "")
                else -> ""
            }
            val color = if (messageType == "error") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            Text(text = text, color = color)
        }
    }
}

