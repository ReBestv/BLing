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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.standbyus.app.ui.checkin.CheckinCalendarScreen
import com.standbyus.app.ui.history.HistoryScreen
import com.standbyus.app.ui.home.HomeScreen
import com.standbyus.app.ui.poststatus.PostStatusScreen
import com.standbyus.app.ui.settings.SettingsScreen
import com.standbyus.app.ui.todo.TodoScreen
import com.standbyus.app.ui.theme.Border
import com.standbyus.app.ui.theme.Primary
import com.standbyus.app.ui.theme.PrimarySoft
import com.standbyus.app.ui.theme.StandByUsTheme
import com.standbyus.app.ui.theme.Surface
import com.standbyus.app.ui.theme.TextSecondary
import com.standbyus.app.ui.theme.TextPrimary
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
                        BottomNavItem(Routes.TODO, NavIcons.Todo, "待办"),
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
                                    onBack = { navController.popBackStack() },
                                    onOpenCalendar = { navController.navigate(Routes.CHECKIN_CALENDAR) }
                                )
                            }
                            composable(Routes.CHECKIN_CALENDAR) {
                                CheckinCalendarScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(Routes.TODO) {
                                TodoScreen(
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
            .background(Surface.copy(alpha = 0.94f))
            .drawBehind {
                drawLine(
                    color = Border.copy(alpha = 0.82f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 10.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onItemSelected(item) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) PrimarySoft else androidx.compose.ui.graphics.Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = item.iconResId),
                        contentDescription = item.label,
                        tint = if (selected) Primary else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label,
                    color = if (selected) TextPrimary else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
