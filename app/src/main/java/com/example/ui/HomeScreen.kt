package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoryEntity
import com.example.data.model.ProfileEntity
import com.example.data.model.ShortcutEntity
import com.example.ui.components.ProfileAvatar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getShortcutIcon(iconKey: String): ImageVector {
    return when (iconKey.lowercase()) {
        "google" -> Icons.Default.Search
        "book", "wiki", "wikipedia" -> Icons.Default.Book
        "code", "github" -> Icons.Default.Code
        "newspaper", "news", "hackernews" -> Icons.Default.Newspaper
        "chat", "reddit" -> Icons.Default.Chat
        "video", "youtube" -> Icons.Default.VideoLibrary
        else -> Icons.Default.Public
    }
}

data class DiscoverArticle(
    val title: String,
    val source: String,
    val timeAgo: String,
    val url: String,
    val category: String
)

val SampleDiscoverArticles = listOf(
    DiscoverArticle(
        title = "Android 16 Developer Preview: What's New in Edge-to-Edge and Privacy",
        source = "Android Developers",
        timeAgo = "2h ago",
        url = "https://developer.android.com",
        category = "Tech"
    ),
    DiscoverArticle(
        title = "James Webb Space Telescope detects unexpected atmospheric features on exoplanet",
        source = "NASA News",
        timeAgo = "4h ago",
        url = "https://nasa.gov",
        category = "Science"
    ),
    DiscoverArticle(
        title = "Kotlin 2.2 Released with Faster Compiler and Compose Multiplatform Updates",
        source = "JetBrains",
        timeAgo = "6h ago",
        url = "https://kotlinlang.org",
        category = "Dev"
    ),
    DiscoverArticle(
        title = "Modern Web Architecture: The Shift Towards Privacy-First Sandboxed Profiles",
        source = "Web Standards",
        timeAgo = "8h ago",
        url = "https://w3.org",
        category = "Security"
    )
)

@Composable
fun HomeScreen(
    activeProfile: ProfileEntity,
    shortcuts: List<ShortcutEntity>,
    recentHistory: List<HistoryEntity>,
    isPrivateMode: Boolean,
    onUrlSelected: (String) -> Unit,
    onAddShortcut: (String, String) -> Unit,
    onRemoveShortcut: (ShortcutEntity) -> Unit,
    onSwitchProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddShortcutDialog by remember { mutableStateOf(false) }
    var newShortcutTitle by remember { mutableStateOf("") }
    var newShortcutUrl by remember { mutableStateOf("") }
    val profileColor = Color(activeProfile.colorHex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Badge & Mode Indicator
        Surface(
            modifier = Modifier
                .clickable(onClick = onSwitchProfileClick)
                .testTag("home_profile_badge"),
            shape = RoundedCornerShape(20.dp),
            color = if (isPrivateMode) MaterialTheme.colorScheme.tertiaryContainer else profileColor.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isPrivateMode) MaterialTheme.colorScheme.tertiary else profileColor.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPrivateMode) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Incognito Mode",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    ProfileAvatar(profile = activeProfile, size = 20.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${activeProfile.name} Profile",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Browser Logo Title
        Text(
            text = "PRISM",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 4.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Isolated • Secure • Fast",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Search engine input fake-box (tapping navigates focus to address bar or triggers search)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .clickable {
                    onUrlSelected("https://www.google.com")
                }
                .testTag("home_search_box"),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search with ${activeProfile.searchEngine} or enter URL",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Shortcuts Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shortcuts",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = { showAddShortcutDialog = true },
                modifier = Modifier
                    .size(32.dp)
                    .testTag("add_shortcut_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add shortcut",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Shortcuts grid
        val allShortcutsWithAdd = shortcuts
        val chunkedShortcuts = allShortcutsWithAdd.chunked(4)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            for (row in chunkedShortcuts) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (shortcut in row) {
                        ShortcutItem(
                            shortcut = shortcut,
                            onClick = { onUrlSelected(shortcut.url) },
                            onLongClick = { onRemoveShortcut(shortcut) }
                        )
                    }
                }
            }
        }

        // Recent history if available
        if (recentHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recently Visited (${activeProfile.name})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recentHistory.take(6)) { historyItem ->
                    ElevatedCard(
                        modifier = Modifier
                            .width(170.dp)
                            .clickable { onUrlSelected(historyItem.url) }
                            .testTag("recent_history_card_${historyItem.id}"),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = historyItem.title.ifBlank { historyItem.url },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = historyItem.url,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Discovery / News Feed Section
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Article,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Discover & Top Stories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SampleDiscoverArticles.forEach { article ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUrlSelected(article.url) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = article.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${article.source} • ${article.timeAgo}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = article.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    // Add Shortcut Dialog
    if (showAddShortcutDialog) {
        AlertDialog(
            onDismissRequest = { showAddShortcutDialog = false },
            title = { Text("Add Shortcut") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newShortcutTitle,
                        onValueChange = { newShortcutTitle = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newShortcutUrl,
                        onValueChange = { newShortcutUrl = it },
                        label = { Text("URL (e.g. example.com)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newShortcutTitle.isNotBlank() && newShortcutUrl.isNotBlank()) {
                            val finalUrl = if (!newShortcutUrl.startsWith("http://") && !newShortcutUrl.startsWith("https://")) {
                                "https://$newShortcutUrl"
                            } else newShortcutUrl
                            onAddShortcut(newShortcutTitle, finalUrl)
                            newShortcutTitle = ""
                            newShortcutUrl = ""
                            showAddShortcutDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddShortcutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ShortcutItem(
    shortcut: ShortcutEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
            .testTag("shortcut_item_${shortcut.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getShortcutIcon(shortcut.iconKey),
                contentDescription = shortcut.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = shortcut.title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
