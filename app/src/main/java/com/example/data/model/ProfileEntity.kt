package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatar: String, // icon identifier: "person", "work", "school", "shield", "palette", "rocket", "star", "code"
    val colorHex: Long, // 0xFF2563EB, etc.
    val customAvatarPath: String? = null,
    val homepageUrl: String = "prism://home",
    val searchEngine: String = "Google",
    val isProtected: Boolean = false,
    val pinCode: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
