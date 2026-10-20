package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "site_permissions")
data class SitePermissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: String,
    val host: String,
    val permissionType: String, // CAMERA, GEOLOCATION, NOTIFICATIONS
    val isGranted: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)
