package com.standbyus.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
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
import com.standbyus.app.ui.components.AppHeader
import com.standbyus.app.ui.components.AvatarPicker
import com.standbyus.app.ui.components.UserAvatar
import com.standbyus.app.ui.components.allAvatarEmojis
import com.standbyus.app.ui.theme.EmojiThemeSet
import com.standbyus.app.ui.theme.EmojiThemeManager

// ===== Design Tokens =====
private val Primary = Color(0xFFFF8E78)
private val Danger = Color(0xFFE8505B)
private val Surface = Color(0xFFFFFFFF)
private val Background = Color(0xFFFFF8F3)
private val Border = Color(0xFFEFE2DA)
private val TextPrimary = Color(0xFF3D3029)
private val TextSecondary = Color(0xFF8F7469)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val pairingCode by viewModel.pairingCode.collectAsState()
    val joinCodeInput by viewModel.joinCodeInput.collectAsState()
    val statusText by viewModel.status.collectAsState()
    val isPaired by viewModel.isPaired.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val nameInput by viewModel.nameInput.collectAsState()
    val nickname by viewModel.nicknameInput.collectAsState()
    val partnerDisplayName by viewModel.partnerDisplayName.collectAsState()
    val avatarEmoji by viewModel.avatarEmoji.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()
    val avatarUploading by viewModel.avatarUploading.collectAsState()
    val myName by viewModel.myName.collectAsState()

    // Avatar picker dialog state
    var showAvatarPicker by remember { mutableStateOf(false) }
    val avatarPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.uploadAvatar(it) } }

    // 动态主题列表
    val themes by viewModel.availableThemes.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Background)) {
        val layoutMetrics = SettingsLayout.metrics(
            availableWidthDp = maxWidth.value,
            availableHeightDp = maxHeight.value
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ===== Custom App Bar =====
            AppHeader(
                title = "设置",
                onBack = onBack
            )

            Box(modifier = Modifier.fillMaxSize()) {
                // ===== Scrollable Content =====
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = layoutMetrics.horizontalPaddingDp.dp)
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
                    SectionCard(contentPadding = layoutMetrics.cardPaddingDp.dp) {
                        SectionTitle(text = "个人头像")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAvatarPicker = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.shadow(
                                    12.dp,
                                    CircleShape,
                                    ambientColor = Primary.copy(alpha = 0.3f)
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                UserAvatar(
                                    avatarUrl = avatarUrl,
                                    avatarEmoji = avatarEmoji,
                                    size = layoutMetrics.profileAvatarSizeDp.dp,
                                    borderColor = Color.White,
                                    textSize = 32.sp
                                )
                                if (avatarUploading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(26.dp),
                                        strokeWidth = 2.dp,
                                        color = Primary
                                    )
                                }
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
                                    text = "照片或表情头像，会展示给对方",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // ===== Section 1: 配对状态 =====
                    SectionCard(contentPadding = layoutMetrics.cardPaddingDp.dp) {
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
                            AvatarPhotoPickerRow(
                                avatarUrl = avatarUrl,
                                avatarEmoji = avatarEmoji,
                                uploading = avatarUploading,
                                onPickPhoto = {
                                    avatarPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
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
                                PairingCodeChips(
                                    code = pairingCode,
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
                            AvatarPhotoPickerRow(
                                avatarUrl = avatarUrl,
                                avatarEmoji = avatarEmoji,
                                uploading = avatarUploading,
                                onPickPhoto = {
                                    avatarPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
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
                    if (statusText.isNotEmpty()) {
                        Text(
                            text = statusText,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    // ===== Section 2: 外观 =====
                    SectionCard(contentPadding = layoutMetrics.cardPaddingDp.dp) {
                        SectionTitle(text = "外观")

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
                        contentPadding = layoutMetrics.cardPaddingDp.dp,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Bling v1.1",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "专为情侣打造的私密空间",
                            fontSize = 12.sp,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "字体：标题使用得意黑，正文使用 MiSans",
                            fontSize = 11.sp,
                            color = TextSecondary.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "本应用已使用 MiSans Fonts",
                            fontSize = 11.sp,
                            color = TextSecondary.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ===== 头像选择弹窗 =====
    }

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
                        )
 {
                            Text(
                                text = "选择头像",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        AvatarPhotoPickerRow(
                            avatarUrl = avatarUrl,
                            avatarEmoji = avatarEmoji,
                            uploading = avatarUploading,
                            onPickPhoto = {
                                avatarPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AvatarPicker(
                            selectedEmoji = avatarEmoji,
                            onAvatarSelected = {
                                viewModel.selectAvatar(it)
                                showAvatarPicker = false
                            },
                            columns = 4,
                            modifier = Modifier.heightIn(max = layoutMetrics.avatarPickerMaxHeightDp.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

    }
}

// ===== Section Card =====
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.ui.unit.Dp = 20.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(27.dp),
                ambientColor = Color(0x145A4A42),
                spotColor = Color(0x145A4A42)
            )
            .clip(RoundedCornerShape(27.dp))
            .background(Surface.copy(alpha = 0.72f))
            .border(1.dp, Color(0xADFFFFFF), RoundedCornerShape(27.dp))
            .padding(contentPadding),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
private fun PairingCodeChips(
    code: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        code.take(6).forEach { char ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFF0EB), Color(0xFFFFE4DA))
                        )
                    )
                    .border(1.dp, Color(0x52FF8E78), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF8B5146)
                )
            }
        }
    }
}

// ===== Section Title =====
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
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
                val icon = theme.resolvedIcon
                if (icon != null && icon.startsWith("http")) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(icon)
                            .crossfade(true)
                            .build(),
                        contentDescription = theme.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Text(
                        text = icon ?: "🎨",
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

@Composable
private fun AvatarPhotoPickerRow(
    avatarUrl: String,
    avatarEmoji: String,
    uploading: Boolean,
    onPickPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFFF8F5))
            .border(1.dp, Border, RoundedCornerShape(18.dp))
            .clickable(enabled = !uploading) { onPickPhoto() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            UserAvatar(
                avatarUrl = avatarUrl,
                avatarEmoji = avatarEmoji,
                size = 42.dp,
                textSize = 22.sp,
                borderColor = Color.White
            )
            if (uploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = Primary
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (avatarUrl.isBlank()) "从相册选择" else "更换照片头像",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Text(
            text = if (uploading) "上传中" else "选择",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Primary
        )
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
