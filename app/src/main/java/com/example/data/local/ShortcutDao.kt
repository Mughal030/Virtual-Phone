package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ShortcutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts WHERE profileId = :profileId ORDER BY id ASC")
    fun getShortcutsByProfileFlow(profileId: String): Flow<List<ShortcutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: ShortcutEntity): Long

    @Delete
    suspend fun deleteShortcut(shortcut: ShortcutEntity)

    @Query("DELETE FROM shortcuts WHERE id = :id")
    suspend fun deleteShortcutById(id: Long)

    @Query("DELETE FROM shortcuts WHERE profileId = :profileId")
    suspend fun clearShortcutsForProfile(profileId: String)
}
