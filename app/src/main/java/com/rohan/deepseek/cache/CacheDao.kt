package com.rohan.deepseek.cache

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheDao {

    @Query("SELECT * FROM cache_entries ORDER BY sizeBytes DESC")
    fun observeAll(): Flow<List<CacheEntry>>

    @Query("SELECT SUM(sizeBytes) FROM cache_entries")
    fun observeTotalSize(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM cache_entries")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM cache_entries")
    suspend fun getAll(): List<CacheEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: CacheEntry)

    @Delete
    suspend fun delete(entry: CacheEntry)

    @Query("DELETE FROM cache_entries")
    suspend fun deleteAll()

    @Query("SELECT * FROM cache_entries WHERE url = :url LIMIT 1")
    suspend fun findByUrl(url: String): CacheEntry?
}
