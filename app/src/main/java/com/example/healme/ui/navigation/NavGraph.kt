package com.example.healme.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.healme.ui.components.menu.ConditionalDrawer
import com.example.healme.ui.screens.admin.AdminHomeScreen
import com.example.healme.ui.screens.doctor.DoctorHomeScreen
import com.example.healme.ui.screens.mutual.ChangeUserScreen
import com.example.healme.ui.screens.mutual.ChatScreen
import com.example.healme.ui.screens.mutual.LoginScreen
import com.example.healme.ui.screens.mutual.RegisterScreen
import com.example.healme.ui.screens.patient.PatientHomeScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    val drawerEnabledRoutes = listOf("patient", "chat", "change_user", "doctor")
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

            composable("chat") {
                ChatScreen()
            }

            composable("change_user") {
                ChangeUserScreen(navController)
            }

            composable("admin") {
                AdminHomeScreen(navController)
            }

            composable("doctor") {
                DoctorHomeScreen(navController)
            }

            composable("admin_change_user?userId={userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                ChangeUserScreen(navController, userId = userId.toString())
            }

        }
    }
}