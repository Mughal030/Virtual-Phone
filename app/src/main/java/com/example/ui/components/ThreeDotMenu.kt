package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreeDotMenu(
    isBookmarked: Boolean,
    isDesktopSite: Boolean,
    currentUrl: String,
    pageTitle: String,
    activeProfile: ProfileEntity,
    onDismiss: () -> Unit,
    onNewTab: () -> Unit,
    onNewPrivateTab: () -> Unit,
    onSwitchProfile: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit,
    onFindInPage: () -> Unit,
    onToggleDesktopSite: () -> Unit,
    onTranslate: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onReload: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Quick action icons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = {
                        onToggleBookmark()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("menu_quick_bookmark")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        onReload()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("menu_quick_reload")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        if (currentUrl.isNotBlank() && currentUrl != "prism://home") {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, pageTitle)
                                putExtra(Intent.EXTRA_TEXT, currentUrl)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share webpage"))
                        }
                        onDismiss()
                    },
                    modifier = Modifier.testTag("menu_quick_share")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        onOpenDownloads()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("menu_quick_download")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Downloads",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Main Actions
            MenuItem(
                icon = Icons.Default.Add,
                title = "New Tab",
                testTag = "menu_new_tab",
                onClick = {
                    onNewTab()
                    onDismiss()
                }
            )

            MenuItem(
                icon = Icons.Default.Security,
                title = "New Private Tab",
                testTag = "menu_new_private_tab",
                onClick = {
                    onNewPrivateTab()
                    onDismiss()
                }
            )

            MenuItem(
                icon = Icons.Default.SwitchAccount,
                title = "Switch Profile (${activeProfile.name})",
                testTag = "menu_switch_profile",
                onClick = {
                    onSwitchProfile()
                    onDismiss()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            MenuItem(
                icon = Icons.Default.Bookmark,
                title = "Bookmarks",
                testTag = "menu_bookmarks",
                onClick = {
                    onOpenBookmarks()
                    onDismiss()
                }
            )

            MenuItem(
                icon = Icons.Default.History,
                title = "History",
                testTag = "menu_history",
                onClick = {
                    onOpenHistory()
                    onDismiss()
                }
            )

            MenuItem(
                icon = Icons.Default.Download,
                title = "Downloads",
                testTag = "menu_downloads",
                onClick = {
                    onOpenDownloads()
                    onDismiss()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            MenuItem(
                icon = Icons.Default.FindInPage,
                title = "Find in page",
                testTag = "menu_find_in_page",
                onClick = {
                    onFindInPage()
                    onDismiss()
                }
            )

            MenuItem(
                icon = Icons.Default.Translate,
                title = "Translate...",
                testTag = "menu_translate",
                onClick = {
                    onTranslate()
                    onDismiss()
                }
            )

            // Desktop site toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleDesktopSite() }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DesktopMac,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Desktop site",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isDesktopSite,
                    onCheckedChange = { onToggleDesktopSite() },
                    modifier = Modifier.testTag("menu_desktop_site_switch")
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            MenuItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                testTag = "menu_settings",
                onClick = {
                    onOpenSettings()
                    onDismiss()
                }
            )

            MenuItem(
                icon = Icons.Default.Info,
                title = "About Prism Browser",
                testTag = "menu_about",
                onClick = {
                    onOpenAbout()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
