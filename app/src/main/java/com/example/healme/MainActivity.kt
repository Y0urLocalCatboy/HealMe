package com.example.healme

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.healme.ui.navigation.NavGraph
import com.example.healme.ui.theme.HealMeTheme
import android.app.Application
import com.instacart.library.truetime.TrueTime

class MainActivity : ComponentActivity() {

    private lateinit var appTrueTime: TrueTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}
