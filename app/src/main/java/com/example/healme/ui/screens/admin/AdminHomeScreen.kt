package com.example.healme.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.healme.R
import com.example.healme.data.models.user.Doctor
import com.example.healme.data.models.user.Patient
import com.example.healme.data.models.user.User
import com.example.healme.ui.components.menu.CalendarPicker
import com.example.healme.viewmodel.AdminViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.text.compareTo
import kotlin.text.get

/**
 * AdminHomeScreen is the main screen for the admin panel.
 *
 * @param navController The NavHostController used for navigation.
 * @param adminViewModel The ViewModel for managing admin-related data.
 */
@Composable
fun AdminHomeScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel = viewModel()
) {
    val users by adminViewModel.users.collectAsState()
    var selectedIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("") } // Pusty string oznacza "Wszyscy"

    val adminRole by rememberUpdatedState(newValue = stringResource(R.string.admin_panel_admin))
    val doctorRole by rememberUpdatedState(newValue = stringResource(R.string.admin_panel_doctor))
    val patientRole by rememberUpdatedState(newValue = stringResource(R.string.admin_panel_patient))
    val allRole by rememberUpdatedState(newValue = stringResource(R.string.admin_panel_all))

    // Filtrowanie użytkowników
    val filteredUsers = remember(users, searchQuery, selectedRoleFilter) {
        users.filter { user ->
            val matchesSearch = searchQuery.isEmpty() ||
                    "${user.name} ${user.surname}".contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true)

            val matchesRole = selectedRoleFilter.isEmpty() || when (selectedRoleFilter) {
                doctorRole -> user is Doctor
                patientRole -> user is Patient
                adminRole -> user !is Doctor && user !is Patient
                else -> true
            }

            matchesSearch && matchesRole
        }
    }

    LaunchedEffect(Unit) {
        adminViewModel.loadAllUsers()
    }

    LaunchedEffect(navController.currentBackStackEntryAsState()) {
        adminViewModel.loadAllUsers()
    }

    LaunchedEffect(filteredUsers.size) {
        if (selectedIndex >= filteredUsers.size && filteredUsers.isNotEmpty()) {
            selectedIndex = 0
        }
    }

    val onEditUser: (String) -> Unit = { userId ->
        navController.navigate("admin_change_user?userId=$userId")
    }

    val roles = remember(filteredUsers) {
        filteredUsers.map { user ->
            when(user) {
                is Doctor -> doctorRole
                is Patient -> patientRole
                else -> adminRole
            }
        }
    }

    val onLogOut: () -> Unit = {
        FirebaseAuth.getInstance().signOut()
        navController.navigate("login") {
            popUpTo("welcome") { inclusive = true }
        }
    }



    AdminHomeContent(
        users = filteredUsers,
        roles = roles,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = { selectedIndex = it },
        specificRole = if (filteredUsers.isNotEmpty() && selectedIndex < roles.size)
            roles[selectedIndex] else "",
        onEditUser = onEditUser,
        onLogOut = onLogOut,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        selectedRoleFilter = selectedRoleFilter,
        onRoleFilterChange = { selectedRoleFilter = it },
        doctorRole = doctorRole,
        patientRole = patientRole,
        adminRole = adminRole,
        allRole = allRole
    )
}

