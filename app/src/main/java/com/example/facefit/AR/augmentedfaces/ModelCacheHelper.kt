package com.example.facefit.AR.augmentedfaces

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ModelCacheHelper(private val context: Context) {
    companion object {
        private const val CACHE_DIR = "ar_models"
        private const val TEXTURE_CACHE_DIR = "ar_textures"
    }

    // Get cached model or texture
    fun getModel(modelUrl: String): InputStream? {
        return getCachedAsset(modelUrl, CACHE_DIR, ".obj")
    }

    fun getTexture(textureUrl: String): String? {
        val cacheFile = getCachedFile(textureUrl, TEXTURE_CACHE_DIR, ".png")
        return if (cacheFile.exists()) {
            cacheFile.absolutePath
        } else {
            // Try to download and cache the texture
            downloadAndCache(textureUrl, cacheFile)?.let {
                cacheFile.absolutePath
            }
        }
    }

    private fun getCachedAsset(url: String, subDir: String, extension: String): InputStream? {
        val cacheFile = getCachedFile(url, subDir, extension)
        return try {
            if (cacheFile.exists()) {
                FileInputStream(cacheFile)
            } else {
                downloadAndCache(url, cacheFile)
            }
        } catch (e: IOException) {
            Log.e("ModelCache", "Failed to load asset: ${e.message}")
            null
        }
    }

    private fun getCachedFile(url: String, subDir: String, extension: String): File {
        val fileName = "${url.hashCode()}$extension"
        val cacheDir = File(context.cacheDir, subDir)
        if (!cacheDir.exists()) cacheDir.mkdirs()
        return File(cacheDir, fileName)
    }

    private fun downloadAndCache(url: String, cacheFile: File): InputStream? {
        return try {
            val downloadedBytes = downloadFromUrl(url)
            FileOutputStream(cacheFile).use { it.write(downloadedBytes) }
            FileInputStream(cacheFile)
        } catch (e: Exception) {
            Log.e("ModelCache", "Download failed: ${e.message}")
            null
        }
    }

    private fun downloadFromUrl(url: String): ByteArray {
        // Implement your download logic here (example with OkHttp)
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        return response.body?.bytes() ?: throw IOException("Empty response")
    }
}