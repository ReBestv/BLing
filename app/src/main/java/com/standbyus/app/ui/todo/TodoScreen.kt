package com.standbyus.app.ui.todo

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.standbyus.app.data.model.TodoItem
import com.standbyus.app.data.model.TodoList
import com.standbyus.app.ui.components.AppHeader

private val BgColor = Color(0xFFFFF8F5)
private val SurfaceColor = Color(0xFFFFFFFF)
private val PrimaryColor = Color(0xFFFF7E67)
private val PrimarySoft = Color(0xFFFFEEE7)
private val TextPrimary = Color(0xFF5A4A42)
private val TextSecondary = Color(0xFF9E8E86)
private val CompletedColor = Color(0xFF9E8E86)
private val DoneCheckColor = Color(0xFF4CAF50)

@Composable
fun TodoScreen(
    onBack: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

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

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(
                title = "TODO",
                rightIcon = {
                    IconButton(onClick = { viewModel.showAddListDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "新建清单",
                            tint = PrimaryColor
                        )
                    }
                }
            )

            when {
                !uiState.isPaired -> UnpairedPlaceholder()
                uiState.isLoading && uiState.lists.isEmpty() -> LoadingState()
                uiState.lists.isEmpty() -> EmptyListsPlaceholder(
                    onCreateList = { viewModel.showAddListDialog(true) }
                )
                else -> {
                    val currentList = uiState.lists.firstOrNull { it.id == uiState.currentListId }
                    Column(modifier = Modifier.fillMaxSize()) {
                        ListTabs(
                            lists = uiState.lists,
                            currentListId = uiState.currentListId,
                            syncingListIds = uiState.syncingListIds,
                            onSelectList = { viewModel.selectList(it) },
                            onDeleteList = { viewModel.deleteList(it.id) }
                        )

                        currentList?.let { list ->
                            ListPermissionBanner(
                                list = list,
                                canEdit = uiState.canEditCurrentList
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            TaskList(
                                items = uiState.items,
                                isLoading = uiState.isLoading,
                                syncingItemIds = uiState.syncingItemIds,
                                canEdit = uiState.canEditCurrentList,
                                onToggle = { viewModel.toggleItem(it) },
                                onDelete = { viewModel.deleteItem(it.id) }
                            )
                        }

                        AddTaskButton(
                            enabled = (uiState.currentListId ?: 0L) > 0L && uiState.canEditCurrentList,
                            onClick = { viewModel.showAddItemSheet(true) }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }

    if (uiState.showAddListDialog) {
        AddListDialog(
            onDismiss = { viewModel.showAddListDialog(false) },
            onConfirm = { name, isShared ->
                viewModel.createList(name, isShared)
                viewModel.showAddListDialog(false)
            }
        )
    }

    if (uiState.showAddItemSheet) {
        AddItemDialog(
            onDismiss = { viewModel.showAddItemSheet(false) },
            onConfirm = { title, note ->
                viewModel.addItem(title, note)
                viewModel.showAddItemSheet(false)
            }
        )
    }
}

@Composable
private fun ListPermissionBanner(
    list: TodoList,
    canEdit: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!list.isShared) {
            FilterChip(
                selected = true,
                onClick = {},
                enabled = false,
                label = { Text(formatOwnerBadge(list.ownerId)) },
                colors = FilterChipDefaults.filterChipColors(
                    disabledContainerColor = Color(0xFFF5F0ED),
                    disabledLabelColor = TextSecondary
                )
            )
        }
        FilterChip(
            selected = true,
            onClick = {},
            enabled = false,
            label = {
                Text(if (list.isShared) "共同清单" else "个人清单")
            },
            colors = FilterChipDefaults.filterChipColors(
                disabledContainerColor = PrimarySoft,
                disabledLabelColor = PrimaryColor
            )
        )
        if (!canEdit) {
            FilterChip(
                selected = true,
                onClick = {},
                enabled = false,
                label = { Text("仅对方可编辑") },
                colors = FilterChipDefaults.filterChipColors(
                    disabledContainerColor = Color(0xFFF5F0ED),
                    disabledLabelColor = TextSecondary
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}

private fun formatOwnerBadge(ownerId: String): String {
    val normalized = ownerId.filter { it.isLetterOrDigit() }.uppercase()
    return if (normalized.length >= 4) {
        "ID ${normalized.takeLast(4)}"
    } else {
        "ID ${normalized.ifBlank { "----" }}"
    }
}

@Composable
private fun UnpairedPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "请先绑定伴侣",
            fontSize = 20.sp,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "绑定后就能和 TA 一起管理待办清单",
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = PrimaryColor, strokeWidth = 2.5.dp)
            Text(
                text = "正在同步清单…",
                color = TextSecondary,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun EmptyListsPlaceholder(
    onCreateList: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "还没有待办清单",
            fontSize = 22.sp,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "创建第一个清单开始管理任务吧",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = onCreateList,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text("创建清单")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListTabs(
    lists: List<TodoList>,
    currentListId: Long?,
    syncingListIds: Set<Long>,
    onSelectList: (Long) -> Unit,
    onDeleteList: (TodoList) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var pendingDeleteList by remember(lists.map { it.id }) { mutableStateOf<TodoList?>(null) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(lists, key = { it.id }) { list ->
            val isSelected = list.id == currentListId
            val isSyncing = list.id in syncingListIds
            val shareEmoji = if (list.isShared) "👫" else "🧍"

            Card(
                modifier = Modifier
                    .animateContentSize()
                    .combinedClickable(
                        enabled = !isSyncing,
                        onClick = { onSelectList(list.id) },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            pendingDeleteList = list
                        }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) PrimaryColor else SurfaceColor
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = shareEmoji, fontSize = 14.sp)
                    Text(
                        text = list.name,
                        fontSize = 14.sp,
                        color = if (isSelected) Color.White else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = if (isSelected) Color.White else PrimaryColor,
                            strokeWidth = 1.6.dp
                        )
                    }
                }
            }
        }
    }

    pendingDeleteList?.let { list ->
        AlertDialog(
            onDismissRequest = { pendingDeleteList = null },
            title = { Text("删除清单") },
            text = { Text("确定要删除“${list.name}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteList = null
                        onDeleteList(list)
                    }
                ) {
                    Text("删除", color = PrimaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteList = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun TaskList(
    items: List<TodoItem>,
    isLoading: Boolean,
    syncingItemIds: Set<Long>,
    canEdit: Boolean,
    onToggle: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit
) {
    var pendingDeleteItem by remember(items.map { it.id }) { mutableStateOf<TodoItem?>(null) }

    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PrimaryColor,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "正在加载任务…",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            } else {
                Text(
                    text = "还没有任务，马上添加一个吧",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { it.id }) { item ->
                TaskItemRow(
                    item = item,
                    isSyncing = item.id in syncingItemIds,
                    canEdit = canEdit,
                    onToggle = { onToggle(item) },
                    onLongPressDelete = { pendingDeleteItem = item }
                )
            }
        }
    }

    pendingDeleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDeleteItem = null },
            title = { Text("删除任务") },
            text = { Text("确定要删除“${item.title}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteItem = null
                        onDelete(item)
                    }
                ) {
                    Text("删除", color = PrimaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteItem = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskItemRow(
    item: TodoItem,
    isSyncing: Boolean,
    canEdit: Boolean,
    onToggle: () -> Unit,
    onLongPressDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val alpha by animateFloatAsState(
        targetValue = when {
            isSyncing -> 0.72f
            !canEdit -> 0.78f
            item.isDone -> 0.6f
            else -> 1f
        },
        label = "taskAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .alpha(alpha)
            .combinedClickable(
                enabled = !isSyncing && canEdit,
                onClick = onToggle,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPressDelete()
                }
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 84.dp)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            item.isDone -> DoneCheckColor
                            canEdit -> PrimarySoft
                            else -> Color(0xFFF5F0ED)
                        }
                    )
                    .clickable(enabled = !isSyncing && canEdit) { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = if (item.isDone) Color.White else PrimaryColor,
                        strokeWidth = 1.8.dp
                    )
                } else if (item.isDone) {
                    Text("✓", color = Color.White, fontSize = 13.sp)
                }
            }

            Text(
                text = item.title,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                color = if (item.isDone) CompletedColor else TextPrimary,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            when {
                isSyncing -> {
                    Text(
                        text = "同步中",
                        fontSize = 11.sp,
                        color = PrimaryColor
                    )
                }

                !canEdit -> {
                    Text(
                        text = "只读",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                item.note.isNotBlank() -> {
                    Text(
                        text = "备注",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTaskButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(56.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0x145A4A42),
                spotColor = Color(0x145A4A42)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryColor,
            contentColor = Color.White,
            disabledContainerColor = PrimaryColor.copy(alpha = 0.58f),
            disabledContentColor = Color.White.copy(alpha = 0.82f)
        )
    ) {
        Text(
            text = "💪",
            fontSize = 18.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (enabled) "添加任务" else "清单创建中…")
    }
}

@Composable
private fun AddListDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isShared by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建清单") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("清单名称") },
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("与伴侣共享")
                    Switch(
                        checked = isShared,
                        onCheckedChange = { isShared = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, isShared) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加任务") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任务名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, note) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
