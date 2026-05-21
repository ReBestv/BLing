package com.standbyus.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.standbyus.app.ui.checkin.CheckinScreen
import com.standbyus.app.ui.history.HistoryScreen
import com.standbyus.app.ui.home.HomeScreen
import com.standbyus.app.ui.poststatus.PostStatusScreen
import com.standbyus.app.ui.settings.SettingsScreen
import com.standbyus.app.ui.theme.Border
import com.standbyus.app.ui.theme.Primary
import com.standbyus.app.ui.theme.StandByUsTheme
import com.standbyus.app.ui.theme.Surface
import com.standbyus.app.ui.theme.TextSecondary
import com.standbyus.app.ui.theme.NavIcons
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class BottomNavItem(
    val route: String,
    @DrawableRes val iconResId: Int,
    val label: String
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // No-op: notification delivery gracefully skips when permission is denied.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        val celebration = checkCelebrationDay()

        setContent {
            StandByUsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showCelebration by remember { mutableStateOf(celebration != null) }
                    val activeCelebration = remember { celebration }

                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val navItems = listOf(
                        BottomNavItem(Routes.HOME, NavIcons.Home, "首页"),
                        BottomNavItem(Routes.CHECKIN, NavIcons.Checkin, "打卡"),
                        BottomNavItem(Routes.ALBUM, NavIcons.Album, "相册"),
                        BottomNavItem(Routes.HISTORY, NavIcons.Timeline, "时光轴")
                    )

                    Scaffold(
                        bottomBar = {
                            if (currentRoute in navItems.map { it.route }) {
                                BottomNavBar(
                                    items = navItems,
                                    currentRoute = currentRoute,
                                    onItemSelected = { item ->
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
                            composable(Routes.CHECKIN) {
                                CheckinScreen(
                                    onBack = { navController.popBackStack() }
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
        val now = Date()
        val today = SimpleDateFormat("MM/dd", Locale.US).format(now).split("/")
        val dateKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(now)
        val month = today[0].toInt()
        val day = today[1].toInt()

        val match = CelebrationConfig.match(month, day) ?: return null

        val prefs = getSharedPreferences("celebration", Context.MODE_PRIVATE)
        val todayKey = CelebrationConfig.displayKey(match.id, dateKey)
        if (prefs.getBoolean(todayKey, false)) return null

        prefs.edit().putBoolean(todayKey, true).apply()

        return match
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val prefs = getSharedPreferences(NOTIFICATION_PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_NOTIFICATION_PERMISSION_ASKED, false)) return

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            prefs.edit().putBoolean(KEY_NOTIFICATION_PERMISSION_ASKED, true).apply()
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        private const val NOTIFICATION_PREFS_NAME = "notification_permission"
        private const val KEY_NOTIFICATION_PERMISSION_ASKED = "asked_once"
    }
}

@Composable
private fun BottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemSelected: (BottomNavItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .drawBehind {
                drawLine(
                    color = Border,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onItemSelected(item) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = item.iconResId),
                    contentDescription = item.label,
                    tint = if (selected) Primary else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.label,
                    color = if (selected) Primary else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
