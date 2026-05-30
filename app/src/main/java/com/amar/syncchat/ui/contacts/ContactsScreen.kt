package com.amar.syncchat.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amar.syncchat.data.model.User
import com.amar.syncchat.util.Resource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onContactClick: (User) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val contactsState by viewModel.contactsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshContacts()
        }
    }

    LaunchedEffect(contactsState) {
        if (contactsState !is Resource.Loading) {
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = { Text("SyncChat", fontWeight = FontWeight.Bold) }
                )
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            when (contactsState) {
                is Resource.Loading -> {
                    if (!pullToRefreshState.isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                is Resource.Success -> {
                    val users = (contactsState as Resource.Success<List<User>>).data ?: emptyList()
                    if (users.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No chats found", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(users) { user ->
                                ContactItem(user = user, onClick = { onContactClick(user) })
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = contactsState.message ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
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
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search users...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = CircleShape,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun ContactItem(user: User, onClick: () -> Unit) {
    val name = user.name ?: "Unknown"
    val lastMsg = user.lastMessage ?: user.mobile ?: ""
    val time = user.lastMessageTime?.let { formatTime(it) } ?: ""

    ListItem(
        headlineContent = { 
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (time.isNotEmpty()) {
                    Text(time, fontSize = 11.sp, color = Color.Gray)
                }
            }
        },
        supportingContent = { 
            Text(lastMsg, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingContent = {
            Box {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (name.isNotEmpty()) name.take(1).uppercase() else "?", fontSize = 22.sp)
                    }
                }
                if (user.online) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF4CAF50)))
                    }
                }
            }
        },
        trailingContent = {
            if (user.unreadCount > 0) {
                Badge(containerColor = Color(0xFF25D366)) {
                    Text("${user.unreadCount}", color = Color.White)
                }
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}
