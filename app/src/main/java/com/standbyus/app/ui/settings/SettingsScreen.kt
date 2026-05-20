package com.standbyus.app.ui.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.window.Dialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.standbyus.app.ui.celebration.CelebrationDay
import com.standbyus.app.ui.celebration.CelebrationOverlay
import com.standbyus.app.ui.components.AppHeader
import com.standbyus.app.ui.components.AvatarPicker
import com.standbyus.app.ui.components.allAvatarEmojis
import com.standbyus.app.ui.theme.EmojiThemeSet
import com.standbyus.app.ui.theme.EmojiThemeManager

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
    val nameInput by viewModel.nameInput.collectAsState()
    val nickname by viewModel.nicknameInput.collectAsState()
    val partnerDisplayName by viewModel.partnerDisplayName.collectAsState()
    val avatarEmoji by viewModel.avatarEmoji.collectAsState()
    val myName by viewModel.myName.collectAsState()

    // Dark mode local state (visual only)
    var isDarkMode by remember { mutableStateOf(false) }

    // Avatar picker dialog state
    var showAvatarPicker by remember { mutableStateOf(false) }

    // 动态主题列表
    val themes by viewModel.availableThemes.collectAsState()

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
                    // ===== Section 0: 个人头像 =====
                    SectionCard {
                        SectionTitle(text = "个人头像")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAvatarPicker = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Avatar circle
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .shadow(12.dp, CircleShape, ambientColor = Primary.copy(alpha = 0.3f))
                                    .clip(CircleShape)
                                    .background(Color(0xFFF5F0ED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = avatarEmoji, fontSize = 32.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (myName.isNotEmpty()) myName else "我",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "点击更换头像",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // ===== Section 1: 配对状态 =====
                    SectionCard {
                        SectionTitle(text = "配对状态")

                        if (isPaired) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "已与 $partnerDisplayName 已绑定",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "对方昵称",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = nickname,
                                    onValueChange = { viewModel.updateNickname(it) },
                                    placeholder = { Text("输入对方昵称", color = TextSecondary.copy(alpha = 0.5f)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = Border,
                                        cursorColor = Primary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
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
                            // 名字输入
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { viewModel.updateNameInput(it) },
                                placeholder = { Text("输入你的昵称", color = TextSecondary.copy(alpha = 0.5f)) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = Border,
                                    cursorColor = Primary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // 选择头像
                            Text(
                                text = "选择你的头像",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            InlineAvatarPicker(
                                selectedEmoji = avatarEmoji,
                                onSelected = { viewModel.selectAvatar(it) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.createCode() },
                                enabled = nameInput.isNotBlank(),
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
                                text = "输入你的昵称",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { viewModel.updateNameInput(it) },
                                placeholder = { Text("输入你的昵称", color = TextSecondary.copy(alpha = 0.5f)) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = Border,
                                    cursorColor = Primary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
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
                            // 选择头像
                            Text(
                                text = "选择你的头像",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            InlineAvatarPicker(
                                selectedEmoji = avatarEmoji,
                                onSelected = { viewModel.selectAvatar(it) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.joinPair() },
                                enabled = joinCodeInput.length == 6 && nameInput.isNotBlank(),
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

                        // 动态主题列表
                        Column(modifier = Modifier.fillMaxWidth()) {
                            themes.forEach { theme ->
                                ThemeRow(
                                    theme = theme,
                                    isSelected = currentTheme.id == theme.id,
                                    onClick = { viewModel.selectTheme(theme.id) }
                                )
                            }
                        }
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

        // ===== 头像选择弹窗 =====
        if (showAvatarPicker) {
            Dialog(onDismissRequest = { showAvatarPicker = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Surface,
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Column {
                        // Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF8F5), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "选择头像",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        AvatarPicker(
                            selectedEmoji = avatarEmoji,
                            onAvatarSelected = {
                                viewModel.selectAvatar(it)
                                showAvatarPicker = false
                            },
                            modifier = Modifier.heightIn(max = 400.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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

// ===== Theme Row =====
@Composable
private fun ThemeRow(
    theme: EmojiThemeSet,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() }
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 主题图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF5F0ED)),
                contentAlignment = Alignment.Center
            ) {
                if (theme.icon != null && theme.icon.startsWith("http")) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(theme.icon)
                            .crossfade(true)
                            .build(),
                        contentDescription = theme.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Text(
                        text = theme.icon ?: "🎨",
                        fontSize = 20.sp
                    )
                }
            }

            // 主题名
            Text(
                text = theme.name,
                fontSize = 15.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )

            // 选中勾
            if (isSelected) {
                Text(text = "✓", color = Primary, fontSize = 18.sp)
            }
        }

        HorizontalDivider(color = Border, thickness = 0.5.dp)
    }
}

// ===== Inline Avatar Picker =====
@Composable
private fun InlineAvatarPicker(
    selectedEmoji: String,
    onSelected: (String) -> Unit
) {
    val emojis = allAvatarEmojis.chunked(7)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        emojis.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { emoji ->
                    val isSelected = emoji == selectedEmoji
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Primary.copy(alpha = 0.2f) else Color(0xFFF5F0ED))
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}
