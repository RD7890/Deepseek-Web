package com.rohan.deepseek.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_entries")
data class CacheEntry(
    @PrimaryKey val url: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val cachedAt: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis()
)
