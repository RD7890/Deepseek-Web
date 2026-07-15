package com.rohan.deepseek.cache

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class CacheManager(context: Context) {

    private val db = CacheDatabase.getInstance(context)
    private val dao = db.cacheDao()
    private val cacheDir = File(context.cacheDir, "deepseek_assets").apply { mkdirs() }
    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory index for zero-latency cache lookups on intercept thread
    private val memIndex = ConcurrentHashMap<String, CacheEntry>()

    init {
        scope.launch {
            dao.getAll().forEach { memIndex[it.url] = it }
        }
    }

    private val cacheableExtensions = setOf(
        "png", "jpg", "jpeg", "gif", "webp", "svg", "ico",
        "woff", "woff2", "ttf", "otf", "css", "js"
    )

    fun intercept(request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()
        if (!isCacheable(url)) return null

        // Fast in-memory lookup first
        val cached = memIndex[url]
        if (cached != null) {
            val file = File(cacheDir, cached.fileName)
            if (file.exists()) return WebResourceResponse(cached.mimeType, "UTF-8", file.inputStream())
            // File missing — remove stale entry
            memIndex.remove(url)
        }

        // Network fetch + store
        return try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout    = 15_000
                setRequestProperty("User-Agent", CHROME_UA)
                connect()
            }
            if (conn.responseCode != 200) { conn.disconnect(); return null }

            val mimeType = conn.contentType?.substringBefore(";")?.trim() ?: extMime(url)
            val bytes    = conn.inputStream.readBytes()
            conn.disconnect()

            val fileName = "${url.hashCode()}_${System.currentTimeMillis()}.bin"
            File(cacheDir, fileName).writeBytes(bytes)

            val entry = CacheEntry(
                url = url, fileName = fileName,
                mimeType = mimeType, sizeBytes = bytes.size.toLong()
            )
            memIndex[url] = entry
            scope.launch { dao.upsert(entry) }

            WebResourceResponse(mimeType, "UTF-8", bytes.inputStream())
        } catch (_: Exception) {
            null
        }
    }

    suspend fun clearAll() {
        cacheDir.listFiles()?.forEach { it.delete() }
        memIndex.clear()
        dao.deleteAll()
    }

    suspend fun deleteEntry(entry: CacheEntry) {
        File(cacheDir, entry.fileName).delete()
        memIndex.remove(entry.url)
        dao.delete(entry)
    }

    private fun isCacheable(url: String): Boolean {
        val ext = url.substringAfterLast('.')
            .substringBefore('?').substringBefore('#').lowercase()
        return ext in cacheableExtensions
    }

    private fun extMime(url: String): String {
        val ext = url.substringAfterLast('.').substringBefore('?').lowercase()
        return when (ext) {
            "css"        -> "text/css"
            "js"         -> "application/javascript"
            "png"        -> "image/png"
            "jpg","jpeg" -> "image/jpeg"
            "gif"        -> "image/gif"
            "webp"       -> "image/webp"
            "svg"        -> "image/svg+xml"
            "ico"        -> "image/x-icon"
            "woff"       -> "font/woff"
            "woff2"      -> "font/woff2"
            "ttf"        -> "font/ttf"
            "otf"        -> "font/otf"
            else         -> "application/octet-stream"
        }
    }

    companion object {
        const val CHROME_UA =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/126.0.0.0 Mobile Safari/537.36"
    }
}
