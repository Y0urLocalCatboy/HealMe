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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.healme.R
import com.example.healme.data.models.user.Doctor
import com.example.healme.data.models.user.Patient
import com.example.healme.data.models.user.User
import com.example.healme.viewmodel.AdminViewModel
import kotlin.text.compareTo
import kotlin.text.get

@Composable
fun AdminHomeScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel = viewModel()
) {
    val users by adminViewModel.users.collectAsState()
    var selectedIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        adminViewModel.loadAllUsers()
    }

    val roles = users.map { user ->
        when(user) {
            is Doctor -> stringResource(R.string.admin_panel_doctor)
            is Patient -> stringResource(R.string.admin_panel_patient)
            else -> stringResource(R.string.admin_panel_admin)
        }
    }

    LaunchedEffect(users.size) {
        if (selectedIndex >= users.size && users.isNotEmpty()) {
            selectedIndex = 0
        }
    }

    val onEditUser: (String) -> Unit = { userId ->
        navController.navigate("edit_user/$userId")
    }

    AdminHomeContent(
        users = users,
        roles = roles,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = { selectedIndex = it },
        specificRole = if (users.isNotEmpty() && selectedIndex < roles.size)
            roles[selectedIndex] else "",
        onEditUser = onEditUser
    )
}

@Composable
fun AdminHomeContent(
    users: List<User>,
    roles: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    specificRole: String,
    onEditUser: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.admin_panel_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (users.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(id = R.string.admin_panel_loading))
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
                text = stringResource(R.string.admin_panel_all_users),
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

@Composable
fun UserDetailsCard(
    specificRole: String,
    user: User,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.light_blue).copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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

                IconButton(
                    onClick = onEditClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "",
                        tint = colorResource(id = R.color.green)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.email) + ": ${user.email}")
                Text(stringResource(R.string.admin_panel_birthdate) + ": ${user.dateOfBirth}")
                Text(specificRole)
                if (user is Doctor) {
                    Text("Specialization: ${user.specialization.takeIf { it != "placeholder" } ?: "Not specified"}")
                }
                else {
                    Text("")
                }
            }
        }
    }
}

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