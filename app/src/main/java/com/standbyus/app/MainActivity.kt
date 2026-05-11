package com.standbyus.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.standbyus.app.navigation.Routes
import com.standbyus.app.ui.celebration.CelebrationConfig
import com.standbyus.app.ui.celebration.CelebrationDay
import com.standbyus.app.ui.celebration.CelebrationOverlay
import com.standbyus.app.ui.home.HomeScreen
import com.standbyus.app.ui.history.HistoryScreen
import com.standbyus.app.ui.poststatus.PostStatusScreen
import com.standbyus.app.ui.settings.SettingsScreen
import com.standbyus.app.ui.theme.StandByUsTheme
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                    NavHost(navController = navController, startDestination = Routes.HOME) {
                        composable(Routes.HOME) {
                            HomeScreen(
                                onNavigateToPost = { navController.navigate(Routes.POST_STATUS) },
                                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
                            )
                        }
                        composable(Routes.POST_STATUS) {
                            PostStatusScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Routes.HISTORY) {
                            HistoryScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Routes.SETTINGS) {
                            SettingsScreen(onBack = { navController.popBackStack() })
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
