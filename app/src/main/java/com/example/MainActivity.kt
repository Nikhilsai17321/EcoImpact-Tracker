package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EcoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: EcoViewModel = viewModel()
                val currentUser by viewModel.currentUser.collectAsState()

                if (currentUser == null) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        LoginRegisterScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                } else {
                    var currentTab by remember { mutableStateOf("Dashboard") }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentTab == "Dashboard",
                                    onClick = { currentTab = "Dashboard" },
                                    label = { Text("Dashboard") },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Dashboard,
                                            contentDescription = "Dashboard"
                                        )
                                    }
                                )
                                NavigationBarItem(
                                    selected = currentTab == "Log Activity",
                                    onClick = { currentTab = "Log Activity" },
                                    label = { Text("Log") },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Spa,
                                            contentDescription = "Log Activity"
                                        )
                                    }
                                )
                                NavigationBarItem(
                                    selected = currentTab == "History",
                                    onClick = { currentTab = "History" },
                                    label = { Text("History") },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "History"
                                        )
                                    }
                                )
                                NavigationBarItem(
                                    selected = currentTab == "Leaderboard",
                                    onClick = { currentTab = "Leaderboard" },
                                    label = { Text("Leaderboard") },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = "Leaderboard"
                                        )
                                    }
                                )
                                NavigationBarItem(
                                    selected = currentTab == "Profile",
                                    onClick = { currentTab = "Profile" },
                                    label = { Text("Profile") },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Profile"
                                        )
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentTab) {
                                "Dashboard" -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToLog = { currentTab = "Log Activity" }
                                )
                                "Log Activity" -> LogActivityScreen(
                                    viewModel = viewModel,
                                    onSuccess = { currentTab = "Dashboard" }
                                )
                                "History" -> HistoryScreen(viewModel = viewModel)
                                "Leaderboard" -> LeaderboardScreen(viewModel = viewModel)
                                "Profile" -> ProfileScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
