package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DownloadItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads WHERE profileId = :profileId ORDER BY downloadedAt DESC")
    fun getDownloadsByProfileFlow(profileId: String): Flow<List<DownloadItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadItemEntity): Long

    @Update
    suspend fun updateDownload(item: DownloadItemEntity)

    @Delete
    suspend fun deleteDownload(item: DownloadItemEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: Long)

    @Query("DELETE FROM downloads WHERE profileId = :profileId")
    suspend fun clearDownloadsForProfile(profileId: String)
}
