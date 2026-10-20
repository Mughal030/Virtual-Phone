package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.BrowserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsPreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("prism_settings", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<BrowserSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): BrowserSettings {
        return BrowserSettings(
            themeMode = prefs.getString("themeMode", "SYSTEM") ?: "SYSTEM",
            defaultSearchEngine = prefs.getString("defaultSearchEngine", "Google") ?: "Google",
            defaultHomepage = prefs.getString("defaultHomepage", "prism://home") ?: "prism://home",
            defaultProfileId = prefs.getString("defaultProfileId", "personal") ?: "personal",
            javascriptEnabled = prefs.getBoolean("javascriptEnabled", true),
            desktopSiteDefault = prefs.getBoolean("desktopSiteDefault", false),
            blockPopups = prefs.getBoolean("blockPopups", true),
            doNotTrack = prefs.getBoolean("doNotTrack", true),
            safeBrowsing = prefs.getBoolean("safeBrowsing", true),
            textZoom = prefs.getInt("textZoom", 100),
            toolbarPosition = prefs.getString("toolbarPosition", "BOTTOM") ?: "BOTTOM",
            showHomepageShortcuts = prefs.getBoolean("showHomepageShortcuts", true),
            showNewsFeed = prefs.getBoolean("showNewsFeed", true)
        )
    }

    fun updateSettings(update: (BrowserSettings) -> BrowserSettings) {
        val current = _settingsFlow.value
        val newSettings = update(current)
        prefs.edit().apply {
            putString("themeMode", newSettings.themeMode)
            putString("defaultSearchEngine", newSettings.defaultSearchEngine)
            putString("defaultHomepage", newSettings.defaultHomepage)
            putString("defaultProfileId", newSettings.defaultProfileId)
            putBoolean("javascriptEnabled", newSettings.javascriptEnabled)
            putBoolean("desktopSiteDefault", newSettings.desktopSiteDefault)
            putBoolean("blockPopups", newSettings.blockPopups)
            putBoolean("doNotTrack", newSettings.doNotTrack)
            putBoolean("safeBrowsing", newSettings.safeBrowsing)
            putInt("textZoom", newSettings.textZoom)
            putString("toolbarPosition", newSettings.toolbarPosition)
            putBoolean("showHomepageShortcuts", newSettings.showHomepageShortcuts)
            putBoolean("showNewsFeed", newSettings.showNewsFeed)
            apply()
        }
        _settingsFlow.value = newSettings
    }
}
