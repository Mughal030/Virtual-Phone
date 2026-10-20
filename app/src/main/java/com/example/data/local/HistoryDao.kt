package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history WHERE profileId = :profileId ORDER BY visitedAt DESC")
    fun getHistoryByProfileFlow(profileId: String): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE profileId = :profileId ORDER BY visitedAt DESC LIMIT :limit")
    suspend fun getRecentHistory(profileId: String, limit: Int = 10): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE profileId = :profileId AND (title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%') ORDER BY visitedAt DESC")
    suspend fun searchHistory(profileId: String, query: String): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE profileId = :profileId AND url = :url LIMIT 1")
    suspend fun getHistoryItemByUrl(profileId: String, url: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(item: HistoryEntity): Long

    @Update
    suspend fun updateHistoryItem(item: HistoryEntity)

    @Delete
    suspend fun deleteHistoryItem(item: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM history WHERE profileId = :profileId")
    suspend fun clearHistoryForProfile(profileId: String)

    @Query("DELETE FROM history WHERE profileId = :profileId AND visitedAt >= :fromTimestamp")
    suspend fun clearHistorySince(profileId: String, fromTimestamp: Long)
}
