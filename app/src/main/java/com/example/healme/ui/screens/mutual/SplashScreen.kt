package com.example.healme.ui.screens.mutual

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.healme.viewmodel.AuthViewModel

@Composable
fun SplashScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }

    LaunchedEffect(key1 = true) {
        val destination = authViewModel.getStartDestination()
        navController.navigate(destination) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
        }
    }
}