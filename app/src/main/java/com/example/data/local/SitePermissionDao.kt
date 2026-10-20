package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SitePermissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SitePermissionDao {
    @Query("SELECT * FROM site_permissions WHERE profileId = :profileId ORDER BY updatedAt DESC")
    fun getPermissionsByProfileFlow(profileId: String): Flow<List<SitePermissionEntity>>

    @Query("SELECT * FROM site_permissions WHERE profileId = :profileId AND host = :host AND permissionType = :permissionType LIMIT 1")
    suspend fun getPermission(profileId: String, host: String, permissionType: String): SitePermissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePermission(permission: SitePermissionEntity): Long

    @Delete
    suspend fun deletePermission(permission: SitePermissionEntity)

    @Query("DELETE FROM site_permissions WHERE id = :id")
    suspend fun deletePermissionById(id: Long)

    @Query("DELETE FROM site_permissions WHERE profileId = :profileId")
    suspend fun clearPermissionsForProfile(profileId: String)
}
