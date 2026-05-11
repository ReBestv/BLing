package com.standbyus.app.data.remote

/**
 * Supabase 配置
 *
 * 1. 注册 Supabase: https://supabase.com
 * 2. 创建项目
 * 3. 在项目 Settings → API 中找到 Project URL 和 anon/public key
 * 4. 替换下面两个值
 */
object SupabaseConfig {
    // ========== 请替换为你的 Supabase 项目信息 ==========
    const val SUPABASE_URL = "https://dxwnnskelbygqdvjlorj.supabase.co"
    const val ANON_KEY = "sb_publishable_6nPBkQliN6I3cmDMWa5EGg_2x4sE-yZ"
    // =============================================

    const val REST_URL = "$SUPABASE_URL/rest/v1/"
}
