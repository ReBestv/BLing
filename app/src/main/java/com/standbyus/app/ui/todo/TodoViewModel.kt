package com.standbyus.app.ui.todo

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.TodoItem
import com.standbyus.app.data.model.TodoList
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val supabaseService: SupabaseService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("pairing", Context.MODE_PRIVATE)
    private val myDeviceId by lazy { supabaseService.getDeviceId(context) }
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    private var nextLocalId = -1L

    init {
        refreshPairingState()
    }

    fun refreshPairingState() {
        val pairId = prefs.getString("pair_id", null)
        if (pairId.isNullOrEmpty()) {
            _uiState.value = TodoUiState(isLoading = false)
            return
        }

        _uiState.update {
            it.copy(isPaired = true, isLoading = it.lists.isEmpty(), error = null)
        }
        loadLists()
    }

    fun refresh() {
        refreshPairingState()
    }

    fun loadLists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val lists = todoRepository.getLists()
                val currentListId = _uiState.value.currentListId?.takeIf { id ->
                    lists.any { it.id == id }
                } ?: lists.firstOrNull()?.id
                val items = if (currentListId != null && currentListId > 0) {
                    todoRepository.getItems(currentListId)
                } else {
                    emptyList()
                }
                val currentList = lists.firstOrNull { it.id == currentListId }
                _uiState.update {
                    it.copy(
                        lists = lists,
                        currentListId = currentListId,
                        items = items,
                        isLoading = false,
                        canEditCurrentList = canEditList(currentList)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadLists failed", e)
                _uiState.update {
                    it.copy(
                        error = "加载清单失败：${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectList(listId: Long) {
        if (listId == _uiState.value.currentListId) return
        _uiState.update {
            val currentList = it.lists.firstOrNull { list -> list.id == listId }
            it.copy(
                currentListId = listId,
                items = emptyList(),
                isLoading = true,
                error = null,
                canEditCurrentList = canEditList(currentList)
            )
        }
        viewModelScope.launch {
            try {
                val items = todoRepository.getItems(listId)
                _uiState.update {
                    val currentList = it.lists.firstOrNull { list -> list.id == listId }
                    if (it.currentListId != listId) {
                        it
                    } else {
                        it.copy(
                            items = items,
                            isLoading = false,
                            canEditCurrentList = canEditList(currentList)
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "selectList failed", e)
                _uiState.update {
                    if (it.currentListId != listId) {
                        it
                    } else {
                        it.copy(
                            error = "加载任务失败：${e.message}",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun createList(name: String, isShared: Boolean) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        val previousState = _uiState.value
        val localId = newLocalId()
        val optimisticList = TodoList(
            id = localId,
            name = trimmedName,
            ownerId = myDeviceId,
            isShared = isShared,
            createdAt = System.currentTimeMillis()
        )

        _uiState.update {
            it.copy(
                lists = listOf(optimisticList) + it.lists,
                currentListId = localId,
                items = emptyList(),
                syncingListIds = it.syncingListIds + localId,
                error = null,
                canEditCurrentList = true
            )
        }

        viewModelScope.launch {
            try {
                val result = todoRepository.createList(trimmedName, isShared)
                if (result == null) {
                    rollbackCreateList(
                        localId = localId,
                        previousListId = previousState.currentListId,
                        previousItems = previousState.items,
                        message = "创建清单失败，请确认 Supabase 已创建 todo_lists 和 todo_items，并允许当前匿名访问。"
                    )
                    return@launch
                }

                _uiState.update { state ->
                    val updatedLists = state.lists.map { list ->
                        if (list.id == localId) result else list
                    }
                    state.copy(
                        lists = updatedLists,
                        currentListId = if (state.currentListId == localId) result.id else state.currentListId,
                        syncingListIds = state.syncingListIds - localId,
                        canEditCurrentList = canEditList(
                            updatedLists.firstOrNull { list ->
                                list.id == if (state.currentListId == localId) result.id else state.currentListId
                            }
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "createList failed", e)
                rollbackCreateList(
                    localId = localId,
                    previousListId = previousState.currentListId,
                    previousItems = previousState.items,
                    message = "创建清单失败：${e.message}"
                )
            }
        }
    }

    fun deleteList(id: Long) {
        val list = _uiState.value.lists.firstOrNull { it.id == id }
        if (!canEditList(list)) {
            _uiState.update { it.copy(error = personalListReadOnlyMessage) }
            return
        }
        viewModelScope.launch {
            try {
                todoRepository.deleteList(id)
                val currentId = _uiState.value.currentListId
                if (currentId == id) {
                    _uiState.update { it.copy(currentListId = null, items = emptyList()) }
                }
                loadLists()
            } catch (e: Exception) {
                Log.e(TAG, "deleteList failed", e)
                _uiState.update {
                    it.copy(error = "删除清单失败：${e.message}")
                }
            }
        }
    }

    fun addItem(title: String, note: String = "") {
        val listId = _uiState.value.currentListId ?: return
        if (listId <= 0) {
            _uiState.update {
                it.copy(error = "清单还在创建中，请稍等一下再添加任务。")
            }
            return
        }
        if (!canEditCurrentList()) {
            _uiState.update { it.copy(error = personalListReadOnlyMessage) }
            return
        }

        val trimmedTitle = title.trim()
        val trimmedNote = note.trim()
        if (trimmedTitle.isBlank()) return

        val localId = newLocalId()
        val optimisticItem = TodoItem(
            id = localId,
            listId = listId,
            title = trimmedTitle,
            note = trimmedNote,
            createdAt = System.currentTimeMillis()
        )

        _uiState.update {
            it.copy(
                items = listOf(optimisticItem) + it.items,
                syncingItemIds = it.syncingItemIds + localId,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val result = todoRepository.createItem(listId, trimmedTitle, trimmedNote)
                if (result == null) {
                    rollbackCreateItem(localId, "添加任务失败，请确认 Supabase 已创建 todo_items，并允许当前匿名访问。")
                    return@launch
                }

                _uiState.update { state ->
                    val updatedItems = state.items.map { item ->
                        if (item.id == localId) result else item
                    }
                    state.copy(
                        items = updatedItems,
                        syncingItemIds = state.syncingItemIds - localId
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "addItem failed", e)
                rollbackCreateItem(localId, "添加任务失败：${e.message}")
            }
        }
    }

    fun toggleItem(item: TodoItem) {
        if (item.id <= 0 || item.id in _uiState.value.syncingItemIds) return
        if (!canEditCurrentList()) {
            _uiState.update { it.copy(error = personalListReadOnlyMessage) }
            return
        }

        val toggled = item.copy(
            isDone = !item.isDone,
            doneAt = if (!item.isDone) System.currentTimeMillis() else null
        )

        _uiState.update { state ->
            state.copy(
                items = state.items.map { current ->
                    if (current.id == item.id) toggled else current
                },
                syncingItemIds = state.syncingItemIds + item.id,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val success = todoRepository.toggleItem(item.id, toggled.isDone)
                if (!success) {
                    throw IllegalStateException("网络同步未成功")
                }
                _uiState.update { state ->
                    state.copy(syncingItemIds = state.syncingItemIds - item.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "toggleItem failed", e)
                _uiState.update { state ->
                    state.copy(
                        items = state.items.map { current ->
                            if (current.id == item.id) item else current
                        },
                        syncingItemIds = state.syncingItemIds - item.id,
                        error = "更新任务状态失败：${e.message}"
                    )
                }
            }
        }
    }

    fun deleteItem(id: Long) {
        if (id <= 0 || id in _uiState.value.syncingItemIds) return
        if (!canEditCurrentList()) {
            _uiState.update { it.copy(error = personalListReadOnlyMessage) }
            return
        }

        val snapshot = _uiState.value
        val removedIndex = snapshot.items.indexOfFirst { it.id == id }
        val removedItem = snapshot.items.getOrNull(removedIndex) ?: return

        _uiState.update {
            it.copy(
                items = it.items.filterNot { item -> item.id == id },
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val success = todoRepository.deleteItem(id)
                if (!success) {
                    throw IllegalStateException("网络同步未成功")
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteItem failed", e)
                _uiState.update { state ->
                    val restoredItems = state.items.toMutableList().apply {
                        add(removedIndex.coerceIn(0, size), removedItem)
                    }
                    state.copy(
                        items = restoredItems,
                        error = "删除任务失败：${e.message}"
                    )
                }
            }
        }
    }

    fun showAddListDialog(show: Boolean) {
        _uiState.update { it.copy(showAddListDialog = show) }
    }

    fun showAddItemSheet(show: Boolean) {
        if (show && !canEditCurrentList()) {
            _uiState.update { it.copy(error = personalListReadOnlyMessage) }
            return
        }
        _uiState.update { it.copy(showAddItemSheet = show) }
    }

    fun setEditingItem(item: TodoItem?) {
        _uiState.update { it.copy(editingItem = item) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun rollbackCreateList(
        localId: Long,
        previousListId: Long?,
        previousItems: List<TodoItem>,
        message: String
    ) {
        _uiState.update { state ->
            val updatedLists = state.lists.filterNot { it.id == localId }
            val fallbackListId = when {
                previousListId != null && updatedLists.any { it.id == previousListId } -> previousListId
                else -> updatedLists.firstOrNull()?.id
            }
            state.copy(
                lists = updatedLists,
                currentListId = fallbackListId,
                items = if (fallbackListId == previousListId) previousItems else emptyList(),
                syncingListIds = state.syncingListIds - localId,
                canEditCurrentList = canEditList(updatedLists.firstOrNull { it.id == fallbackListId }),
                error = message
            )
        }
    }

    private fun currentList(state: TodoUiState = _uiState.value): TodoList? {
        val currentListId = state.currentListId ?: return null
        return state.lists.firstOrNull { it.id == currentListId }
    }

    private fun canEditCurrentList(): Boolean = canEditList(currentList())

    private fun canEditList(list: TodoList?): Boolean {
        if (list == null) return false
        return list.isShared || list.ownerId == myDeviceId
    }

    private fun rollbackCreateItem(localId: Long, message: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.filterNot { it.id == localId },
                syncingItemIds = state.syncingItemIds - localId,
                error = message
            )
        }
    }

    private fun newLocalId(): Long = nextLocalId--

    companion object {
        private const val TAG = "TodoViewModel"
        private const val personalListReadOnlyMessage = "这是对方的个人清单，只有对方可以编辑"
    }
}

data class TodoUiState(
    val isPaired: Boolean = false,
    val lists: List<TodoList> = emptyList(),
    val currentListId: Long? = null,
    val items: List<TodoItem> = emptyList(),
    val isLoading: Boolean = true,
    val showAddListDialog: Boolean = false,
    val showAddItemSheet: Boolean = false,
    val editingItem: TodoItem? = null,
    val syncingItemIds: Set<Long> = emptySet(),
    val syncingListIds: Set<Long> = emptySet(),
    val canEditCurrentList: Boolean = false,
    val error: String? = null
)
