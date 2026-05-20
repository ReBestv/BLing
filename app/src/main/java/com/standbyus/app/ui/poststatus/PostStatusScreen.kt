package com.standbyus.app.ui.poststatus

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.standbyus.app.ui.theme.EmojiThemeManager
import com.standbyus.app.ui.components.AppHeader

// Design tokens — matches preview.html
private val DesignPrimary = Color(0xFFFFB4A2)
private val DesignBg = Color(0xFFFFF8F5)
private val DesignBorder = Color(0xFFF0EAE6)

private fun isEmoji(text: String) = !text.startsWith("http")

@Composable
fun PostStatusScreen(
    onBack: () -> Unit,
    viewModel: PostStatusViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val themeVersion by EmojiThemeManager.themeVersion.collectAsState()

    // Animated background tint based on selected feeling
    val bgTint by animateColorAsState(
        targetValue = viewModel.selectedFeeling.color.copy(alpha = 0.08f),
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // ── 4-column mood grid (16 feelings) ──
                val allFeelings = Feeling.entries

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    userScrollEnabled = false
                ) {
                    items(allFeelings) { feeling ->
                        val isSelected = feeling == viewModel.selectedFeeling

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.selectFeeling(feeling) }
                                .padding(vertical = 4.dp)
                        ) {
                            // Mood circle
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .scale(if (isSelected) 1.12f else 1f)
                                    .shadow(
                                        elevation = if (isSelected) 12.dp else 0.dp,
                                        shape = CircleShape,
                                        ambientColor = feeling.color.copy(alpha = 0.3f),
                                        spotColor = feeling.color.copy(alpha = 0.3f)
                                    )
                                    .background(
                                        color = if (isSelected) feeling.color.copy(alpha = 0.25f)
                                        else Color(0xFFFFF2EF),
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
                                val emoji = remember(themeVersion, feeling.displayName) {
                                    EmojiThemeManager.getEmoji(context, feeling.displayName)
                                }
                                if (isEmoji(emoji)) {
                                    Text(text = emoji, fontSize = 32.sp)
                                } else {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(emoji)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = feeling.displayName,
                                        loading = {
                                            Box(
                                                modifier = Modifier.size(58.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp,
                                                    color = feeling.color
                                                )
                                            }
                                        },
                                        error = {
                                            Text(text = feeling.emoji, fontSize = 28.sp)
                                        },
                                        modifier = Modifier
                                            .size(58.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Label
                            Text(
                                text = feeling.displayName,
                                fontSize = 12.sp,
                                color = if (isSelected) Color(0xFF252220)
                                else Color(0xFF807975),
                                fontWeight = if (isSelected) FontWeight.Medium
                                else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignPrimary,
                        unfocusedBorderColor = DesignBorder,
                        cursorColor = DesignPrimary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(16.dp))
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
                        ambientColor = Color(0x0F5A4A42),
                        spotColor = Color(0x0F5A4A42)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DesignPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = DesignPrimary.copy(alpha = 0.6f),
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
