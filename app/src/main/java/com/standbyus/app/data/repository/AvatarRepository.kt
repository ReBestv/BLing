package com.standbyus.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import com.standbyus.app.data.remote.SupabaseService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class AvatarRepository @Inject constructor(
    private val supabaseService: SupabaseService,
    @ApplicationContext private val context: Context
) {
    suspend fun uploadAvatar(uri: Uri, deviceId: String): String {
        val bytes = compressAvatar(uri)
        val timestamp = System.currentTimeMillis()
        val path = "avatars/$deviceId/$timestamp.jpg"
        return supabaseService.uploadFile(BUCKET, path, bytes, "image/jpeg")
    }

    private fun compressAvatar(uri: Uri): ByteArray {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }

        val side = min(bitmap.width, bitmap.height)
        val left = (bitmap.width - side) / 2
        val top = (bitmap.height - side) / 2
        val cropped = Bitmap.createBitmap(bitmap, left, top, side, side)
        val resized = if (side > AVATAR_SIZE_PX) {
            Bitmap.createScaledBitmap(cropped, AVATAR_SIZE_PX, AVATAR_SIZE_PX, true)
        } else {
            cropped
        }

        val output = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)

        if (resized != cropped) resized.recycle()
        if (cropped != bitmap) cropped.recycle()
        bitmap.recycle()

        return output.toByteArray()
    }

    companion object {
        private const val BUCKET = "our-story"
        private const val AVATAR_SIZE_PX = 512
        private const val JPEG_QUALITY = 88
    }
}
