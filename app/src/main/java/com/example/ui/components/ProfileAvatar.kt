package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.ProfileEntity

fun getProfileAvatarVector(avatarKey: String): ImageVector {
    return when (avatarKey.lowercase()) {
        "work", "business" -> Icons.Default.Business
        "school", "study" -> Icons.Default.School
        "shield", "security", "private" -> Icons.Default.Security
        "palette", "art" -> Icons.Default.ColorLens
        "rocket" -> Icons.Default.RocketLaunch
        "star" -> Icons.Default.Star
        "code", "dev" -> Icons.Default.Code
        else -> Icons.Default.Person
    }
}

val PredefinedProfileAvatars = listOf(
    "person" to "Personal",
    "work" to "Work",
    "school" to "Study",
    "shield" to "Private",
    "code" to "Developer",
    "palette" to "Creative",
    "rocket" to "Fast",
    "star" to "Favorite"
)

val PredefinedProfileColors = listOf(
    0xFF2563EBL to "Blue",
    0xFF10B981L to "Emerald",
    0xFF8B5CF6L to "Purple",
    0xFFF59E0BL to "Amber",
    0xFFEF4444L to "Rose",
    0xFF06B6D4L to "Cyan",
    0xFFEC4899L to "Pink",
    0xFF4B5563L to "Slate"
)

@Composable
fun ProfileAvatar(
    profile: ProfileEntity,
    size: Dp = 36.dp,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val bgColor = Color(profile.colorHex)
    val icon = getProfileAvatarVector(profile.avatar)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                } else Modifier
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .testTag("profile_avatar_${profile.id}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Profile: ${profile.name}",
            tint = Color.White,
            modifier = Modifier.size(size * 0.58f)
        )
    }
}
