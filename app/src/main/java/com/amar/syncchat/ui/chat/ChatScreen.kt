package com.amar.syncchat.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amar.syncchat.data.model.Message
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: String,
    userName: String,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.uiState.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(userId) {
        viewModel.initChat(userId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(userName, style = MaterialTheme.typography.titleMedium)
                        if (isTyping) {
                            Text("typing...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            ChatInput(
                messageText = messageText,
                onMessageChange = { 
                    messageText = it
                    viewModel.sendTyping(userId, it.isNotEmpty())
                },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(userId, messageText)
                        messageText = ""
                        viewModel.sendTyping(userId, false)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                reverseLayout = true
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message, 
                        isCurrentUser = message.senderId != userId
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    val horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = horizontalAlignment) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 0.dp,
                bottomEnd = if (isCurrentUser) 0.dp else 16.dp
            ),
            tonalElevation = 2.dp,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(text = message.message ?: "", color = textColor, fontSize = 16.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = formatTime(message.timestamp),
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                      //  val statusIcon = if (message.status == "seen") Icons.Filled.DoneAll else Icons.Filled.Done
                        val statusIcon = if (message.status == "seen") Icons.Filled.Done else Icons.Filled.Done
                        val iconColor = if (message.status == "seen") Color(0xFF00E5FF) else textColor.copy(alpha = 0.5f)
                        Icon(statusIcon, contentDescription = null, modifier = Modifier.size(14.dp), tint = iconColor)
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

@Composable
fun ChatInput(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(tonalElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = onSendClick,
                shape = androidx.compose.foundation.shape.CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
