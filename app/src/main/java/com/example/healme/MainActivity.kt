package com.example.healme

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.healme.ui.navigation.NavGraph
import com.example.healme.ui.theme.HealMeTheme
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.instacart.library.truetime.TrueTime

class MainActivity : ComponentActivity() {

    private lateinit var appTrueTime: TrueTime

    /**
        * Activity Result Launcher for requesting notification permission.
        * This is used to handle the permission request result
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permissions", "Notification permission granted")
        } else {
            Log.d("Permissions", "Notification permission denied")
        }
    }

    /**
     * onCreate method is called when the activity is created.
     * It initializes TrueTime and sets the content view to the main navigation graph.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        setContent {
            Thread {
                try {
                    TrueTime.build()
                        .withNtpHost("pool.ntp.org")
                        .withConnectionTimeout(31_428)
                        .initialize()
                } catch (e: Exception) {
                    Log.e("TrueTime", "Failed to initialize TrueTime", e)
                }
            }.start()
            val navController = rememberNavController()
            HealMeTheme {NavGraph(navController)}

        }
    }

    /**
     * This function checks if the app has permission to post notifications.
     * If not it requests the permission from the user.
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
