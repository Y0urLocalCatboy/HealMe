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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.healme.R
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
                        label = { Text(stringResource(R.string.patient_panel_home)) },
                        selected = navController.currentDestination?.route == "patient",
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