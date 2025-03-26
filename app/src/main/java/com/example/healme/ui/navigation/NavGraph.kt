package com.example.healme.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.healme.ui.screens.LoginScreen
import com.example.healme.ui.screens.RegisterScreen
import com.example.healme.ui.screens.PatientHomeScreen


@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("patient") { PatientHomeScreen(navController) }
    }
}