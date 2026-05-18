package com.standbyus.app.ui.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.standbyus.app.ui.celebration.CelebrationDay
import com.standbyus.app.ui.celebration.CelebrationOverlay
import com.standbyus.app.ui.components.AppHeader

// ===== Design Tokens =====
private val Primary = Color(0xFFFFB4A2)
private val Danger = Color(0xFFE8505B)
private val Surface = Color(0xFFFFFFFF)
private val Background = Color(0xFFFFF8F5)
private val Border = Color(0xFFF0EAE6)
private val TextPrimary = Color(0xFF5A4A42)
private val TextSecondary = Color(0xFF9E8E86)
private val ToggleInactive = Color(0xFFC4C4C4)

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
    val currentTheme by viewModel.currentTheme.collectAsState()

    // Dark mode local state (visual only)
    var isDarkMode by remember { mutableStateOf(false) }

    // Emoji theme toggle: is cats theme active
    val isCatsTheme = currentTheme.id == "cats"

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ===== Custom App Bar =====
            AppHeader(
                title = "设置",
                onBack = onBack
            )

            // ===== Scrollable Content =====
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else {
                    // ===== Section 1: 配对状态 =====
                    SectionCard {
                        SectionTitle(text = "配对状态")

                        if (isPaired) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "已与 ${currentTheme.icon} 已绑定",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                OutlinedButton(
                                    onClick = { viewModel.unpair() },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Danger
                                    ),
                                    border = BorderStroke(1.dp, Danger),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "断开连接",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            // 创建配对码
                            Text(
                                text = "如果你先安装，生成配对码发给对方",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.createCode() },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "生成配对码",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            if (pairingCode.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = pairingCode,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 4.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = Border, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(24.dp))

                            // 加入配对
                            Text(
                                text = "输入对方的配对码",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = joinCodeInput,
                                onValueChange = { viewModel.updateJoinCode(it.uppercase()) },
                                placeholder = { Text("输入6位配对码", color = TextSecondary.copy(alpha = 0.5f)) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = Border,
                                    cursorColor = Primary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.joinPair() },
                                enabled = joinCodeInput.length == 6,
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary,
                                    disabledContainerColor = Primary.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "连接",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Status message
                    if (statusText.isNotEmpty() && !justPaired) {
                        Text(
                            text = statusText,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    // ===== Section 2: 外观 =====
                    SectionCard {
                        SectionTitle(text = "外观")

                        // 深色模式 toggle
                        ToggleRow(
                            label = "🌙 深色模式",
                            isActive = isDarkMode,
                            onToggle = { isDarkMode = !isDarkMode },
                            showDivider = true
                        )

                        // 猫咪主题 toggle
                        ToggleRow(
                            label = "😺 猫咪主题",
                            isActive = isCatsTheme,
                            onToggle = {
                                viewModel.selectTheme(if (isCatsTheme) "default" else "cats")
                            },
                            showDivider = false
                        )
                    }

                    // ===== Section 3: 关于 =====
                    SectionCard(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedButton(
                            onClick = { /* Preview celebration */ },
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Primary
                            ),
                            border = BorderStroke(1.dp, Primary)
                        ) {
                            Text(
                                text = "🎉 庆祝效果预览",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "StandByUs v1.0.0",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "专为情侣打造的私密空间",
                            fontSize = 12.sp,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // ===== 配对成功庆祝动画 =====
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

// ===== Section Card =====
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x1F5A4A42),
                spotColor = Color(0x1F5A4A42)
            )
            .background(Surface, RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

// ===== Section Title =====
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

// ===== Toggle Row =====
@Composable
private fun ToggleRow(
    label: String,
    isActive: Boolean,
    onToggle: () -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggle() }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                color = TextPrimary
            )
            CustomToggle(
                isActive = isActive,
                onToggle = onToggle
            )
        }
        if (showDivider) {
            HorizontalDivider(color = Border, thickness = 1.dp)
        }
    }
}

// ===== Custom Toggle =====
@Composable
private fun CustomToggle(
    isActive: Boolean,
    onToggle: () -> Unit
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (isActive) 24.dp else 2.dp,
        animationSpec = tween(durationMillis = 200),
        label = "thumbOffset"
    )

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isActive) Primary else ToggleInactive)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle() }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .shadow(2.dp, CircleShape)
                .background(Color.White, CircleShape)
        )
    }
}
