package com.amar.syncchat.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.amar.syncchat.data.model.User
import com.amar.syncchat.ui.contacts.ContactsScreen
import com.amar.syncchat.ui.navigation.BottomNavItem
import com.amar.syncchat.ui.profile.ProfileScreen

@Composable
fun MainScreen(
    onContactClick: (User) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Chats,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Chats.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Chats.route) {
                ContactsScreen(
                    onContactClick = onContactClick,
                    onProfileClick = { /* Handled via Bottom Nav */ }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onBackClick = { /* Handled via Bottom Nav */ },
                    onLogout = onLogout
                )
            }
        }
    }
}
