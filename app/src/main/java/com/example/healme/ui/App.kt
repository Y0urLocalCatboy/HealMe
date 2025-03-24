package com.example.healme.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.healme.ui.navigation.NavGraph

@Composable
fun App() {
    val navController = rememberNavController()

    MaterialTheme {
        NavGraph(navController = navController)
    }
}
