package com.example.healme.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.healme.ui.components.menu.ConditionalDrawer
import com.example.healme.ui.screens.mutual.LoginScreen
import com.example.healme.ui.screens.mutual.RegisterScreen
import com.example.healme.ui.screens.patient.PatientHomeScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    // Keep track of current destination
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    // Screens where drawer should be visible
    val drawerEnabledRoutes = listOf("home", "patient")
    val showDrawer = currentDestination in drawerEnabledRoutes

    ConditionalDrawer(
        showDrawer = showDrawer,
        navController = navController
    ) {
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = modifier
        ) {
            composable("login") {
                LoginScreen(navController)
            }

            composable("register") {
                RegisterScreen(navController)
            }

            composable("patient") {
                PatientHomeScreen(navController)
            }

            composable("profile") {
                PatientHomeScreen(navController)
            }
        }
    }
}
