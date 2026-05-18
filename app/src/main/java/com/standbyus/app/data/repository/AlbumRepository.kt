package com.standbyus.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.standbyus.app.data.model.AlbumPhoto
import com.standbyus.app.data.remote.SupabaseService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumRepository @Inject constructor(
    private val supabaseService: SupabaseService,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "StandByAlbumRepo"
        private const val BUCKET = "our-story"
        private const val TABLE = "photos"
        private const val MAX_WIDTH = 1920
        private const val COMPRESS_QUALITY = 80
    }

    /** 上传照片 */
    suspend fun uploadPhoto(uri: Uri, caption: String, deviceId: String): AlbumPhoto {
        // 读取并压缩
        val bytes = compressImage(uri)
        val timestamp = System.currentTimeMillis()
        val path = "$deviceId/$timestamp.jpg"

        // 上传到 Storage
        val url = supabaseService.uploadFile(BUCKET, path, bytes, "image/jpeg")
        Log.d(TAG, "Uploaded: $url")

        // 写入数据库
        val data = mapOf(
            "deviceId" to deviceId,
            "url" to url,
            "caption" to caption,
            "createdAt" to timestamp
        )
        supabaseService.create(TABLE, data)
        return AlbumPhoto(deviceId = deviceId, url = url, caption = caption, createdAt = timestamp)
    }

    /** 获取双方照片列表 */
    suspend fun getPhotos(myDeviceId: String, partnerId: String, sinceTimestamp: Long = 0L): List<AlbumPhoto> {
        val timeFilter = if (sinceTimestamp > 0L) "&createdAt=gte.$sinceTimestamp" else ""
        val myPhotos = supabaseService.query(
            TABLE,
            "deviceId=eq.$myDeviceId$timeFilter&order=createdAt.desc&limit=50"
        )
        val partnerPhotos = supabaseService.query(
            TABLE,
            "deviceId=eq.$partnerId$timeFilter&order=createdAt.desc&limit=50"
        )
        return (myPhotos + partnerPhotos).mapNotNull { raw ->
            try { AlbumPhoto.fromMap(raw) } catch (_: Exception) { null }
        }.sortedByDescending { it.createdAt }
    }

    /** 删除照片 */
    suspend fun deletePhoto(photo: AlbumPhoto) {
        // 从 Storage 删除文件
        val path = photo.url.substringAfter("our-story/")
        try {
            supabaseService.deleteFile(BUCKET, path)
        } catch (_: Exception) { }
        // 从数据库删除记录
        supabaseService.delete(TABLE, "id=eq.${photo.id}")
    }

    private fun compressImage(uri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ByteArray(0)
        val bitmap = inputStream.use { BitmapFactory.decodeStream(it) }
            ?: return ByteArray(0)

        // 按比例缩小
        val (newW, newH) = if (bitmap.width > MAX_WIDTH || bitmap.height > MAX_WIDTH) {
            val ratio = MAX_WIDTH.toFloat() / maxOf(bitmap.width, bitmap.height)
            ((bitmap.width * ratio).toInt() to (bitmap.height * ratio).toInt())
        } else bitmap.width to bitmap.height

        val resized = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        val output = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, output)
        if (resized != bitmap) resized.recycle()
        bitmap.recycle()
        return output.toByteArray()
    }
}
