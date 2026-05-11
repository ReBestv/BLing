# 生日/纪念日全屏庆祝动画 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 App 启动时检测当前日期，匹配到预配置的节日/纪念日则弹出全屏庆祝动画覆盖层。

**Architecture:** 纯 Compose 组件，无额外依赖。使用 Canvas 绘制粒子动画 + 覆盖层实现全屏效果。日期检测和每日频率控制通过 SimpleDateFormat + SharedPreferences 实现。

**Tech Stack:** Jetpack Compose Animation, Canvas, SharedPreferences

---

### Task 1: 创建数据类和配置

**Files:**
- Create: `app/src/main/java/com/standbyus/app/ui/celebration/CelebrationDay.kt`
- Create: `app/src/main/java/com/standbyus/app/ui/celebration/CelebrationConfig.kt`

**Context:** 这两文件定义纪念日的数据结构和可配置列表。

- [ ] **Step 1: 创建 CelebrationDay.kt**

```kotlin
package com.standbyus.app.ui.celebration

data class CelebrationDay(
    val month: Int,
    val day: Int,
    val emoji: String,
    val message: String
)
```

- [ ] **Step 2: 创建 CelebrationConfig.kt**

```kotlin
package com.standbyus.app.ui.celebration

object CelebrationConfig {
    val days = listOf(
        CelebrationDay(5, 7, "🎂", "生日快乐！"),
        CelebrationDay(12, 25, "🎄", "圣诞快乐！"),
        CelebrationDay(2, 14, "💕", "情人节快乐！"),
    )
}
```

- [ ] **Step 3: Commit**

---

### Task 2: 创建全屏庆祝动画 Composable

**Files:**
- Create: `app/src/main/java/com/standbyus/app/ui/celebration/CelebrationOverlay.kt`

**Context:** 全屏覆盖层，包含：
- 半透明黑色遮罩
- Canvas 绘制的 20 个彩色粒子（圆点 + emoji 混合）
- 中心缩放弹入的 emoji + 祝福语
- 点击任意位置或 4 秒后自动关闭

- [ ] **Step 1: 创建 CelebrationOverlay.kt**

```kotlin
package com.standbyus.app.ui.celebration

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

// 单个粒子的状态
private data class Particle(
    var x: Float,
    var y: Float,
    var speed: Float,
    var wobblePhase: Float,
    var wobbleSpeed: Float,
    var size: Float,
    var color: Color
)

@Composable
fun CelebrationOverlay(
    celebration: CelebrationDay,
    onDismiss: () -> Unit
) {
    val colors = remember {
        listOf(
            Color(0xFFFF9F43),
            Color(0xFFFFD93D),
            Color(0xFF74B9FF),
            Color(0xFFFD79A8),
            Color(0xFF55EFC4),
            Color(0xFFA29BFE),
            Color(0xFF00CEC9),
        )
    }

    // 初始化 20 个粒子（彩色圆点）
    val particles = remember {
        (1..20).map {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -1f,
                speed = 0.2f + Random.nextFloat() * 0.5f,
                wobblePhase = Random.nextFloat() * 6.28f,
                wobbleSpeed = 0.5f + Random.nextFloat() * 1.5f,
                size = 8f + Random.nextFloat() * 12f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    // 入场弹性缩放
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.5f,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // 4 秒后自动关闭
    LaunchedEffect(Unit) {
        delay(4000)
        onDismiss()
    }

    // 粒子动画
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleProgress"
    )

    Box(modifier = Modifier.fillMaxSize().clickable(onClick = onDismiss)) {
        // 半透明黑色遮罩
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color.Black.copy(alpha = 0.45f))
        }

        // 粒子层
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = progress / 1000f
            for (p in particles) {
                p.y = ((p.y + p.speed * t / 20f) % 1.2f) - 0.2f
                p.x += kotlin.math.sin(p.wobblePhase + t * p.wobbleSpeed) * 0.005f
                p.x = p.x.coerceIn(0f, 1f)

                drawCircle(
                    color = p.color,
                    radius = p.size,
                    center = Offset(p.x * w, p.y * h)
                )
            }
        }

        // 中心内容
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
            ) {
                Text(
                    text = celebration.emoji,
                    fontSize = 72.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = celebration.message,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

---

### Task 3: 修改 MainActivity 集成覆盖层

**Files:**
- Modify: `app/src/main/java/com/standbyus/app/MainActivity.kt`

**Context:** 在 HomeScreen 外层添加覆盖层逻辑。检测当前日期是否匹配配置，检查今日是否已显示，若匹配则显示 CelebrationOverlay。

- [ ] **Step 1: 修改 MainActivity.kt**

替换原有内容为：

```kotlin
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

                    // 导航内容
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

                    // 庆祝覆盖层（最上层）
                    if (showCelebration && activeCelebration != null) {
                        CelebrationOverlay(
                            celebration = activeCelebration,
                            onDismiss = {
                                showCelebration = false
                            }
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

        // 检查是否匹配配置
        val match = CelebrationConfig.days.firstOrNull {
            it.month == month && it.day == day
        } ?: return null

        // 检查今日是否已显示
        val prefs = getSharedPreferences("celebration", Context.MODE_PRIVATE)
        val todayKey = "shown_${month}_$day"
        if (prefs.getBoolean(todayKey, false)) return null

        // 标记今日已显示
        prefs.edit().putBoolean(todayKey, true).apply()

        return match
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd "E:/AndroidProject/StandByUs" && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**
