package com.example.healme.ui.components.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionalDrawer(
    showDrawer: Boolean,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("HealMe App", modifier = Modifier.padding(16.dp))
                    NavigationDrawerItem(
                        label = { Text("Home") },
                        selected = navController.currentDestination?.route == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
                    )
                    NavigationDrawerItem(
                        label = { Text("Change User Data") },
                        selected = navController.currentDestination?.route == "change_user",
                        onClick = {
                            navController.navigate("change_user")
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "change_user") }
                    )
                    NavigationDrawerItem(
                        label = { Text("Chat") },
                        selected = false,
                        onClick = {
                            navController.navigate("chat")
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("HealMe") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    content()
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("HealMe", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                content()
            }
        }
    }
}