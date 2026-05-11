package com.standbyus.app.ui.album

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.standbyus.app.data.model.AlbumPhoto
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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
                TopAppBar(
                    title = { Text("我们的故事", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        if (!uploading) {
                            IconButton(onClick = {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }) {
                                Text("＋", fontSize = 22.sp, fontWeight = FontWeight.Light)
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                // 时间筛选
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(0 to "全部", 7 to "最近7天", 30 to "最近30天")
                    filters.forEach { (days, label) ->
                        FilterChip(
                            selected = filterDays == days,
                            onClick = { viewModel.setFilter(days) },
                            label = { Text(label, fontSize = 12.sp) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (photos.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🖼️", fontSize = 64.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("还没有照片", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text("点击右上角 ＋ 上传第一张", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(photos, key = { it.id }) { photo ->
                            PhotoCard(
                                photo = photo,
                                onDelete = { viewModel.deletePhoto(photo) }
                            )
                        }
                    }
                }
            }
        }

        // 上传底部弹窗
        if (showSheet && selectedUri != null) {
            Box(
                modifier = Modifier.fillMaxSize().clickable { viewModel.dismissSheet() }
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f))
            )
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp).height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                    Spacer(Modifier.height(16.dp))
                    // 预览图
                    AsyncImage(
                        model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(selectedUri).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { viewModel.updateCaption(it) },
                        placeholder = { Text("写一句小记（可选）") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    if (error.isNotEmpty()) {
                        Text(error, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                    }
                    Button(
                        onClick = { viewModel.upload() },
                        enabled = !uploading,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.standbyus.app.ui.theme.Primary
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (uploading) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = androidx.compose.ui.graphics.Color.White)
                        } else {
                            Text("发布到我们的故事", fontWeight = FontWeight.SemiBold)
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
    val sdf = remember { SimpleDateFormat("M月d日", Locale.CHINESE) }
    val dateStr = remember(photo.createdAt) { sdf.format(Date(photo.createdAt)) }
    var showConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.clip(RoundedCornerShape(14.dp))) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 2.dp,
            modifier = Modifier.combinedClickable(
                onClick = { },
                onLongClick = { showConfirm = true }
            )
        ) {
            Column {
                AsyncImage(
                    model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(photo.url).crossfade(true).build(),
                    contentDescription = photo.caption,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
                if (photo.caption.isNotEmpty()) {
                    Text(
                        text = photo.caption,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 8.dp)
                )
            }
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
