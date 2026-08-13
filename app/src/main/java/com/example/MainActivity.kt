package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.SecurityViewModel
import com.example.ui.screens.*
import com.example.ui.theme.ShieldAITheme
import com.example.ui.theme.ShieldCyanPrimary
import com.example.ui.theme.ShieldDarkBg
import com.example.ui.theme.ShieldSurface

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("dashboard", "Home", Icons.Default.Shield)
    object Scanners : BottomNavItem("url_scan", "Scanners", Icons.Default.Radar)
    object Ai : BottomNavItem("ai_assistant", "AI Assistant", Icons.Default.SmartToy)
    object Notifications : BottomNavItem("notifications", "Alerts", Icons.Default.Notifications)
    object Settings : BottomNavItem("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val securityViewModel: SecurityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ShieldAITheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomItems = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Scanners,
                    BottomNavItem.Ai,
                    BottomNavItem.Notifications,
                    BottomNavItem.Settings
                )

                val showBottomBar = currentRoute in listOf(
                    "dashboard", "url_scan", "ai_assistant", "notifications", "settings"
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = ShieldDarkBg,
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = ShieldSurface,
                                contentColor = ShieldCyanPrimary
                            ) {
                                bottomItems.forEach { item ->
                                    NavigationBarItem(
                                        selected = currentRoute == item.route,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(item.icon, contentDescription = item.title) },
                                        label = { Text(item.title) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                            selectedTextColor = ShieldCyanPrimary,
                                            indicatorColor = ShieldCyanPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "onboarding",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("onboarding") {
                            OnboardingScreen(viewModel = securityViewModel) {
                                navController.navigate("auth") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        }
                        composable("auth") {
                            AuthScreen(viewModel = securityViewModel) {
                                navController.navigate("dashboard") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        }
                        composable("dashboard") {
                            DashboardScreen(viewModel = securityViewModel) { route ->
                                navController.navigate(route)
                            }
                        }
                        composable("app_audit") {
                            InstalledAppAuditScreen(viewModel = securityViewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("permission_audit") {
                            PermissionAuditorScreen(viewModel = securityViewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("url_scan") {
                            UrlScannerScreen(viewModel = securityViewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("phone_spam") {
                            PhoneSpamCheckerScreen(viewModel = securityViewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("sms_security") {
                            SmsSecurityScreen(viewModel = securityViewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("network_security") {
                            NetworkSecurityScreen(viewModel = securityViewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("email_security") {
                            EmailSecurityScreen(viewModel = securityViewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("quarantine") {
                            QuarantineScreen(viewModel = securityViewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("ai_assistant") {
                            AiAssistantScreen(viewModel = securityViewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("privacy") {
                            PrivacyCenterScreen(viewModel = securityViewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("notifications") {
                            NotificationCenterScreen(viewModel = securityViewModel) {
                                navController.popBackStack()
                            }
                        }
                        composable("settings") {
                            SettingsScreen(viewModel = securityViewModel) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}
