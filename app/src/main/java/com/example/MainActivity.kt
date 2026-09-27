package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ServersScreen
import com.example.ui.screens.SpeedTestScreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SpeedTestViewModel

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    SPEED_TEST("Test", Icons.Filled.Speed, Icons.Outlined.Speed, "tab_speed_test"),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History, "tab_history"),
    SERVERS("Servers", Icons.Filled.Dns, Icons.Outlined.Dns, "tab_servers")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: SpeedTestViewModel = viewModel()
                var currentTab by remember { mutableStateOf(MainTab.SPEED_TEST) }

                BackHandler(enabled = currentTab != MainTab.SPEED_TEST) {
                    currentTab = MainTab.SPEED_TEST
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("main_navigation_bar")
                        ) {
                            MainTab.values().forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = CyanNeon.copy(alpha = 0.2f),
                                        selectedIconColor = CyanNeon,
                                        selectedTextColor = CyanNeon,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            MainTab.SPEED_TEST -> SpeedTestScreen(viewModel = viewModel)
                            MainTab.HISTORY -> HistoryScreen(
                                viewModel = viewModel,
                                onNavigateToTest = { currentTab = MainTab.SPEED_TEST }
                            )
                            MainTab.SERVERS -> ServersScreen(
                                viewModel = viewModel,
                                onNavigateToTest = { currentTab = MainTab.SPEED_TEST }
                            )
                        }
                    }
                }
            }
        }
    }
}
