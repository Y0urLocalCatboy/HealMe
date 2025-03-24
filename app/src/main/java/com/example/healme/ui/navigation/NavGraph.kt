package com.example.healme.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.healme.ui.screens.RegisterScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "register") {
        composable("register") { RegisterScreen(navController) }
        //composable("login") { LoginScreen(navController) }
    }
}
