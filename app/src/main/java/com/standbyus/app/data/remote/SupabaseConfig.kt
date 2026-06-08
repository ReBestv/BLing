package com.standbyus.app.data.remote

import com.standbyus.app.BuildConfig

/**
 * Supabase 配置
 *
 * 1. 注册 Supabase: https://supabase.com
 * 2. 创建项目
 * 3. 在项目 Settings → API 中找到 Project URL 和 anon/public key
 * 4. 在本地 local.properties 中配置 SUPABASE_URL 和 SUPABASE_ANON_KEY
 */
object SupabaseConfig {
    val SUPABASE_URL: String = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
    val ANON_KEY: String = BuildConfig.SUPABASE_ANON_KEY.trim()

    val REST_URL: String = "$SUPABASE_URL/rest/v1/"

    // Supabase Storage public URL for publicly readable theme and sticker assets.
    val STORAGE_URL: String = "$SUPABASE_URL/storage/v1/object/public/"

    fun isConfigured(): Boolean {
        return SUPABASE_URL != "https://example.supabase.co" && ANON_KEY.isNotBlank()
    }
}
