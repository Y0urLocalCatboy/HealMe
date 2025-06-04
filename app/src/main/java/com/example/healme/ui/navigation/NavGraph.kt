package com.example.healme.ui.navigation

import DoctorPrescriptionsScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.healme.ui.screens.patient.BookingConfirmationScreen
import com.example.healme.ui.components.menu.ConditionalDrawer
import com.example.healme.ui.screens.patient.PatientCalendarScreen
import com.example.healme.ui.screens.admin.AdminHomeScreen
import com.example.healme.ui.screens.doctor.CalendarScreen
import com.example.healme.ui.screens.doctor.DoctorHomeScreen
import com.example.healme.ui.screens.doctor.DoctorPatientsScreen
import com.example.healme.ui.screens.mutual.ChangeUserScreen
import com.example.healme.ui.screens.mutual.ChatScreen
import com.example.healme.ui.screens.mutual.LoginScreen
import com.example.healme.ui.screens.mutual.RegisterScreen
import com.example.healme.ui.screens.patient.AppointmentScreen
import com.example.healme.ui.screens.patient.PatientHomeScreen
import com.example.healme.ui.screens.patient.PatientPrescriptionsScreen


import com.example.healme.ui.screens.patient.PatientMedicalHistoryScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    val drawerEnabledRoutes = listOf("patient", "chat", "change_user", "calendar", "available_dates", "medical_history", "patient_prescription", "appointment")
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

            composable("doctor") {
                DoctorHomeScreen(navController)
            }

            composable("chat") {
                ChatScreen(navController)
            }

            composable("change_user") {
                ChangeUserScreen(navController)
            }

            composable("doctor_change_user") {
                ChangeUserScreen(navController)
            }

            composable("doctor_chat") {
                ChatScreen(navController)
            }

            composable("doctor_patients") {
                DoctorPatientsScreen(navController)
            }

            composable("doctor_schedule/{doctorId}") { backStackEntry ->
                val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
                CalendarScreen(
                    doctorId = doctorId,
                    onExit = { navController.popBackStack() }
                )
            }

            composable("doctor_prescription") {
                DoctorPrescriptionsScreen(navController)
            }

            composable("patient_prescription") {
                PatientPrescriptionsScreen(navController)
            }

            composable("admin") {
                AdminHomeScreen(navController)
            }

            composable("admin_change_user?userId={userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                ChangeUserScreen(navController, userId = userId.toString())
            }

            composable("calendar") {
                PatientCalendarScreen(navController)
            }

            composable("available_dates") {
                AppointmentScreen(navController)
            }




            composable("confirmation/{doctorName}/{doctorSurname}/{timestamp}") { backStackEntry ->
                val doctorName = backStackEntry.arguments?.getString("doctorName") ?: ""
                val doctorSurname = backStackEntry.arguments?.getString("doctorSurname") ?: ""
                val timestamp = backStackEntry.arguments?.getString("timestamp")?.toLongOrNull() ?: 0L

                BookingConfirmationScreen(
                    doctorName = doctorName,
                    doctorSurname = doctorSurname,
                    timestamp = timestamp,
                    navController = navController
                )
            }

            composable("medical_history") {
                PatientMedicalHistoryScreen(navController)
            }

        }
    }
}