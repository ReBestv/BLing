package com.standbyus.app.ui.album

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.standbyus.app.ui.theme.StandByUsLightColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.standbyus.app.data.model.AlbumPhoto
import com.standbyus.app.ui.components.AppHeader

@Composable
fun AlbumScreen(
    onBack: () -> Unit,
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val photos by viewModel.photos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val uploading by viewModel.uploading.collectAsState()
    val showSheet by viewModel.showUploadSheet.collectAsState()
    val selectedUri by viewModel.selectedUri.collectAsState()
    val caption by viewModel.caption.collectAsState()
    val error by viewModel.error.collectAsState()
    val filterDays by viewModel.filterDays.collectAsState()

    var fullScreenPhoto by remember { mutableStateOf<AlbumPhoto?>(null) }

    // 相册选择器
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> uri?.let { viewModel.selectPhoto(it) } }

    val snackbarHostState = remember { SnackbarHostState() }

    // 显示错误提示
    LaunchedEffect(error) {
        if (error.isNotEmpty()) {
            snackbarHostState.showSnackbar(error)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                AppHeader(
                    title = "我们的相册",
                    onBack = onBack,
                    rightIcon = if (!uploading) {
                        {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = "上传照片",
                                tint = Color(0xFF9E8E86),
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                            )
                        }
                    } else null
                )
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                // 横向可滚动的筛选标签
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(0 to "全部", 7 to "近7天", 30 to "近30天", 90 to "近90天")
                    filters.forEach { (days, label) ->
                        val isActive = filterDays == days
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isActive) Color(0xFFFFB4A2) else Color.Transparent)
                                .then(
                                    if (!isActive) Modifier.border(
                                        1.dp,
                                        Color(0xFFF0EAE6),
                                        RoundedCornerShape(20.dp)
                                    ) else Modifier
                                )
                                .clickable { viewModel.setFilter(days) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                color = if (isActive) Color.White else Color(0xFF9E8E86)
                            )
                        }
                    }
                }

                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (photos.isEmpty()) {
                    // 空状态
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📷", fontSize = 64.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "还没有照片",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = StandByUsLightColors.fgSecondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "点击右上角上传第一张",
                                fontSize = 13.sp,
                                color = StandByUsLightColors.muted
                            )
                        }
                    }
                } else {
                    // 3列照片网格
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(photos, key = { it.id }) { photo ->
                            PhotoCard(
                                photo = photo,
                                onClick = { fullScreenPhoto = photo },
                                onDelete = { viewModel.deletePhoto(photo) }
                            )
                        }
                    }
                }
            }
        }

        // 上传底部弹窗
        if (showSheet && selectedUri != null) {
            // 遮罩
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewModel.dismissSheet() }
                    .background(Color.Black.copy(alpha = 0.4f))
            )
            // 底部面板
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = StandByUsLightColors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 拖拽手柄
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(StandByUsLightColors.borderLight)
                    )
                    Spacer(Modifier.height(16.dp))

                    // 预览图
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(selectedUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(16.dp))

                    // 输入框
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { viewModel.updateCaption(it) },
                        placeholder = { Text("写一句小记（可选）") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    // 错误信息
                    if (error.isNotEmpty()) {
                        Text(
                            text = error,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // 发布按钮
                    Button(
                        onClick = { viewModel.upload() },
                        enabled = !uploading,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB4A2)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (uploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                "发布到我们的相册",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoCard(
    photo: AlbumPhoto,
    onDelete: () -> Unit = {}
) {
    var showConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = { },
                onLongClick = { showConfirm = true }
            )
    ) {
        // 图片 - 正方形
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.url)
                .crossfade(true)
                .build(),
            contentDescription = photo.caption,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        // 底部渐变遮罩 + 标题覆盖
        if (photo.caption.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        ),
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    )
            )
            Text(
                text = photo.caption,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            )
        }

        // 长按确认删除对话框
        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                title = { Text("删除照片") },
                text = { Text("确定要删除这张照片吗？") },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirm = false
                        onDelete()
                    }) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirm = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}
