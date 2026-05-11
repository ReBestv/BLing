package com.standbyus.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.standbyus.app.navigation.Routes
import com.standbyus.app.ui.album.AlbumScreen
import com.standbyus.app.ui.celebration.CelebrationConfig
import com.standbyus.app.ui.celebration.CelebrationDay
import com.standbyus.app.ui.celebration.CelebrationOverlay
import com.standbyus.app.ui.history.HistoryScreen
import com.standbyus.app.ui.home.HomeScreen
import com.standbyus.app.ui.poststatus.PostStatusScreen
import com.standbyus.app.ui.settings.SettingsScreen
import com.standbyus.app.ui.theme.StandByUsTheme
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val celebration = checkCelebrationDay()

        setContent {
            StandByUsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showCelebration by remember { mutableStateOf(celebration != null) }
                    val activeCelebration = remember { celebration }

                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val items = listOf(
                        BottomNavItem(Routes.HOME, Icons.Filled.Home, "首页"),
                        BottomNavItem(Routes.ALBUM, Icons.Filled.PhotoLibrary, "我们的故事"),
                        BottomNavItem(Routes.HISTORY, Icons.Filled.History, "历史"),
                        BottomNavItem(Routes.SETTINGS, Icons.Filled.Settings, "设置")
                    )

                    Scaffold(
                        bottomBar = {
                            if (currentRoute in items.map { it.route }) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ) {
                                    items.forEach { item ->
                                        val selected = navBackStackEntry?.destination?.hierarchy?.any {
                                            it.route == item.route
                                        } == true
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            icon = {
                                                Icon(
                                                    item.icon,
                                                    contentDescription = item.label,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            },
                                            label = { Text(item.label) }
                                        )
                                    }
                                }
                            }
                        }
                    ) { padding ->
                        NavHost(
                            navController = navController,
                            startDestination = Routes.HOME,
                            modifier = Modifier.fillMaxSize().padding(padding)
                        ) {
                            composable(Routes.HOME) {
                                HomeScreen(
                                    onNavigateToPost = { navController.navigate(Routes.POST_STATUS) },
                                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
                                )
                            }
                            composable(Routes.ALBUM) {
                                AlbumScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(Routes.HISTORY) {
                                HistoryScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(Routes.POST_STATUS) {
                                PostStatusScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(Routes.SETTINGS) {
                                SettingsScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }

                    if (showCelebration && activeCelebration != null) {
                        CelebrationOverlay(
                            celebration = activeCelebration,
                            onDismiss = { showCelebration = false }
                        )
                    }
                }
            }
        }
    }

    private fun checkCelebrationDay(): CelebrationDay? {
        val today = SimpleDateFormat("MM/dd", Locale.US)
            .format(Date())
            .split("/")
        val month = today[0].toInt()
        val day = today[1].toInt()

        val match = CelebrationConfig.days.firstOrNull {
            it.month == month && it.day == day
        } ?: return null

        val prefs = getSharedPreferences("celebration", Context.MODE_PRIVATE)
        val todayKey = "shown_${month}_$day"
        if (prefs.getBoolean(todayKey, false)) return null

        prefs.edit().putBoolean(todayKey, true).apply()

        return match
    }
}

