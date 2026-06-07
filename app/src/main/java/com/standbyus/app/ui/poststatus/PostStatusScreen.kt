package com.standbyus.app.ui.poststatus

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.ThemeSticker
import com.standbyus.app.ui.theme.EmojiThemeManager
import com.standbyus.app.ui.components.AppHeader

// Design tokens — matches the redesign preview direction.
private val DesignPrimary = Color(0xFFFF8E78)
private val DesignPrimarySoft = Color(0xFFFFF0EB)
private val DesignBg = Color(0xFFFFF8F3)
private val DesignBorder = Color(0xFFEFE2DA)

private fun isEmoji(text: String) = !text.startsWith("http")

@Composable
fun PostStatusScreen(
    onBack: () -> Unit,
    viewModel: PostStatusViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val themeVersion by EmojiThemeManager.themeVersion.collectAsState()
    val currentTheme = remember(themeVersion) { EmojiThemeManager.getCurrentTheme(context) }
    val hasThemeStickers = currentTheme.stickers.isNotEmpty()
    val selectedSticker = remember(currentTheme, viewModel.selectedStickerId) {
        currentTheme.stickerById(viewModel.selectedStickerId.orEmpty())
    }
    val selectedStickerFeeling = remember(selectedSticker) {
        selectedSticker?.let { EmojiThemeManager.inferFeelingFromSticker(it) }
    }
    val statusAccentFeeling = if (hasThemeStickers) {
        selectedStickerFeeling
    } else {
        viewModel.selectedFeeling
    }

    LaunchedEffect(hasThemeStickers) {
        if (hasThemeStickers) {
            viewModel.clearFeelingSelection()
        }
    }

    // Animated background tint based on selected feeling
    val bgTint by animateColorAsState(
        targetValue = (statusAccentFeeling?.color ?: PostStatusViewModel.NeutralFeelingColor)
            .copy(alpha = 0.08f),
        animationSpec = tween(400)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgTint, DesignBg),
                    startY = 0f,
                    endY = 1200f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // ── Custom App Bar ──
            AppHeader(
                title = "发布状态",
                onBack = onBack,
                showDivider = false
            )

            // ── Content Area ──
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val layoutMetrics = PostStatusLayout.metrics(
                    availableWidthDp = maxWidth.value,
                    availableHeightDp = maxHeight.value
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = layoutMetrics.horizontalPaddingDp.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Spacer(modifier = Modifier.height(layoutMetrics.topSpacerDp.dp))

                if (hasThemeStickers) {
                    StickerGridPicker(
                        stickers = currentTheme.stickers,
                        bucket = currentTheme.bucket,
                        selectedStickerId = viewModel.selectedStickerId,
                        onStickerClick = viewModel::selectSticker,
                        imageSizeDp = layoutMetrics.stickerImageSizeDp,
                        gridHeightDp = layoutMetrics.stickerGridHeightDp,
                        cardHorizontalPaddingDp = layoutMetrics.stickerCardHorizontalPaddingDp,
                        cardVerticalPaddingDp = layoutMetrics.stickerCardVerticalPaddingDp,
                        gridHorizontalGapDp = layoutMetrics.stickerGridHorizontalGapDp,
                        gridVerticalGapDp = layoutMetrics.stickerGridVerticalGapDp,
                        labelLineHeightSp = layoutMetrics.stickerLabelLineHeightSp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(layoutMetrics.sectionGapDp.dp))
                }

                if (!hasThemeStickers) {
                    MoodGridPicker(
                        feelings = Feeling.entries,
                        selectedFeeling = viewModel.selectedFeeling,
                        onFeelingClick = viewModel::selectFeeling,
                        themeVersion = themeVersion,
                        gridHeightDp = layoutMetrics.moodGridHeightDp,
                        circleSizeDp = layoutMetrics.moodCircleSizeDp,
                        imageSizeDp = layoutMetrics.moodImageSizeDp,
                        emojiTextSizeSp = layoutMetrics.moodEmojiTextSizeSp,
                        labelTopGapDp = layoutMetrics.labelTopGapDp,
                        labelLineHeightSp = layoutMetrics.moodLabelLineHeightSp,
                        gridHorizontalGapDp = layoutMetrics.gridHorizontalGapDp,
                        gridVerticalGapDp = layoutMetrics.gridVerticalGapDp,
                        useDefaultEmoji = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(layoutMetrics.sectionGapDp.dp))

                PhraseSuggestionChips(
                    selectedSticker = selectedSticker,
                    selectedFeeling = statusAccentFeeling,
                    onPhraseClick = viewModel::updateCustomDoing,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height((layoutMetrics.sectionGapDp * 0.6f).dp))

                // ── Note input (replaces DoingPicker) ──
                OutlinedTextField(
                    value = viewModel.customDoing,
                    onValueChange = { viewModel.updateCustomDoing(it) },
                    placeholder = {
                        Text(
                            "想补充点什么...",
                            fontSize = 15.sp,
                            color = Color(0xFFA29B97)
                        )
                    },
                    textStyle = TextStyle(fontSize = 15.sp),
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(layoutMetrics.noteHeightDp.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignPrimary,
                        unfocusedBorderColor = DesignBorder,
                        cursorColor = DesignPrimary,
                        focusedContainerColor = Color.White.copy(alpha = 0.84f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.72f)
                    )
                )

                Spacer(modifier = Modifier.height((layoutMetrics.sectionGapDp * 0.7f).dp))

                // ── Error message ──
                if (viewModel.error.isNotEmpty()) {
                    Text(
                        text = viewModel.error,
                        color = Color(0xFFDC2626),
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height((layoutMetrics.sectionGapDp * 0.7f).dp))
                }
            }

            // ── Publish button pinned to bottom ──
            Button(
                onClick = { viewModel.publish() },
                enabled = !viewModel.isPublishing && !viewModel.published,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = DesignPrimary.copy(alpha = 0.22f),
                        spotColor = DesignPrimary.copy(alpha = 0.22f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DesignPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = DesignPrimary.copy(alpha = 0.48f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                )
            ) {
                if (viewModel.isPublishing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else if (viewModel.published) {
                    Text(
                        "已发布！",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        "发布",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Navigate back after successful publish
    LaunchedEffect(viewModel.published) {
        if (viewModel.published) {
            kotlinx.coroutines.delay(1000)
            onBack()
        }
    }
}

@Composable
private fun MoodGridPicker(
    feelings: List<Feeling>,
    selectedFeeling: Feeling?,
    onFeelingClick: (Feeling) -> Unit,
    themeVersion: Int,
    gridHeightDp: Float,
    circleSizeDp: Float,
    imageSizeDp: Float,
    emojiTextSizeSp: Float,
    labelTopGapDp: Float,
    labelLineHeightSp: Float,
    gridHorizontalGapDp: Float,
    gridVerticalGapDp: Float,
    useDefaultEmoji: Boolean,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.height(gridHeightDp.dp),
        horizontalArrangement = Arrangement.spacedBy(gridHorizontalGapDp.dp),
        verticalArrangement = Arrangement.spacedBy(gridVerticalGapDp.dp),
        userScrollEnabled = false
    ) {
        items(feelings) { feeling ->
            MoodOption(
                feeling = feeling,
                isSelected = feeling == selectedFeeling,
                onClick = { onFeelingClick(feeling) },
                themeVersion = themeVersion,
                circleSizeDp = circleSizeDp,
                imageSizeDp = imageSizeDp,
                emojiTextSizeSp = emojiTextSizeSp,
                labelTopGapDp = labelTopGapDp,
                labelLineHeightSp = labelLineHeightSp,
                useDefaultEmoji = useDefaultEmoji
            )
        }
    }
}

@Composable
private fun MoodOption(
    feeling: Feeling,
    isSelected: Boolean,
    onClick: () -> Unit,
    themeVersion: Int,
    circleSizeDp: Float,
    imageSizeDp: Float,
    emojiTextSizeSp: Float,
    labelTopGapDp: Float,
    labelLineHeightSp: Float,
    useDefaultEmoji: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(circleSizeDp.dp)
                .scale(if (isSelected) 1.12f else 1f)
                .shadow(
                    elevation = if (isSelected) 12.dp else 0.dp,
                    shape = CircleShape,
                    ambientColor = feeling.color.copy(alpha = 0.3f),
                    spotColor = feeling.color.copy(alpha = 0.3f)
                )
                .background(
                    color = if (isSelected) feeling.color.copy(alpha = 0.25f) else Color(0xFFFFF2EF),
                    shape = CircleShape
                )
                .then(
                    if (isSelected) {
                        Modifier
                            .border(3.dp, Color.White, CircleShape)
                            .border(2.dp, feeling.color, CircleShape)
                    } else {
                        Modifier.border(1.dp, DesignBorder, CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val emoji = remember(themeVersion, feeling.key, useDefaultEmoji) {
                if (useDefaultEmoji) feeling.emoji else EmojiThemeManager.getEmoji(context, feeling.key)
            }
            if (isEmoji(emoji)) {
                Text(text = emoji, fontSize = emojiTextSizeSp.sp)
            } else {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(emoji)
                        .crossfade(true)
                        .build(),
                    contentDescription = feeling.displayName,
                    loading = {
                        Box(
                            modifier = Modifier.size(imageSizeDp.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size((imageSizeDp * 0.32f).dp),
                                strokeWidth = 2.dp,
                                color = feeling.color
                            )
                        }
                    },
                    error = {
                        Text(text = feeling.emoji, fontSize = emojiTextSizeSp.sp)
                    },
                    modifier = Modifier
                        .size(imageSizeDp.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(labelTopGapDp.dp))

        Text(
            text = feeling.displayName,
            fontSize = 12.sp,
            lineHeight = labelLineHeightSp.sp,
            color = if (isSelected) Color(0xFF252220) else Color(0xFF807975),
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun StickerGridPicker(
    stickers: List<ThemeSticker>,
    bucket: String,
    selectedStickerId: String?,
    onStickerClick: (ThemeSticker) -> Unit,
    imageSizeDp: Float,
    gridHeightDp: Float,
    cardHorizontalPaddingDp: Float,
    cardVerticalPaddingDp: Float,
    gridHorizontalGapDp: Float,
    gridVerticalGapDp: Float,
    labelLineHeightSp: Float,
    modifier: Modifier = Modifier
) {
    if (stickers.isEmpty()) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "表情贴纸",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF807975)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeightDp.dp),
            horizontalArrangement = Arrangement.spacedBy(gridHorizontalGapDp.dp),
            verticalArrangement = Arrangement.spacedBy(gridVerticalGapDp.dp),
            userScrollEnabled = false
        ) {
            items(stickers) { sticker ->
                val isSelected = sticker.id == selectedStickerId
                val stickerUrl = remember(bucket, sticker.asset) {
                    if (sticker.asset.startsWith("http")) {
                        sticker.asset
                    } else {
                        com.standbyus.app.ui.theme.themeAssetUrl(bucket, sticker.asset)
                    }
                }
                Surface(
                    onClick = { onStickerClick(sticker) },
                    shape = RoundedCornerShape(22.dp),
                    color = if (isSelected) DesignPrimarySoft else Color.White.copy(alpha = 0.70f),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) DesignPrimary.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.68f)
                    ),
                    modifier = Modifier
                        .scale(if (isSelected) 1.04f else 1f)
                        .shadow(
                            elevation = if (isSelected) 12.dp else 0.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = DesignPrimary.copy(alpha = 0.12f),
                            spotColor = DesignPrimary.copy(alpha = 0.12f)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = cardHorizontalPaddingDp.dp,
                                vertical = cardVerticalPaddingDp.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(stickerUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = sticker.label,
                            modifier = Modifier
                                .size(imageSizeDp.dp)
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sticker.label,
                            fontSize = 11.sp,
                            lineHeight = labelLineHeightSp.sp,
                            maxLines = 1,
                            color = if (isSelected) Color(0xFF5A4A42) else Color(0xFF807975),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhraseSuggestionChips(
    selectedSticker: ThemeSticker?,
    selectedFeeling: Feeling?,
    onPhraseClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val phrases = remember(selectedSticker, selectedFeeling) {
        StatusPhraseSuggestions.forSticker(
            sticker = selectedSticker,
            inferredFeeling = selectedFeeling
        )
    }
    val accentColor = selectedFeeling?.color ?: PostStatusViewModel.NeutralFeelingColor

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "常用短句",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF807975)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(phrases) { phrase ->
                Surface(
                    onClick = { onPhraseClick(phrase) },
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.72f),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                accentColor.copy(alpha = 0.34f),
                                Color.White.copy(alpha = 0.78f)
                            )
                        )
                    ),
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = phrase,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5A4A42),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