@Composable
fun AdminHomeContent(
    users: List<User>,
    roles: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    specificRole: String,
    onEditUser: (String) -> Unit,
    onLogOut: () -> Unit = {},
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedRoleFilter: String,
    onRoleFilterChange: (String) -> Unit,
    doctorRole: String,
    patientRole: String,
    adminRole: String,
    allRole: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onLogOut,
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Text(stringResource(R.string.admin_panel_logout))
            }

            Button(
                onClick = { navController.navigate("newsletter") },
                modifier = Modifier.padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.green))
            ) {
                Text(stringResource(R.string.admin_newsletter)
            }


            Text(
                text = stringResource(R.string.admin_panel_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text(stringResource(R.string.admin_panel_search)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filtry według roli
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = selectedRoleFilter == "",
                onClick = { onRoleFilterChange("") },
                label = { Text(allRole) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorResource(id = R.color.green)
                )
            )

            FilterChip(
                selected = selectedRoleFilter == doctorRole,
                onClick = { onRoleFilterChange(doctorRole) },
                label = { Text(doctorRole) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorResource(id = R.color.green)
                )
            )

            FilterChip(
                selected = selectedRoleFilter == patientRole,
                onClick = { onRoleFilterChange(patientRole) },
                label = { Text(patientRole) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorResource(id = R.color.green)
                )
            )

            FilterChip(
                selected = selectedRoleFilter == adminRole,
                onClick = { onRoleFilterChange(adminRole) },
                label = { Text(adminRole) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorResource(id = R.color.green)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (users.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (searchQuery.isEmpty() && selectedRoleFilter.isEmpty())
                        stringResource(id = R.string.admin_panel_loading)
                    else
                        stringResource(id = R.string.admin_panel_no_results)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (selectedIndex > 0) {
                            onSelectedIndexChange(selectedIndex - 1)
                        }
                    },
                    enabled = selectedIndex > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.green),
                        disabledContainerColor = colorResource(id = R.color.gray)
                    )
                ) {
                    Text(stringResource(R.string.admin_panel_previous))
                }

                Button(
                    onClick = {
                        if (selectedIndex < users.size - 1) {
                            onSelectedIndexChange(selectedIndex + 1)
                        }
                    },
                    enabled = selectedIndex < users.size - 1,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.green),
                        disabledContainerColor = colorResource(id = R.color.gray)
                    )
                ) {
                    Text(stringResource(R.string.admin_panel_next))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (users.isNotEmpty() && selectedIndex < users.size) {
                UserDetailsCard(
                    specificRole = specificRole,
                    user = users[selectedIndex],
                    onEditClick = { onEditUser(users[selectedIndex].id) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.admin_panel_all_users) + " (${users.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(users.indices.toList()) { index ->
                    UserListItem(
                        user = users[index],
                        role = roles[index],
                        isSelected = index == selectedIndex,
                        onClick = {
                            onSelectedIndexChange(index)
                        }
                    )
                }
            }
        }
    }
}

/**
 * UserDetailsCard displays detailed information about a user.
 *
 * @param specificRole The specific role of the user (e.g., Doctor, Patient).
 * @param user The User object containing user details.
 * @param onEditClick Callback function to handle edit action.
 */
@Composable
fun UserDetailsCard(
    specificRole: String,
    user: User,
    onEditClick: () -> Unit
) {
    var showAvailabilityDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.light_blue).copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${user.name} ${user.surname}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "",
                        tint = colorResource(id = R.color.green)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.register_email) + ": ${user.email}")
                Text(stringResource(R.string.admin_panel_birthdate) + ": ${user.dateOfBirth}")
                Text(specificRole)

                if (user is Doctor) {
                    Text("${stringResource(R.string.admin_panel_specialization)} ${user.specialization.takeIf { it != "placeholder" } ?: "Not specified"}")

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showAvailabilityDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.green))
                    ) {
                        Text("Change Availability")
                    }

                    if (showAvailabilityDialog) {
                        Dialog(onDismissRequest = { showAvailabilityDialog = false }) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                shape = RoundedCornerShape(12.dp),
                                tonalElevation = 6.dp
                            ) {
                                CalendarPicker(
                                    doctorId = user.id,
                                    onExit = {showAvailabilityDialog = false}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * UserListItem displays a single item in the user list.
 *
 * @param user The User object to display.
 * @param role The role of the user (e.g., Doctor, Patient).
 * @param isSelected Indicates if the item is selected.
 * @param onClick Callback function to handle item click.
 */
@Composable
fun UserListItem(
    user: User,
    role: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isSelected)
                    colorResource(id = R.color.green).copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surface
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${user.name} ${user.surname}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}