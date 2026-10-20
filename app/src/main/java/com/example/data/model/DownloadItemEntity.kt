package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: String,
    val filename: String,
    val url: String,
    val mimeType: String,
    val sizeBytes: Long = 0L,
    val downloadedAt: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED", // DOWNLOADING, COMPLETED, FAILED
    val filePath: String? = null
)
