package com.miesport.app.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayOutputStream

/**
 * Uploads images to ImgBB and returns a public URL. Used for:
 * - Tournament banners (admin panel + admin-side Android flows)
 * - Payment screenshot uploads (registration / wallet deposit)
 * - Payment QR codes (admin support page)
 * - Chat images
 */
class ImgBbRepository(
    private val client: OkHttpClient = OkHttpClient()
) {
    companion object {
        private const val API_KEY = "6bdb23b28e7581721b28e46ce313308b"
        private const val UPLOAD_URL = "https://api.imgbb.com/1/upload"
        private const val MAX_DIMENSION = 1600 // downscale large photos before upload
    }

    suspend fun uploadImage(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = uriToBitmap(context, uri)
            val base64 = bitmapToBase64(bitmap)

            val formBody = FormBody.Builder()
                .add("key", API_KEY)
                .add("image", base64)
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(formBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException("ImgBB upload fail hua: ${response.code}")
                }
                val json = JSONObject(bodyStr)
                if (!json.optBoolean("success", false)) {
                    throw IllegalStateException("ImgBB ne upload reject kar diya")
                }
                json.getJSONObject("data").getString("url")
            }
        }
    }

    private fun uriToBitmap(context: Context, uri: Uri): Bitmap {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Image file nahi khul saki")
        val original = BitmapFactory.decodeStream(input)
        input.close()

        // Downscale to keep uploads fast and within ImgBB's size comfort zone
        val ratio = minOf(
            MAX_DIMENSION.toFloat() / original.width,
            MAX_DIMENSION.toFloat() / original.height,
            1f
        )
        return if (ratio < 1f) {
            Bitmap.createScaledBitmap(
                original,
                (original.width * ratio).toInt(),
                (original.height * ratio).toInt(),
                true
            )
        } else {
            original
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}
