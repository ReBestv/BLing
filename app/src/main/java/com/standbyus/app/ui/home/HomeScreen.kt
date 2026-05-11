package com.standbyus.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.standbyus.app.ui.components.StatusCard
import com.standbyus.app.ui.theme.Orange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPost: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val partnerStatus by viewModel.partnerStatus.collectAsState()
    val myStatus by viewModel.myStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StandBy Us", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "历史")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Text("🏠", fontSize = 20.sp) },
                    label = { Text("首页") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Text("📜", fontSize = 20.sp) },
                    label = { Text("历史") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Partner status card
            StatusCard(
                status = partnerStatus,
                userName = "她",
                modifier = Modifier.weight(0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // My status bar
            Surface(
                onClick = onNavigateToPost,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = myStatus?.let {
                        "${it.feelingEmoji} ${it.doing}${if (it.customDoing.isNotEmpty()) " · ${it.customDoing}" else ""}"
                    } ?: "🤔 设置我的状态", fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("✏️", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Share button
            Button(
                onClick = onNavigateToPost,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                Text("✨ 分享瞬间", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
