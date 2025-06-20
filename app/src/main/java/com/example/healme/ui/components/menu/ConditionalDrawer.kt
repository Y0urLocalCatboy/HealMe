package com.example.healme.ui.components.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.healme.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * ConditionalDrawer is a composable function that conditionally displays a navigation drawer
 * based on the current route. It uses Jetpack Compose's Material3 library to create a responsive
 * UI with a top app bar and a navigation drawer.
 *
 * @param showDrawer A boolean indicating whether to show the drawer or not.
 * @param navController The NavHostController used for navigation.
 * @param content The main content of the screen.
 */
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
                        label = { Text(stringResource(R.string.patient_panel_home)) },
                        selected = false,
                        onClick = {
                            navController.navigate("patient") {
                                popUpTo("patient") { inclusive = true }
                            }
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Home,
                            contentDescription = stringResource(R.string.patient_panel_home)) }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.patient_panel_profile)) },
                        selected = navController.currentDestination?.route == "change_user",
                        onClick = {
                            navController.navigate("change_user")
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Person,
                            contentDescription = stringResource(R.string.patient_panel_profile)) }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.patient_panel_chat)) },
                        selected = false,
                        onClick = {
                            navController.navigate("chat")
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Chat, contentDescription = stringResource(R.string.patient_panel_chat)) }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.admin_panel_logout)) },
                        selected = false,
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("login") {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                            }
                            scope.launch { drawerState.close() }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = stringResource(R.string.admin_panel_logout)
                            )
                        }
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
                                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.patient_panel_menu))
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
    } else
        Scaffold { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                content()
        }
    }
}