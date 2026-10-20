package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProfileEntity

@Composable
fun BrowserTopBar(
    activeProfile: ProfileEntity,
    currentUrl: String,
    isLoading: Boolean,
    progress: Float,
    isSecure: Boolean,
    tabCount: Int,
    searchSuggestions: List<String>,
    onProfileClick: () -> Unit,
    onTabCounterClick: () -> Unit,
    onMenuClick: () -> Unit,
    onUrlSubmit: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onReload: () -> Unit,
    onSecurityClick: () -> Unit,
    onQrClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Avatar on top-left
            ProfileAvatar(
                profile = activeProfile,
                size = 36.dp,
                onClick = onProfileClick
            )

            // Address Bar in middle
            Box(modifier = Modifier.weight(1f)) {
                AddressBar(
                    currentUrl = currentUrl,
                    isLoading = isLoading,
                    progress = progress,
                    isSecure = isSecure,
                    searchEngine = activeProfile.searchEngine,
                    suggestions = searchSuggestions,
                    onUrlSubmit = onUrlSubmit,
                    onSearchQueryChange = onSearchQueryChange,
                    onReload = onReload,
                    onSecurityClick = onSecurityClick,
                    onQrClick = onQrClick
                )
            }

            // Tab counter button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(onClick = onTabCounterClick)
                    .testTag("tab_counter_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tabCount > 99) ":D" else tabCount.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Three-dot menu button
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("three_dot_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Browser menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
