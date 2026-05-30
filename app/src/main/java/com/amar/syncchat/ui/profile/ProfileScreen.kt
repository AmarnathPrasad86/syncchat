package com.amar.syncchat.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amar.syncchat.data.model.User
import com.amar.syncchat.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.getProfile()
        }
    }

    LaunchedEffect(profileState) {
        if (profileState !is Resource.Loading) {
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            when (profileState) {
                is Resource.Loading -> {
                    if (!pullToRefreshState.isRefreshing) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is Resource.Success -> {
                    val user = (profileState as Resource.Success<User>).data
                    if (user != null) {
                        ProfileContent(user, onLogout)
                    }
                }
                is Resource.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = profileState.message ?: "Error", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun ProfileContent(user: User, onLogout: () -> Unit) {
    val scrollState = rememberScrollState()
    val name = user.name ?: "Unknown"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image Section with Gradient
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (name.isNotEmpty()) name.take(1).uppercase() else "?",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            text = if (user.online) "Online" else "Offline",
            color = if (user.online) Color(0xFF4CAF50) else Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Info Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoItem(icon = Icons.Default.Person, label = "Full Name", value = name)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                ProfileInfoItem(icon = Icons.Default.Email, label = "Email Address", value = user.email ?: "Not provided")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                ProfileInfoItem(icon = Icons.Default.Phone, label = "Mobile Number", value = user.mobile ?: "Not provided")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
