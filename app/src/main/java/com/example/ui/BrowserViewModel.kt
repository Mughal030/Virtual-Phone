package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.BrowserRepository
import com.example.engine.ProfileStorageManager
import com.example.engine.SearchEngineHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class ScreenDestination {
    BROWSER,
    TAB_SWITCHER,
    BOOKMARKS,
    HISTORY,
    DOWNLOADS,
    SETTINGS,
    SITE_PERMISSIONS,
    ABOUT,
    PROFILE_MANAGEMENT
}

data class WebPageState(
    val url: String = "prism://home",
    val title: String = "New Tab",
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isSecure: Boolean = false,
    val isDesktopSite: Boolean = false,
    val favicon: Bitmap? = null,
    val sslError: String? = null
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    val repository: BrowserRepository
    val profileStorageManager: ProfileStorageManager

    init {
        val db = BrowserDatabase.getInstance(application)
        val settingsManager = SettingsPreferenceManager(application)
        repository = BrowserRepository(db, settingsManager)
        profileStorageManager = ProfileStorageManager(application)

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            loadInitialProfileAndTabs()
        }
    }

    // Settings
    val settings: StateFlow<BrowserSettings> = repository.settingsFlow

    // Profiles
    val allProfiles: StateFlow<List<ProfileEntity>> = repository.getAllProfilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeProfile = MutableStateFlow(
        ProfileEntity(
            id = "personal",
            name = "Personal",
            avatar = "person",
            colorHex = 0xFF2563EB,
            homepageUrl = "prism://home",
            searchEngine = "Google"
        )
    )
    val activeProfile: StateFlow<ProfileEntity> = _activeProfile.asStateFlow()

    // Screen navigation
    private val _currentScreen = MutableStateFlow(ScreenDestination.BROWSER)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Tabs
    private val _isPrivateMode = MutableStateFlow(false)
    val isPrivateMode: StateFlow<Boolean> = _isPrivateMode.asStateFlow()

    private val _tabs = MutableStateFlow<List<TabEntity>>(emptyList())
    val tabs: StateFlow<List<TabEntity>> = _tabs.asStateFlow()

    private val _activeTab = MutableStateFlow<TabEntity?>(null)
    val activeTab: StateFlow<TabEntity?> = _activeTab.asStateFlow()

    // Web Page State
    private val _pageState = MutableStateFlow(WebPageState())
    val pageState: StateFlow<WebPageState> = _pageState.asStateFlow()

    // Bookmarks for active profile
    private val _isCurrentPageBookmarked = MutableStateFlow(false)
    val isCurrentPageBookmarked: StateFlow<Boolean> = _isCurrentPageBookmarked.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> = MutableStateFlow<List<BookmarkEntity>>(emptyList())
    private val _profileBookmarks = MutableStateFlow<List<BookmarkEntity>>(emptyList())
    val profileBookmarks: StateFlow<List<BookmarkEntity>> = _profileBookmarks.asStateFlow()

    // History for active profile
    private val _profileHistory = MutableStateFlow<List<HistoryEntity>>(emptyList())
    val profileHistory: StateFlow<List<HistoryEntity>> = _profileHistory.asStateFlow()

    // Shortcuts for active profile
    private val _profileShortcuts = MutableStateFlow<List<ShortcutEntity>>(emptyList())
    val profileShortcuts: StateFlow<List<ShortcutEntity>> = _profileShortcuts.asStateFlow()

    // Downloads for active profile
    private val _profileDownloads = MutableStateFlow<List<DownloadItemEntity>>(emptyList())
    val profileDownloads: StateFlow<List<DownloadItemEntity>> = _profileDownloads.asStateFlow()

    // Site Permissions
    private val _profilePermissions = MutableStateFlow<List<SitePermissionEntity>>(emptyList())
    val profilePermissions: StateFlow<List<SitePermissionEntity>> = _profilePermissions.asStateFlow()

    // Dialog & UI Sheet Controls
    private val _showProfileSwitcher = MutableStateFlow(false)
    val showProfileSwitcher: StateFlow<Boolean> = _showProfileSwitcher.asStateFlow()

    private val _editingProfile = MutableStateFlow<ProfileEntity?>(null)
    val editingProfile: StateFlow<ProfileEntity?> = _editingProfile.asStateFlow()
    private val _showCreateProfile = MutableStateFlow(false)
    val showCreateProfile: StateFlow<Boolean> = _showCreateProfile.asStateFlow()

    private val _showSecurityDialog = MutableStateFlow(false)
    val showSecurityDialog: StateFlow<Boolean> = _showSecurityDialog.asStateFlow()

    private val _showFindInPage = MutableStateFlow(false)
    val showFindInPage: StateFlow<Boolean> = _showFindInPage.asStateFlow()

    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery.asStateFlow()

    private val _showThreeDotMenu = MutableStateFlow(false)
    val showThreeDotMenu: StateFlow<Boolean> = _showThreeDotMenu.asStateFlow()

    private val _showQrScanner = MutableStateFlow(false)
    val showQrScanner: StateFlow<Boolean> = _showQrScanner.asStateFlow()

    // Search Suggestions
    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()

    // Web Actions dispatch (triggers reload, back, forward, find, etc.)
    private val _navigationAction = MutableStateFlow<NavigationAction?>(null)
    val navigationAction: StateFlow<NavigationAction?> = _navigationAction.asStateFlow()

    sealed class NavigationAction {
        object GoBack : NavigationAction()
        object GoForward : NavigationAction()
        object Reload : NavigationAction()
        object StopLoading : NavigationAction()
        data class LoadUrl(val url: String) : NavigationAction()
        data class FindInPage(val query: String, val forward: Boolean = true) : NavigationAction()
        object ClearFindMatches : NavigationAction()
    }

    private suspend fun loadInitialProfileAndTabs() {
        val profiles = repository.databaseProfileDao().getAllProfiles()
        val defaultProfile = profiles.firstOrNull { it.id == settings.value.defaultProfileId }
            ?: profiles.firstOrNull()
            ?: ProfileEntity(
                id = "personal",
                name = "Personal",
                avatar = "person",
                colorHex = 0xFF2563EB
            )

        _activeProfile.value = defaultProfile
        observeProfileData(defaultProfile.id)
        refreshTabsForCurrentProfile()
    }

    private fun BrowserRepository.databaseProfileDao() =
        BrowserDatabase.getInstance(getApplication()).profileDao()

    private fun observeProfileData(profileId: String) {
        viewModelScope.launch {
            repository.getBookmarksByProfileFlow(profileId).collect {
                _profileBookmarks.value = it
                checkCurrentPageBookmarked()
            }
        }
        viewModelScope.launch {
            repository.getHistoryByProfileFlow(profileId).collect {
                _profileHistory.value = it
            }
        }
        viewModelScope.launch {
            repository.getShortcutsByProfileFlow(profileId).collect {
                _profileShortcuts.value = it
            }
        }
        viewModelScope.launch {
            repository.getDownloadsByProfileFlow(profileId).collect {
                _profileDownloads.value = it
            }
        }
        viewModelScope.launch {
            repository.getPermissionsByProfileFlow(profileId).collect {
                _profilePermissions.value = it
            }
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        _currentScreen.value = destination
    }

    fun switchProfile(profile: ProfileEntity) {
        if (profile.id == _activeProfile.value.id) {
            _showProfileSwitcher.value = false
            return
        }

        viewModelScope.launch {
            // Save current active tab state before switching
            _activeTab.value?.let { currentTab ->
                repository.saveTab(
                    currentTab.copy(
                        url = _pageState.value.url,
                        title = _pageState.value.title,
                        lastActiveAt = System.currentTimeMillis()
                    )
                )
            }

            _activeProfile.value = profile
            _showProfileSwitcher.value = false
            _isPrivateMode.value = false
            observeProfileData(profile.id)
            refreshTabsForCurrentProfile()
        }
    }

    fun refreshTabsForCurrentProfile() {
        viewModelScope.launch {
            val profileId = _activeProfile.value.id
            val isPrivate = _isPrivateMode.value
            val existingTabs = repository.getTabsByProfile(profileId, isPrivate)
            if (existingTabs.isEmpty()) {
                val newTab = TabEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "New Tab",
                    url = _activeProfile.value.homepageUrl,
                    isPrivate = isPrivate
                )
                repository.saveTab(newTab)
                _tabs.value = listOf(newTab)
                selectTab(newTab)
            } else {
                _tabs.value = existingTabs
                val lastActive = existingTabs.maxByOrNull { it.lastActiveAt } ?: existingTabs.first()
                selectTab(lastActive)
            }
        }
    }

    fun selectTab(tab: TabEntity) {
        _activeTab.value = tab
        _pageState.value = _pageState.value.copy(
            url = tab.url,
            title = tab.title,
            isSecure = tab.url.startsWith("https://")
        )
        if (tab.url != "prism://home") {
            _navigationAction.value = NavigationAction.LoadUrl(tab.url)
        }
        checkCurrentPageBookmarked()
    }

    fun createNewTab(url: String = "prism://home", isPrivate: Boolean = _isPrivateMode.value) {
        viewModelScope.launch {
            val newTab = TabEntity(
                id = UUID.randomUUID().toString(),
                profileId = _activeProfile.value.id,
                title = "New Tab",
                url = url,
                isPrivate = isPrivate
            )
            repository.saveTab(newTab)
            val updated = listOf(newTab) + _tabs.value
            _tabs.value = updated
            selectTab(newTab)
            _currentScreen.value = ScreenDestination.BROWSER
        }
    }

    fun closeTab(tab: TabEntity) {
        viewModelScope.launch {
            repository.deleteTab(tab.id)
            val remaining = _tabs.value.filter { it.id != tab.id }
            _tabs.value = remaining
            if (tab.id == _activeTab.value?.id) {
                if (remaining.isNotEmpty()) {
                    selectTab(remaining.first())
                } else {
                    createNewTab(isPrivate = _isPrivateMode.value)
                }
            }
        }
    }

    fun closeAllTabs() {
        viewModelScope.launch {
            repository.clearTabsByProfile(_activeProfile.value.id, _isPrivateMode.value)
            if (_isPrivateMode.value) {
                profileStorageManager.clearPrivateSessions()
            }
            _tabs.value = emptyList()
            createNewTab(isPrivate = _isPrivateMode.value)
        }
    }

    fun setPrivateMode(isPrivate: Boolean) {
        if (_isPrivateMode.value != isPrivate) {
            _isPrivateMode.value = isPrivate
            refreshTabsForCurrentProfile()
        }
    }

    fun onUrlSubmitted(input: String) {
        val targetUrl = SearchEngineHelper.resolveInputToUrl(input, _activeProfile.value.searchEngine)
        _searchSuggestions.value = emptyList()
        _navigationAction.value = NavigationAction.LoadUrl(targetUrl)
        _activeTab.value?.let { tab ->
            viewModelScope.launch {
                repository.saveTab(tab.copy(url = targetUrl))
            }
        }
    }

    fun onSearchInputChanged(query: String) {
        if (query.isBlank()) {
            _searchSuggestions.value = emptyList()
            return
        }
        val historyMatches = _profileHistory.value
            .filter { it.title.contains(query, ignoreCase = true) || it.url.contains(query, ignoreCase = true) }
            .take(3)
            .map { it.title.ifBlank { it.url } }

        val bookmarkMatches = _profileBookmarks.value
            .filter { it.title.contains(query, ignoreCase = true) || it.url.contains(query, ignoreCase = true) }
            .take(2)
            .map { it.title }

        val searchQuick = SearchEngineHelper.defaultQuickSearchSuggestions
            .filter { it.contains(query, ignoreCase = true) }
            .take(3)

        _searchSuggestions.value = (historyMatches + bookmarkMatches + searchQuick).distinct()
    }

    // Web Event Listeners called from WebView
    fun onPageStarted(url: String) {
        _pageState.value = _pageState.value.copy(
            url = url,
            isLoading = true,
            progress = 0.1f,
            isSecure = url.startsWith("https://")
        )
    }

    fun onPageFinished(url: String, title: String?, canGoBack: Boolean, canGoForward: Boolean) {
        val finalTitle = if (!title.isNullOrBlank()) title else SearchEngineHelper.extractDomain(url)
        _pageState.value = _pageState.value.copy(
            url = url,
            title = finalTitle,
            isLoading = false,
            progress = 1.0f,
            canGoBack = canGoBack,
            canGoForward = canGoForward,
            isSecure = url.startsWith("https://")
        )

        // Record history only if not private and not local internal home
        if (!_isPrivateMode.value && url != "prism://home" && !url.startsWith("about:")) {
            viewModelScope.launch {
                repository.recordVisit(_activeProfile.value.id, finalTitle, url)
            }
        }

        // Update active tab in DB
        _activeTab.value?.let { currentTab ->
            val updated = currentTab.copy(url = url, title = finalTitle)
            _activeTab.value = updated
            viewModelScope.launch {
                repository.saveTab(updated)
            }
        }

        checkCurrentPageBookmarked()
    }

    fun onProgressChanged(progress: Int) {
        _pageState.value = _pageState.value.copy(
            progress = progress / 100f,
            isLoading = progress < 100
        )
    }

    fun onTitleReceived(title: String) {
        _pageState.value = _pageState.value.copy(title = title)
    }

    fun onFaviconReceived(icon: Bitmap?) {
        _pageState.value = _pageState.value.copy(favicon = icon)
    }

    // Navigation Controls
    fun goBack() {
        _navigationAction.value = NavigationAction.GoBack
    }

    fun goForward() {
        _navigationAction.value = NavigationAction.GoForward
    }

    fun reload() {
        _navigationAction.value = NavigationAction.Reload
    }

    fun stopLoading() {
        _navigationAction.value = NavigationAction.StopLoading
    }

    fun goHome() {
        val homeUrl = _activeProfile.value.homepageUrl
        onUrlSubmitted(homeUrl)
    }

    fun toggleDesktopSite() {
        val current = _pageState.value.isDesktopSite
        _pageState.value = _pageState.value.copy(isDesktopSite = !current)
        reload()
    }

    // Bookmarks
    fun toggleBookmarkCurrentPage() {
        val url = _pageState.value.url
        val title = _pageState.value.title
        if (url == "prism://home" || url.isBlank()) return

        viewModelScope.launch {
            val isBookmarked = repository.isBookmarked(_activeProfile.value.id, url)
            if (isBookmarked) {
                repository.deleteBookmarkByUrl(_activeProfile.value.id, url)
                _isCurrentPageBookmarked.value = false
            } else {
                repository.addBookmark(_activeProfile.value.id, title, url)
                _isCurrentPageBookmarked.value = true
            }
        }
    }

    private fun checkCurrentPageBookmarked() {
        val url = _pageState.value.url
        viewModelScope.launch {
            _isCurrentPageBookmarked.value = repository.isBookmarked(_activeProfile.value.id, url)
        }
    }

    // Find in Page
    fun setFindInPageVisible(visible: Boolean) {
        _showFindInPage.value = visible
        if (!visible) {
            _findQuery.value = ""
            _navigationAction.value = NavigationAction.ClearFindMatches
        }
    }

    fun findNext(query: String) {
        _findQuery.value = query
        _navigationAction.value = NavigationAction.FindInPage(query, forward = true)
    }

    fun findPrevious(query: String) {
        _findQuery.value = query
        _navigationAction.value = NavigationAction.FindInPage(query, forward = false)
    }

    // Profile Management
    fun openProfileSwitcher() {
        _showProfileSwitcher.value = true
    }

    fun closeProfileSwitcher() {
        _showProfileSwitcher.value = false
    }

    fun openCreateProfileDialog() {
        _showCreateProfile.value = true
        _editingProfile.value = null
    }

    fun openEditProfileDialog(profile: ProfileEntity) {
        _editingProfile.value = profile
        _showCreateProfile.value = true
    }

    fun closeCreateEditProfileDialog() {
        _showCreateProfile.value = false
        _editingProfile.value = null
    }

    fun saveProfile(name: String, avatar: String, colorHex: Long, searchEngine: String, homepage: String) {
        viewModelScope.launch {
            val existing = _editingProfile.value
            val profile = if (existing != null) {
                existing.copy(
                    name = name,
                    avatar = avatar,
                    colorHex = colorHex,
                    searchEngine = searchEngine,
                    homepageUrl = homepage
                )
            } else {
                val newId = name.lowercase().replace(" ", "_") + "_" + System.currentTimeMillis() % 10000
                ProfileEntity(
                    id = newId,
                    name = name,
                    avatar = avatar,
                    colorHex = colorHex,
                    searchEngine = searchEngine,
                    homepageUrl = homepage
                )
            }
            repository.saveProfile(profile)
            if (existing == null || existing.id == _activeProfile.value.id) {
                _activeProfile.value = profile
            }
            closeCreateEditProfileDialog()
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            if (_allProfilesExcept(profileId).isEmpty()) {
                return@launch // Do not delete last remaining profile
            }
            repository.deleteProfile(profileId)
            profileStorageManager.clearProfileStorage(profileId)
            if (_activeProfile.value.id == profileId) {
                val nextProfile = _allProfilesExcept(profileId).first()
                switchProfile(nextProfile)
            }
        }
    }

    private fun _allProfilesExcept(id: String): List<ProfileEntity> =
        allProfiles.value.filter { it.id != id }

    fun clearProfileData(profileId: String) {
        viewModelScope.launch {
            repository.clearProfileData(profileId)
            profileStorageManager.clearProfileStorage(profileId)
            if (_activeProfile.value.id == profileId) {
                _profileBookmarks.value = emptyList()
                _profileHistory.value = emptyList()
                _profileDownloads.value = emptyList()
            }
        }
    }

    // Dialogs
    fun setSecurityDialogVisible(visible: Boolean) {
        _showSecurityDialog.value = visible
    }

    fun setThreeDotMenuVisible(visible: Boolean) {
        _showThreeDotMenu.value = visible
    }

    fun setQrScannerVisible(visible: Boolean) {
        _showQrScanner.value = visible
    }

    fun clearNavigationAction() {
        _navigationAction.value = null
    }

    // Downloads
    fun addDownloadItem(filename: String, url: String, mimeType: String, sizeBytes: Long = 0L) {
        viewModelScope.launch {
            repository.recordDownload(
                DownloadItemEntity(
                    profileId = _activeProfile.value.id,
                    filename = filename,
                    url = url,
                    mimeType = mimeType,
                    sizeBytes = sizeBytes,
                    status = "COMPLETED"
                )
            )
        }
    }

    // Shortcuts
    fun addShortcut(title: String, url: String, iconKey: String = "globe") {
        viewModelScope.launch {
            repository.addShortcut(
                ShortcutEntity(
                    profileId = _activeProfile.value.id,
                    title = title,
                    url = url,
                    iconKey = iconKey
                )
            )
        }
    }

    fun removeShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            repository.deleteShortcut(shortcut)
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    // Site Permission
    fun updateSitePermission(host: String, type: String, granted: Boolean) {
        viewModelScope.launch {
            repository.setSitePermission(_activeProfile.value.id, host, type, granted)
        }
    }
}
