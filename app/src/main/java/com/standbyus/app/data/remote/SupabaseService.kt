package com.standbyus.app.data.remote

import android.content.Context
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseService @Inject constructor() {

    private var cachedDeviceId: String? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build()
            chain.proceed(request)
        }
        .build()

    private val jsonType = "application/json".toMediaType()

    // 文件上传用（不同 Content-Type）
    private val uploadClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .build()
            chain.proceed(request)
        }
        .build()

    /** 上传文件到 Storage，返回公开 URL */
    suspend fun uploadFile(bucket: String, path: String, fileBytes: ByteArray, mimeType: String): String = withContext(Dispatchers.IO) {
        val mediaType = mimeType.toMediaTypeOrNull()
        val body = fileBytes.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${SupabaseConfig.SUPABASE_URL}/storage/v1/object/$bucket/$path")
            .post(body)
            .build()
        val response = uploadClient.newCall(request).execute()
        val bodyStr = response.body?.string() ?: throw IOException("上传空响应")
        if (!response.isSuccessful) {
            throw IOException("上传失败 (${response.code}): $bodyStr")
        }
        "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/public/$bucket/$path"
    }

    /** 获取/生成本地设备 ID */
    fun getDeviceId(context: Context): String {
        if (cachedDeviceId != null) return cachedDeviceId!!
        val prefs = context.getSharedPreferences("supabase_device", Context.MODE_PRIVATE)
        cachedDeviceId = prefs.getString("device_id", null)
        if (cachedDeviceId.isNullOrEmpty()) {
            cachedDeviceId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", cachedDeviceId).apply()
            Log.d(TAG, "Generated new device ID: $cachedDeviceId")
        }
        return cachedDeviceId!!
    }

    /** 获取已缓存的设备 ID */
    fun getCachedDeviceId(): String = cachedDeviceId ?: ""

    /** 创建记录 */
    suspend fun create(table: String, data: Map<String, Any>): Map<String, Any>? {
        val json = JSONObject(data as Map<*, *>).toString()
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}$table")
            .post(json.toRequestBody(jsonType))
            .build()
        return executeAndParse(request).firstOrNull()
    }

    /** 更新记录 */
    suspend fun update(table: String, query: String, data: Map<String, Any>) {
        val json = JSONObject(data as Map<*, *>).toString()
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}$table?$query")
            .patch(json.toRequestBody(jsonType))
            .build()
        executeAndParse(request)
    }

    /** 删除 Storage 文件 */
    suspend fun deleteFile(bucket: String, path: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${SupabaseConfig.SUPABASE_URL}/storage/v1/object/$bucket/$path")
            .delete()
            .build()
        val response = uploadClient.newCall(request).execute()
        if (!response.isSuccessful && response.code != 404) {
            val bodyStr = response.body?.string() ?: ""
            throw IOException("删除文件失败 (${response.code}): $bodyStr")
        }
    }

    /** 删除记录 */
    suspend fun delete(table: String, query: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}$table?$query")
            .delete()
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val bodyStr = response.body?.string() ?: ""
            throw IOException("Supabase 删除错误 (${response.code}): $bodyStr")
        }
    }

    /** 查询记录 */
    suspend fun query(table: String, query: String): List<Map<String, Any>> {
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}$table?$query")
            .get()
            .build()
        return executeAndParse(request)
    }

    private suspend fun executeAndParse(request: Request): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        val response = client.newCall(request).execute()
        val bodyStr = response.body?.string() ?: throw IOException("空响应")
        if (!response.isSuccessful) {
            throw IOException("Supabase 错误 (${response.code}): $bodyStr")
        }
        parseJsonArray(bodyStr)
    }

    private fun parseJsonArray(json: String): List<Map<String, Any>> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            obj.keys().asSequence().associate { key ->
                key to parseValue(obj.get(key))
            }
        }
    }

    private fun parseValue(value: Any): Any = when {
        value is Int || value is Long -> value
        value is Double -> {
            val l = value.toLong()
            if (l.toDouble() == value) l else value
        }
        value is JSONObject -> "{}"
        value is JSONArray -> "[]"
        else -> value
    }

    companion object {
        private const val TAG = "StandBySupabase"
    }
}
