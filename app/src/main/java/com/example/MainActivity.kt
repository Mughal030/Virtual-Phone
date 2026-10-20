package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.BrowserScreen
import com.example.ui.BrowserViewModel
import com.example.ui.ScreenDestination
import com.example.ui.bookmarks.BookmarksScreen
import com.example.ui.downloads.DownloadsScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.profile.ProfileManagementScreen
import com.example.ui.settings.AboutScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SitePermissionsScreen
import com.example.ui.tabs.TabSwitcherScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            MyApplicationTheme(themeMode = settings.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "ScreenTransition"
                    ) { destination ->
                        when (destination) {
                            ScreenDestination.BROWSER -> BrowserScreen(viewModel = viewModel)
                            ScreenDestination.TAB_SWITCHER -> TabSwitcherScreen(viewModel = viewModel)
                            ScreenDestination.PROFILE_MANAGEMENT -> ProfileManagementScreen(viewModel = viewModel)
                            ScreenDestination.BOOKMARKS -> BookmarksScreen(viewModel = viewModel)
                            ScreenDestination.HISTORY -> HistoryScreen(viewModel = viewModel)
                            ScreenDestination.DOWNLOADS -> DownloadsScreen(viewModel = viewModel)
                            ScreenDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            ScreenDestination.SITE_PERMISSIONS -> SitePermissionsScreen(viewModel = viewModel)
                            ScreenDestination.ABOUT -> AboutScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
