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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.standbyus.app.data.model.AlbumPhoto
import com.standbyus.app.ui.components.AppHeader

import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer

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
    val lifecycleOwner = LocalLifecycleOwner.current

    var fullScreenPhoto by remember { mutableStateOf<AlbumPhoto?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val overlayLayoutMetrics = AlbumLayout.metrics(
            availableWidthDp = maxWidth.value,
            availableHeightDp = maxHeight.value
        )

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
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val layoutMetrics = AlbumLayout.metrics(
                    availableWidthDp = maxWidth.value,
                    availableHeightDp = maxHeight.value
                )
            Column(Modifier.fillMaxSize()) {
                // 横向可滚动的筛选标签
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(
                            horizontal = layoutMetrics.horizontalPaddingDp.dp,
                            vertical = layoutMetrics.filterVerticalPaddingDp.dp
                        ),
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
                                .padding(
                                    horizontal = layoutMetrics.filterHorizontalPaddingDp.dp,
                                    vertical = layoutMetrics.filterVerticalPaddingDp.dp
                                )
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
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = layoutMetrics.horizontalPaddingDp.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xE6FFFFFF), Color(0x8CFFFFFF))
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0x66FF8E78),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 34.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("▧", fontSize = 48.sp, color = Color(0xFFFF8E78))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "空相册也可以有第一束光",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = StandByUsLightColors.fg
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "点击右上角上传第一张照片，把某个小瞬间留给我们。",
                                fontSize = 13.sp,
                                color = StandByUsLightColors.muted,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    AlbumMosaic(
                        photos = photos,
                        layoutMetrics = layoutMetrics,
                        onPhotoClick = { fullScreenPhoto = it },
                        onPhotoDelete = viewModel::deletePhoto,
                        modifier = Modifier.fillMaxSize()
                    )
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
                        .padding(overlayLayoutMetrics.sheetPaddingDp.dp),
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
                    Spacer(Modifier.height(overlayLayoutMetrics.sheetSpacerDp.dp))

                    // 预览图
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(selectedUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(overlayLayoutMetrics.sheetPreviewSizeDp.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(overlayLayoutMetrics.sheetSpacerDp.dp))

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
                            .height(overlayLayoutMetrics.sheetButtonHeightDp.dp)
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

        // 全屏查看图片
        fullScreenPhoto?.let { photo ->
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val uploadedAtLabel = remember(photo.createdAt) {
                albumUploadedAtLabel(photo.createdAt)
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offset += pan
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photo.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = photo.caption,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { fullScreenPhoto = null },
                    contentScale = ContentScale.Fit
                )
                
                // 返回按钮
                IconButton(
                    onClick = { fullScreenPhoto = null },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 32.dp, start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // 标题与上传时间
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.72f)
                                )
                            )
                        )
                        .padding(start = 24.dp, end = 24.dp, top = 72.dp, bottom = 42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (photo.caption.isNotEmpty()) {
                            Text(
                                text = photo.caption,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        Text(
                            text = uploadedAtLabel,
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumMosaic(
    photos: List<AlbumPhoto>,
    layoutMetrics: AlbumLayoutMetrics,
    onPhotoClick: (AlbumPhoto) -> Unit,
    onPhotoDelete: (AlbumPhoto) -> Unit,
    modifier: Modifier = Modifier
) {
    val daySections = remember(photos) { albumDaySections(photos) }

    LazyColumn(
        modifier = modifier.padding(horizontal = layoutMetrics.horizontalPaddingDp.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy((layoutMetrics.gridGapDp + 12f).dp)
    ) {
        itemsIndexed(
            items = daySections,
            key = { _, section -> section.date.toString() }
        ) { sectionIndex, section ->
            AlbumDayPhotoSection(
                section = section,
                sectionIndex = sectionIndex,
                layoutMetrics = layoutMetrics,
                onPhotoClick = onPhotoClick,
                onPhotoDelete = onPhotoDelete
            )
        }
    }
}

@Composable
private fun AlbumDayPhotoSection(
    section: AlbumDaySection,
    sectionIndex: Int,
    layoutMetrics: AlbumLayoutMetrics,
    onPhotoClick: (AlbumPhoto) -> Unit,
    onPhotoDelete: (AlbumPhoto) -> Unit
) {
    val groups = remember(section.photos) { section.photos.chunked(6) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AlbumDateHeader(title = section.title)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy((layoutMetrics.gridGapDp + 12f).dp)
        ) {
            groups.forEachIndexed { groupIndex, groupPhotos ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(layoutMetrics.gridGapDp.dp)
                ) {
                    val absoluteGroupIndex = sectionIndex + groupIndex
                    MosaicLeadRow(
                        photos = groupPhotos.take(3),
                        mirror = absoluteGroupIndex % 2 == 1,
                        gapDp = layoutMetrics.gridGapDp,
                        onPhotoClick = onPhotoClick,
                        onPhotoDelete = onPhotoDelete
                    )

                    groupPhotos.getOrNull(3)?.let { photo ->
                        PhotoCard(
                            photo = photo,
                            onClick = { onPhotoClick(photo) },
                            onDelete = { onPhotoDelete(photo) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.72f)
                        )
                    }

                    MosaicDuoRow(
                        photos = groupPhotos.drop(4).take(2),
                        gapDp = layoutMetrics.gridGapDp,
                        onPhotoClick = onPhotoClick,
                        onPhotoDelete = onPhotoDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumDateHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFFFB4A2))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = StandByUsLightColors.fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MosaicLeadRow(
    photos: List<AlbumPhoto>,
    mirror: Boolean,
    gapDp: Float,
    onPhotoClick: (AlbumPhoto) -> Unit,
    onPhotoDelete: (AlbumPhoto) -> Unit
) {
    if (photos.isEmpty()) return

    if (photos.size == 1) {
        PhotoCard(
            photo = photos[0],
            onClick = { onPhotoClick(photos[0]) },
            onDelete = { onPhotoDelete(photos[0]) },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.34f)
        )
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(236.dp),
        horizontalArrangement = Arrangement.spacedBy(gapDp.dp)
    ) {
        val bigCard: @Composable RowScope.() -> Unit = {
            val photo = photos[0]
            PhotoCard(
                photo = photo,
                onClick = { onPhotoClick(photo) },
                onDelete = { onPhotoDelete(photo) },
                modifier = Modifier
                    .weight(1.08f)
                    .fillMaxHeight()
            )
        }
        val smallStack: @Composable RowScope.() -> Unit = {
            Column(
                modifier = Modifier
                    .weight(0.92f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(gapDp.dp)
            ) {
                photos.drop(1).forEach { photo ->
                    PhotoCard(
                        photo = photo,
                        onClick = { onPhotoClick(photo) },
                        onDelete = { onPhotoDelete(photo) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }

        if (mirror) {
            smallStack()
            bigCard()
        } else {
            bigCard()
            smallStack()
        }
    }
}

@Composable
private fun MosaicDuoRow(
    photos: List<AlbumPhoto>,
    gapDp: Float,
    onPhotoClick: (AlbumPhoto) -> Unit,
    onPhotoDelete: (AlbumPhoto) -> Unit
) {
    if (photos.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gapDp.dp)
    ) {
        photos.forEachIndexed { index, photo ->
            PhotoCard(
                photo = photo,
                onClick = { onPhotoClick(photo) },
                onDelete = { onPhotoDelete(photo) },
                modifier = Modifier
                    .weight(if (index == 0) 0.94f else 1.06f)
                    .aspectRatio(if (index == 0) 0.92f else 1.12f)
                    .offset(y = if (index == 0) 8.dp else 0.dp)
            )
        }
        if (photos.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoCard(
    photo: AlbumPhoto,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(25.dp),
                ambientColor = Color(0x125A4A42),
                spotColor = Color(0x125A4A42)
            )
            .clip(RoundedCornerShape(25.dp))
            .background(Color(0xFFFFF0EB))
            .border(1.dp, Color(0x80FFFFFF), RoundedCornerShape(25.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showConfirm = true }
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.url)
                .crossfade(true)
                .build(),
            contentDescription = photo.caption,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(25.dp)),
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
                        shape = RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp)
                    )
            )
            Text(
                text = photo.caption,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0x66301E18))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
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
