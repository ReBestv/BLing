package com.standbyus.app.data.remote

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 贴纸管理：上传图片到 Supabase Storage，返回公开 URL
 *
 * 使用前需在 Supabase 控制台 SQL 编辑器运行：
 *
 *   INSERT INTO storage.buckets (id, name, public)
 *   VALUES ('stickers', 'stickers', true)
 *   ON CONFLICT (id) DO NOTHING;
 *
 *   CREATE POLICY "public_read" ON storage.objects
 *   FOR SELECT USING (bucket_id = 'stickers');
 *
 *   CREATE POLICY "public_insert" ON storage.objects
 *   FOR INSERT WITH CHECK (bucket_id = 'stickers');
 */
@Singleton
class StickerManager @Inject constructor(
    private val supabaseService: SupabaseService
) {
    companion object {
        private const val BUCKET = "stickers"
    }

    /** 上传图片，返回公开 URL */
    suspend fun uploadSticker(context: Context, imageUri: Uri, feelingName: String): String {
        val bytes = context.contentResolver.openInputStream(imageUri)?.readBytes()
            ?: throw IllegalArgumentException("无法读取图片")
        val ext = getExtension(context, imageUri)
        val fileName = "${feelingName}_${UUID.randomUUID().toString().take(8)}.$ext"
        val mime = when (ext.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "image/png"
        }
        return supabaseService.uploadFile(BUCKET, fileName, bytes, mime)
    }

    private fun getExtension(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val name = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                name.substringAfterLast('.', "png")
            } else "png"
        } ?: "png"
    }
}
