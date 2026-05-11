package com.standbyus.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.standbyus.app.ui.celebration.CelebrationDay
import com.standbyus.app.ui.celebration.CelebrationOverlay
import com.standbyus.app.ui.theme.Orange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val pairingCode by viewModel.pairingCode.collectAsState()
    val joinCodeInput by viewModel.joinCodeInput.collectAsState()
    val statusText by viewModel.status.collectAsState()
    val isPaired by viewModel.isPaired.collectAsState()
    val justPaired by viewModel.justPaired.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("设置") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            }
        ) { padding ->
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isPaired) {
                        // === 已配对状态 ===
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Orange.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "💕", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "已绑定",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Orange
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "你和另一半已经成功配对啦 🎉",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                // 取消配对按钮
                                OutlinedButton(
                                    onClick = { viewModel.unpair() },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("解除配对")
                                }
                            }
                        }
                    } else {
                        // === 创建配对码 ===
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("创建配对码", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(
                                    "如果你先安装，生成配对码发给对方",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.createCode() },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                                ) {
                                    Text("生成配对码")
                                }
                                if (pairingCode.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = pairingCode,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Orange,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // === 加入配对 ===
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("加入配对", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(
                                    "输入对方的配对码",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = joinCodeInput,
                                    onValueChange = { viewModel.updateJoinCode(it.uppercase()) },
                                    placeholder = { Text("输入6位配对码") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.joinPair() },
                                    enabled = joinCodeInput.length == 6,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                                ) {
                                    Text("连接")
                                }
                            }
                        }
                    }

                    // 状态信息
                    if (statusText.isNotEmpty() && !justPaired) {
                        Text(
                            text = statusText,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 配对成功庆祝动画
        if (justPaired) {
            CelebrationOverlay(
                celebration = CelebrationDay(
                    month = 0, day = 0,
                    emoji = "💕",
                    message = "配对成功！"
                ),
                onDismiss = { viewModel.dismissPairCelebration() }
            )
        }
    }
}
