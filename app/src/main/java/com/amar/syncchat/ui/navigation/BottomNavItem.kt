package com.amar.syncchat.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    data object Chats : BottomNavItem("chats", Icons.AutoMirrored.Filled.Message, "Chats")
    data object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}
