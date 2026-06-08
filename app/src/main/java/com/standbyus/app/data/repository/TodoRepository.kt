package com.standbyus.app.data.repository

import android.content.Context
import android.util.Log
import com.standbyus.app.data.local.TodoItemDao
import com.standbyus.app.data.local.TodoListDao
import com.standbyus.app.data.local.toEntity
import com.standbyus.app.data.local.toTodoItem
import com.standbyus.app.data.local.toTodoList
import com.standbyus.app.data.model.TodoItem
import com.standbyus.app.data.model.TodoList
import com.standbyus.app.data.remote.SupabaseService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val supabaseService: SupabaseService,
    private val todoListDao: TodoListDao,
    private val todoItemDao: TodoItemDao,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TodoRepo"
        private const val TABLE_LISTS = "todo_lists"
        private const val TABLE_ITEMS = "todo_items"
    }

    private val pairingPrefs by lazy {
        context.getSharedPreferences("pairing", Context.MODE_PRIVATE)
    }

    private fun getPairId(): String = pairingPrefs.getString("pair_id", "") ?: ""
    private fun getDeviceId(): String = supabaseService.getDeviceId(context)

    suspend fun getLists(): List<TodoList> {
        val pairId = getPairId()
        if (pairId.isEmpty()) {
            Log.e(TAG, "getLists: no pairId")
            return emptyList()
        }
        return try {
            val results = supabaseService.query(
                TABLE_LISTS,
                "pair_id=eq.$pairId&order=sort_order.desc"
            )
            val lists = results.map { TodoList.fromMap(it) }
            lists.forEach { todoListDao.upsertList(it.toEntity()) }
            Log.d(TAG, "getLists: ${lists.size} lists from Supabase")
            lists
        } catch (e: Exception) {
            Log.e(TAG, "getLists remote failed: ${e.message}")
            todoListDao.getListsByPairId(pairId).map { it.toTodoList() }
        }
    }

    suspend fun createList(name: String, isShared: Boolean): TodoList? {
        val pairId = getPairId()
        val deviceId = getDeviceId()
        val createdAt = System.currentTimeMillis()
        Log.d(TAG, "createList: pairId=$pairId, deviceId=$deviceId, name=$name")
        if (pairId.isEmpty() || deviceId.isEmpty()) {
            Log.e(TAG, "createList: missing pairId or deviceId")
            return null
        }

        val data = TodoList(
            pairId = pairId,
            name = name,
            ownerId = deviceId,
            isShared = isShared,
            createdAt = createdAt
        )

        return try {
            val created = supabaseService.create(TABLE_LISTS, data.toMap())
                ?: runCatching {
                    supabaseService.query(
                        TABLE_LISTS,
                        "pair_id=eq.$pairId&owner_id=eq.$deviceId&created_at=eq.$createdAt&order=id.desc&limit=1"
                    ).firstOrNull()
                }.getOrNull()

            if (created == null) {
                Log.e(TAG, "createList: insert finished but no row could be read back from $TABLE_LISTS")
                return null
            }

            val list = TodoList.fromMap(created)
            todoListDao.upsertList(list.toEntity())
            Log.d(TAG, "createList success: id=${list.id}, name=${list.name}")
            list
        } catch (e: Exception) {
            Log.e(TAG, "createList failed: ${e.message}", e)
            null
        }
    }

    suspend fun deleteList(id: Long): Boolean {
        return try {
            supabaseService.delete(TABLE_LISTS, "id=eq.$id")
            todoListDao.deleteList(id)
            true
        } catch (e: Exception) {
            Log.e(TAG, "deleteList failed: ${e.message}", e)
            false
        }
    }

    suspend fun getItems(listId: Long): List<TodoItem> {
        return try {
            val results = supabaseService.query(
                TABLE_ITEMS,
                "list_id=eq.$listId&order=sort_order.desc"
            )
            val items = results.map { TodoItem.fromMap(it) }
            items.forEach { todoItemDao.upsertItem(it.toEntity()) }
            Log.d(TAG, "getItems: ${items.size} items for list $listId")
            items
        } catch (e: Exception) {
            Log.e(TAG, "getItems remote failed: ${e.message}")
            todoItemDao.getItemsByListId(listId).map { it.toTodoItem() }
        }
    }

    suspend fun createItem(listId: Long, title: String, note: String): TodoItem? {
        val deviceId = getDeviceId()
        val createdAt = System.currentTimeMillis()
        if (deviceId.isEmpty()) {
            Log.e(TAG, "createItem: no deviceId")
            return null
        }

        val data = TodoItem(
            listId = listId,
            title = title,
            note = note,
            ownerId = deviceId,
            createdAt = createdAt
        )

        return try {
            val created = supabaseService.create(TABLE_ITEMS, data.toMap())
                ?: runCatching {
                    supabaseService.query(
                        TABLE_ITEMS,
                        "list_id=eq.$listId&owner_id=eq.$deviceId&created_at=eq.$createdAt&order=id.desc&limit=1"
                    ).firstOrNull()
                }.getOrNull()

            val item = if (created != null) {
                TodoItem.fromMap(created)
            } else {
                Log.e(TAG, "createItem: insert finished but no row could be read back from $TABLE_ITEMS")
                return null
            }

            todoItemDao.upsertItem(item.toEntity())
            Log.d(TAG, "createItem success: ${item.title}")
            item
        } catch (e: Exception) {
            Log.e(TAG, "createItem failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun toggleItem(id: Long, isDone: Boolean): Boolean {
        val deviceId = getDeviceId()
        val updateData = mutableMapOf<String, Any>(
            "is_done" to isDone
        )
        if (isDone) {
            updateData["done_by"] = deviceId
            updateData["done_at"] = System.currentTimeMillis()
        }

        return try {
            supabaseService.update(TABLE_ITEMS, "id=eq.$id", updateData)
            val results = supabaseService.query(TABLE_ITEMS, "id=eq.$id")
            if (results.isNotEmpty()) {
                todoItemDao.upsertItem(TodoItem.fromMap(results.first()).toEntity())
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "toggleItem failed: ${e.message}", e)
            false
        }
    }

    suspend fun deleteItem(id: Long): Boolean {
        return try {
            supabaseService.delete(TABLE_ITEMS, "id=eq.$id")
            todoItemDao.deleteItem(id)
            true
        } catch (e: Exception) {
            Log.e(TAG, "deleteItem failed: ${e.message}", e)
            false
        }
    }
}
