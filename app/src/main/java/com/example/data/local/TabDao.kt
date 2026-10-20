package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {
    @Query("SELECT * FROM tabs WHERE profileId = :profileId AND isPrivate = :isPrivate ORDER BY lastActiveAt DESC")
    fun getTabsByProfileFlow(profileId: String, isPrivate: Boolean): Flow<List<TabEntity>>

    @Query("SELECT * FROM tabs WHERE profileId = :profileId AND isPrivate = :isPrivate ORDER BY lastActiveAt DESC")
    suspend fun getTabsByProfile(profileId: String, isPrivate: Boolean): List<TabEntity>

    @Query("SELECT * FROM tabs WHERE id = :id LIMIT 1")
    suspend fun getTabById(id: String): TabEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTab(tab: TabEntity)

    @Update
    suspend fun updateTab(tab: TabEntity)

    @Delete
    suspend fun deleteTab(tab: TabEntity)

    @Query("DELETE FROM tabs WHERE id = :id")
    suspend fun deleteTabById(id: String)

    @Query("DELETE FROM tabs WHERE profileId = :profileId AND isPrivate = :isPrivate")
    suspend fun clearTabsByProfile(profileId: String, isPrivate: Boolean)

    @Query("DELETE FROM tabs WHERE profileId = :profileId")
    suspend fun deleteAllTabsForProfile(profileId: String)
}
