package com.example.healme.ui.screens.mutual

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.healme.data.models.user.User
import com.example.healme.data.network.FirestoreClass
import com.example.healme.ui.components.popups.SnackBarEffect
import com.example.healme.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChatScreen(navController: NavController,
               viewModel: ChatViewModel
) {
    val fs = FirestoreClass()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    if (currentUser == null) {
        SnackBarEffect(remember { SnackbarHostState() },
            "A critical error has occured - the user is not logged in",
            true)
    }

    var message by remember { mutableStateOf("") }
    val errorMessage = viewModel.messageValidity(message)
    var name = remember { mutableStateOf("") }
    var surname = remember { mutableStateOf("") }

    var doctors by remember { mutableStateOf(listOf<User>()) }


}

@Composable
fun ChatContent(

){

}

@Preview
@Composable
fun ChatScreenPreview() {
    ChatContent()
}