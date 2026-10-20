package com.example.data.model

data class BrowserSettings(
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val defaultSearchEngine: String = "Google",
    val defaultHomepage: String = "prism://home",
    val defaultProfileId: String = "personal",
    val javascriptEnabled: Boolean = true,
    val desktopSiteDefault: Boolean = false,
    val blockPopups: Boolean = true,
    val doNotTrack: Boolean = true,
    val safeBrowsing: Boolean = true,
    val textZoom: Int = 100, // 50 to 150 %
    val toolbarPosition: String = "BOTTOM", // BOTTOM, TOP
    val showHomepageShortcuts: Boolean = true,
    val showNewsFeed: Boolean = true
)
