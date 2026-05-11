package com.standbyus.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.UserStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val history by viewModel.statusHistory.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // 按日期分组
    val groupedHistory = remember(history) {
        history.groupBy { status ->
            val cal = Calendar.getInstance().apply { timeInMillis = status.updatedAt }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
        }.entries.sortedByDescending { it.key }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("时光轴") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty() && !loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "还没有状态记录",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "分享你的第一个瞬间吧",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (loading && history.isNotEmpty()) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }
                }

                groupedHistory.forEach { (dateKey, statuses) ->
                    item {
                        DateHeader(dateKey)
                    }
                    items(statuses) { status ->
                        HistoryItem(status = status)
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun DateHeader(dateKey: String) {
    val parts = dateKey.split("-")
    val cal = Calendar.getInstance()
    val todayCal = Calendar.getInstance()

    val label = if (parts.size == 3) {
        val dateCal = Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
        val daysDiff = ((todayCal.timeInMillis - dateCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        when {
            daysDiff == 0 -> "今天"
            daysDiff == 1 -> "昨天"
            daysDiff <= 7 -> "${daysDiff}天前"
            else -> {
                val fmt = SimpleDateFormat("M月d日", Locale.CHINESE)
                fmt.format(Date(dateCal.timeInMillis))
            }
        }
    } else dateKey

    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun HistoryItem(status: UserStatus) {
    val feeling = Feeling.fromDisplayName(status.feeling) ?: Feeling.HAPPY
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = sdf.format(Date(status.updatedAt))

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = feeling.color.copy(alpha = 0.12f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：时间
            Text(
                text = timeStr,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(44.dp)
            )

            // 中间：Emoji 圆点
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(feeling.color),
                contentAlignment = Alignment.Center
            ) {
                Text(feeling.emoji, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 右侧内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${feeling.displayName} · ${status.customDoing.ifEmpty { status.doing }}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (status.note.isNotEmpty()) {
                    Text(
                        text = status.note,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
