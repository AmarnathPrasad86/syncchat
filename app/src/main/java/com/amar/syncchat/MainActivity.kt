package com.amar.syncchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amar.syncchat.ui.auth.AuthViewModel
import com.amar.syncchat.ui.auth.LoginScreen
import com.amar.syncchat.ui.auth.RegisterScreen
import com.amar.syncchat.ui.chat.ChatScreen
import com.amar.syncchat.ui.main.MainScreen
import com.amar.syncchat.ui.navigation.Screen
import com.amar.syncchat.ui.theme.SyncChatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SyncChatTheme {
                SyncChatNavHost()
            }
        }
    }
}

@Composable
fun SyncChatNavHost(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    if (isLoggedIn == null) return

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn == true) "main" else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate("main") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScreen(
                onContactClick = { contact ->
                    navController.navigate(Screen.Chat.createRoute(contact.id ?: "", contact.name ?: "Unknown"))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            ChatScreen(
                userId = userId,
                userName = userName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
