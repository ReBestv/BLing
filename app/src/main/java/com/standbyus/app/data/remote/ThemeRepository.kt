package com.standbyus.app.data.remote

import android.content.Context
import android.util.Log
import com.standbyus.app.data.model.ThemeManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 负责从 Supabase Storage 拉取 themes/manifest.json，
 * 解析为 [ThemeManifest]，缓存到 SharedPreferences。
 */
@Singleton
class ThemeRepository @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val manifestUrl =
        "${SupabaseConfig.STORAGE_URL}themes/manifest.json"

    companion object {
        private const val TAG = "ThemeRepository"
        private const val PREFS_NAME = "theme_manifest_cache"
        private const val KEY_MANIFEST_JSON = "manifest_json"
    }

    /**
     * 获取主题清单。优先从网络拉取，失败则用缓存兜底。
     * 返回 null 表示网络失败且无缓存（首次安装）。
     */
    suspend fun fetchManifest(context: Context): ThemeManifest? {
        return withContext(Dispatchers.IO) {
            try {
                val json = fetchFromNetwork()
                cacheManifest(context, json)
                ThemeManifest.fromJson(json)
            } catch (e: IOException) {
                Log.w(TAG, "Failed to fetch manifest, using cache", e)
                getCachedManifest(context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse manifest", e)
                getCachedManifest(context)
            }
        }
    }

    private fun fetchFromNetwork(): String {
        val request = Request.Builder()
            .url(manifestUrl)
            .get()
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: ${response.message}")
        }
        return response.body?.string()
            ?: throw IOException("Empty response body")
    }

    private fun cacheManifest(context: Context, json: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_MANIFEST_JSON, json).apply()
    }

    private fun getCachedManifest(context: Context): ThemeManifest? {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MANIFEST_JSON, null) ?: return null
        return try {
            ThemeManifest.fromJson(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse cached manifest", e)
            null
        }
    }
}
