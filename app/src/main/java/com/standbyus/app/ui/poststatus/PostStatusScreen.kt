package com.standbyus.app.ui.poststatus

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.standbyus.app.ui.components.DoingPicker
import com.standbyus.app.ui.components.FeelingPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostStatusScreen(
    onBack: () -> Unit,
    viewModel: PostStatusViewModel = hiltViewModel()
) {
    val bgColor by animateColorAsState(
        targetValue = viewModel.selectedFeeling.color.copy(alpha = 0.15f),
        animationSpec = tween(300)
    )

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("发布状态") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 心情选择（上下翻页）
                FeelingPicker(
                    selectedFeeling = viewModel.selectedFeeling,
                    onFeelingSelected = { viewModel.selectFeeling(it) },
                    modifier = Modifier.weight(1f)
                )

                // 自定义状态（可选）
                DoingPicker(
                    selectedFeeling = viewModel.selectedFeeling,
                    customDoing = viewModel.customDoing,
                    onCustomDoingChanged = { viewModel.updateCustomDoing(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error message
                if (viewModel.error.isNotEmpty()) {
                    Text(
                        text = viewModel.error,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // Publish button
                Button(
                    onClick = { viewModel.publish() },
                    enabled = !viewModel.isPublishing && !viewModel.published,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = viewModel.selectedFeeling.color
                    )
                ) {
                    if (viewModel.isPublishing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else if (viewModel.published) {
                        Text("✅ 已发布！", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("✨ 发布状态", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Auto-navigate back after publish
    LaunchedEffect(viewModel.published) {
        if (viewModel.published) {
            kotlinx.coroutines.delay(1000)
            onBack()
        }
    }
}
