package com.example.healme.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.healme.MainActivity
import com.example.healme.R
import com.example.healme.data.network.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when a new token is generated for the device.
     * This method updates the FCM token in the Firestore database based on the user type.
     *
     * @param token The new FCM token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM token generated: $token")

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid

            determineUserTypeAndUpdateToken(userId, token)
        } else {
            Log.d("FCM", "No authenticated user to update token for.")
        }
    }

    /**
     * Determines the user type (patient, doctor, or admin) and updates the FCM token in the corresponding Firestore collection.
     *
     * @param userId The ID of the authenticated user.
     * @param token The FCM token to be updated.
     */
    private fun determineUserTypeAndUpdateToken(userId: String, token: String) {
        FirebaseFirestore.getInstance().collection("patients").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    updateTokenInCollection("patients", userId, token)
                } else {
                    FirebaseFirestore.getInstance().collection("doctors").document(userId).get()
                        .addOnSuccessListener { docDocument ->
                            if (docDocument.exists()) {
                                updateTokenInCollection("doctors", userId, token)
                            } else {
                                FirebaseFirestore.getInstance().collection("admins").document(userId).get()
                                    .addOnSuccessListener { adminDocument ->
                                        if (adminDocument.exists()) {
                                            updateTokenInCollection("admins", userId, token)
                                        } else {
                                            Log.w("FCM", "User not found in any collection. Token not saved.")
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FCM", "Error checking admin collection: ${e.message}", e)
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FCM", "Error checking doctor collection: ${e.message}", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error checking patient collection: ${e.message}", e)
            }
    }

    /**
     * Updates the FCM token in the specified Firestore collection for the given user ID.
     *
     * @param collection The Firestore collection to update (patients, doctors, or admins).
     * @param userId The ID of the user whose token is being updated.
     * @param token The new FCM token to be saved.
     */
    private fun updateTokenInCollection(collection: String, userId: String, token: String) {
        FirebaseFirestore.getInstance()
            .collection(collection)
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.i("FCM", "Token updated in Firestore for $userId in $collection")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Failed to update token: ${e.message}", e)
            }
    }

    /**
     * Called when a message is received from Firebase Cloud Messaging.
     * This method handles the notification payload and displays a notification to the user.
     *
     * @param remoteMessage The message received from FCM.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "📩 Message received!")

        val notification = remoteMessage.notification
        val title = notification?.title ?: "No Title"
        val body = notification?.body ?: "No Message"

        Log.d("FCM", "Notification payload: title=$title, body=$body")

        if (notification != null) {
            showNotification(title, body)
        } else if (remoteMessage.data.isNotEmpty()) {
            val dataTitle = remoteMessage.data["title"] ?: "No Title"
            val dataBody = remoteMessage.data["body"] ?: "No Message"
            Log.d("FCM", "Data payload received: title=$dataTitle, body=$dataBody")
            showNotification(dataTitle, dataBody)
        } else {
            Log.w("FCM", "No notification or data payload found!")
        }
    }

    private fun showNotification(title: String, message: String) {
        Log.d("FCM", "Attempting to show notification: $title - $message")

        val channelId = "default_channel_id"
        val channelName = "Default Channel"

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
            Log.d("FCM", "Notification channel created (API 26+)")
        }

        try {
            manager.notify(0, builder.build())
            Log.d("FCM", "Notification posted successfully")
        } catch (e: Exception) {
            Log.e("FCM", "Failed to show notification: ${e.message}", e)
        }
    }
}
