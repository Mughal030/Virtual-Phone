package com.example.data.repository

import com.example.data.local.BrowserDatabase
import com.example.data.local.SettingsPreferenceManager
import com.example.data.model.BookmarkEntity
import com.example.data.model.BrowserSettings
import com.example.data.model.DownloadItemEntity
import com.example.data.model.HistoryEntity
import com.example.data.model.ProfileEntity
import com.example.data.model.ShortcutEntity
import com.example.data.model.SitePermissionEntity
import com.example.data.model.TabEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class BrowserRepository(
    private val database: BrowserDatabase,
    private val settingsManager: SettingsPreferenceManager
) {
    val settingsFlow: StateFlow<BrowserSettings> = settingsManager.settingsFlow

    fun updateSettings(update: (BrowserSettings) -> BrowserSettings) {
        settingsManager.updateSettings(update)
    }

    // Profiles
    fun getAllProfilesFlow(): Flow<List<ProfileEntity>> = database.profileDao().getAllProfilesFlow()

    suspend fun getProfileById(id: String): ProfileEntity? = database.profileDao().getProfileById(id)

    suspend fun saveProfile(profile: ProfileEntity) = database.profileDao().insertOrUpdateProfile(profile)

    suspend fun deleteProfile(profileId: String) {
        database.profileDao().deleteProfileById(profileId)
        database.tabDao().deleteAllTabsForProfile(profileId)
        database.bookmarkDao().deleteAllBookmarksForProfile(profileId)
        database.historyDao().clearHistoryForProfile(profileId)
        database.downloadDao().clearDownloadsForProfile(profileId)
        database.shortcutDao().clearShortcutsForProfile(profileId)
        database.sitePermissionDao().clearPermissionsForProfile(profileId)
    }

    suspend fun clearProfileData(profileId: String) {
        database.historyDao().clearHistoryForProfile(profileId)
        database.downloadDao().clearDownloadsForProfile(profileId)
        database.sitePermissionDao().clearPermissionsForProfile(profileId)
    }

    suspend fun seedInitialDataIfEmpty() {
        val profiles = database.profileDao().getAllProfiles()
        if (profiles.isEmpty()) {
            val personal = ProfileEntity(
                id = "personal",
                name = "Personal",
                avatar = "person",
                colorHex = 0xFF2563EB,
                homepageUrl = "prism://home",
                searchEngine = "Google"
            )
            val work = ProfileEntity(
                id = "work",
                name = "Work",
                avatar = "work",
                colorHex = 0xFF10B981,
                homepageUrl = "prism://home",
                searchEngine = "DuckDuckGo"
            )
            val study = ProfileEntity(
                id = "study",
                name = "Study",
                avatar = "school",
                colorHex = 0xFFF59E0B,
                homepageUrl = "prism://home",
                searchEngine = "Google"
            )
            database.profileDao().insertOrUpdateProfile(personal)
            database.profileDao().insertOrUpdateProfile(work)
            database.profileDao().insertOrUpdateProfile(study)

            // Seed default shortcuts for Personal
            val shortcuts = listOf(
                ShortcutEntity(profileId = "personal", title = "Google", url = "https://www.google.com", iconKey = "google"),
                ShortcutEntity(profileId = "personal", title = "Wikipedia", url = "https://www.wikipedia.org", iconKey = "book"),
                ShortcutEntity(profileId = "personal", title = "GitHub", url = "https://github.com", iconKey = "code"),
                ShortcutEntity(profileId = "personal", title = "Hacker News", url = "https://news.ycombinator.com", iconKey = "newspaper"),
                ShortcutEntity(profileId = "personal", title = "Reddit", url = "https://www.reddit.com", iconKey = "chat"),
                ShortcutEntity(profileId = "personal", title = "YouTube", url = "https://www.youtube.com", iconKey = "video")
            )
            shortcuts.forEach { database.shortcutDao().insertShortcut(it) }

            // Initial tab for personal profile
            val initialTab = TabEntity(
                id = UUID.randomUUID().toString(),
                profileId = "personal",
                title = "New Tab",
                url = "prism://home"
            )
            database.tabDao().insertOrUpdateTab(initialTab)
        }
    }

    // Tabs
    fun getTabsByProfileFlow(profileId: String, isPrivate: Boolean): Flow<List<TabEntity>> =
        database.tabDao().getTabsByProfileFlow(profileId, isPrivate)

    suspend fun getTabsByProfile(profileId: String, isPrivate: Boolean): List<TabEntity> =
        database.tabDao().getTabsByProfile(profileId, isPrivate)

    suspend fun saveTab(tab: TabEntity) = database.tabDao().insertOrUpdateTab(tab)

    suspend fun deleteTab(tabId: String) = database.tabDao().deleteTabById(tabId)

    suspend fun clearTabsByProfile(profileId: String, isPrivate: Boolean) =
        database.tabDao().clearTabsByProfile(profileId, isPrivate)

    // Bookmarks
    fun getBookmarksByProfileFlow(profileId: String): Flow<List<BookmarkEntity>> =
        database.bookmarkDao().getBookmarksByProfileFlow(profileId)

    suspend fun isBookmarked(profileId: String, url: String): Boolean =
        database.bookmarkDao().getBookmarkByUrl(profileId, url) != null

    suspend fun addBookmark(profileId: String, title: String, url: String) =
        database.bookmarkDao().insertBookmark(BookmarkEntity(profileId = profileId, title = title, url = url))

    suspend fun deleteBookmark(bookmark: BookmarkEntity) =
        database.bookmarkDao().deleteBookmark(bookmark)

    suspend fun deleteBookmarkByUrl(profileId: String, url: String) =
        database.bookmarkDao().deleteBookmarkByUrl(profileId, url)

    // History
    fun getHistoryByProfileFlow(profileId: String): Flow<List<HistoryEntity>> =
        database.historyDao().getHistoryByProfileFlow(profileId)

    suspend fun getRecentHistory(profileId: String, limit: Int = 8): List<HistoryEntity> =
        database.historyDao().getRecentHistory(profileId, limit)

    suspend fun recordVisit(profileId: String, title: String, url: String) {
        if (url == "prism://home" || url.startsWith("about:") || url.isBlank()) return
        val existing = database.historyDao().getHistoryItemByUrl(profileId, url)
        if (existing != null) {
            database.historyDao().updateHistoryItem(
                existing.copy(
                    title = title.ifBlank { existing.title },
                    visitedAt = System.currentTimeMillis(),
                    visitCount = existing.visitCount + 1
                )
            )
        } else {
            database.historyDao().insertHistoryItem(
                HistoryEntity(
                    profileId = profileId,
                    title = title.ifBlank { url },
                    url = url,
                    visitedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteHistoryItem(id: Long) = database.historyDao().deleteHistoryById(id)

    suspend fun clearHistory(profileId: String) = database.historyDao().clearHistoryForProfile(profileId)

    // Shortcuts
    fun getShortcutsByProfileFlow(profileId: String): Flow<List<ShortcutEntity>> =
        database.shortcutDao().getShortcutsByProfileFlow(profileId)

    suspend fun addShortcut(shortcut: ShortcutEntity) = database.shortcutDao().insertShortcut(shortcut)

    suspend fun deleteShortcut(shortcut: ShortcutEntity) = database.shortcutDao().deleteShortcut(shortcut)

    // Downloads
    fun getDownloadsByProfileFlow(profileId: String): Flow<List<DownloadItemEntity>> =
        database.downloadDao().getDownloadsByProfileFlow(profileId)

    suspend fun recordDownload(item: DownloadItemEntity) = database.downloadDao().insertDownload(item)

    suspend fun deleteDownload(id: Long) = database.downloadDao().deleteDownloadById(id)

    suspend fun clearDownloads(profileId: String) = database.downloadDao().clearDownloadsForProfile(profileId)

    // Site Permissions
    fun getPermissionsByProfileFlow(profileId: String): Flow<List<SitePermissionEntity>> =
        database.sitePermissionDao().getPermissionsByProfileFlow(profileId)

    suspend fun getSitePermission(profileId: String, host: String, type: String): SitePermissionEntity? =
        database.sitePermissionDao().getPermission(profileId, host, type)

    suspend fun setSitePermission(profileId: String, host: String, type: String, granted: Boolean) =
        database.sitePermissionDao().insertOrUpdatePermission(
            SitePermissionEntity(
                profileId = profileId,
                host = host,
                permissionType = type,
                isGranted = granted
            )
        )

    suspend fun deleteSitePermission(id: Long) = database.sitePermissionDao().deletePermissionById(id)
}
