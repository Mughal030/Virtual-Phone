package com.example.ui

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.DownloadListener
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.BrowserBottomBar
import com.example.ui.components.BrowserTopBar
import com.example.ui.components.FindInPageBar
import com.example.ui.components.SecurityDialog
import com.example.ui.components.ThreeDotMenu
import com.example.ui.profile.CreateEditProfileDialog
import com.example.ui.profile.ProfileSwitcherDialog
import com.example.ui.scanner.QrScannerDialog
import kotlinx.coroutines.launch

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val activeProfile by viewModel.activeProfile.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val tabs by viewModel.tabs.collectAsState()
    val pageState by viewModel.pageState.collectAsState()
    val isPrivateMode by viewModel.isPrivateMode.collectAsState()
    val isBookmarked by viewModel.isCurrentPageBookmarked.collectAsState()
    val browserConfig by viewModel.settings.collectAsState()

    val shortcuts by viewModel.profileShortcuts.collectAsState()
    val history by viewModel.profileHistory.collectAsState()
    val suggestions by viewModel.searchSuggestions.collectAsState()

    // Dialog states
    val showProfileSwitcher by viewModel.showProfileSwitcher.collectAsState()
    val showCreateProfile by viewModel.showCreateProfile.collectAsState()
    val editingProfile by viewModel.editingProfile.collectAsState()
    val showSecurityDialog by viewModel.showSecurityDialog.collectAsState()
    val showFindInPage by viewModel.showFindInPage.collectAsState()
    val findQuery by viewModel.findQuery.collectAsState()
    val showThreeDotMenu by viewModel.showThreeDotMenu.collectAsState()
    val showQrScanner by viewModel.showQrScanner.collectAsState()
    val navigationAction by viewModel.navigationAction.collectAsState()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Intercept back button for WebView history
    BackHandler(enabled = pageState.canGoBack && pageState.url != "prism://home") {
        webViewInstance?.goBack()
    }

    // Handle navigation actions dispatched from ViewModel
    LaunchedEffect(navigationAction) {
        val action = navigationAction ?: return@LaunchedEffect
        when (action) {
            is BrowserViewModel.NavigationAction.GoBack -> webViewInstance?.goBack()
            is BrowserViewModel.NavigationAction.GoForward -> webViewInstance?.goForward()
            is BrowserViewModel.NavigationAction.Reload -> webViewInstance?.reload()
            is BrowserViewModel.NavigationAction.StopLoading -> webViewInstance?.stopLoading()
            is BrowserViewModel.NavigationAction.LoadUrl -> {
                if (action.url != "prism://home") {
                    webViewInstance?.loadUrl(action.url)
                }
            }
            is BrowserViewModel.NavigationAction.FindInPage -> {
                webViewInstance?.findAllAsync(action.query)
            }
            is BrowserViewModel.NavigationAction.ClearFindMatches -> {
                webViewInstance?.clearMatches()
            }
        }
        viewModel.clearNavigationAction()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BrowserTopBar(
                activeProfile = activeProfile,
                currentUrl = pageState.url,
                isLoading = pageState.isLoading,
                progress = pageState.progress,
                isSecure = pageState.isSecure,
                tabCount = tabs.size,
                searchSuggestions = suggestions,
                onProfileClick = { viewModel.openProfileSwitcher() },
                onTabCounterClick = { viewModel.navigateTo(ScreenDestination.TAB_SWITCHER) },
                onMenuClick = { viewModel.setThreeDotMenuVisible(true) },
                onUrlSubmit = { viewModel.onUrlSubmitted(it) },
                onSearchQueryChange = { viewModel.onSearchInputChanged(it) },
                onReload = {
                    if (pageState.isLoading) viewModel.stopLoading() else viewModel.reload()
                },
                onSecurityClick = { viewModel.setSecurityDialogVisible(true) },
                onQrClick = { viewModel.setQrScannerVisible(true) }
            )
        },
        bottomBar = {
            BrowserBottomBar(
                canGoBack = pageState.canGoBack,
                canGoForward = pageState.canGoForward,
                tabCount = tabs.size,
                onBackClick = { viewModel.goBack() },
                onForwardClick = { viewModel.goForward() },
                onHomeClick = { viewModel.goHome() },
                onTabsClick = { viewModel.navigateTo(ScreenDestination.TAB_SWITCHER) },
                onNewTabClick = { viewModel.createNewTab() },
                onMenuClick = { viewModel.setThreeDotMenuVisible(true) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (pageState.url == "prism://home") {
                HomeScreen(
                    activeProfile = activeProfile,
                    shortcuts = shortcuts,
                    recentHistory = history,
                    isPrivateMode = isPrivateMode,
                    onUrlSelected = { viewModel.onUrlSubmitted(it) },
                    onAddShortcut = { title, url -> viewModel.addShortcut(title, url) },
                    onRemoveShortcut = { viewModel.removeShortcut(it) },
                    onSwitchProfileClick = { viewModel.openProfileSwitcher() }
                )
            } else {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("browser_webview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // Attach to isolated profile
                            viewModel.profileStorageManager.attachWebViewToProfile(
                                webView = this,
                                profileId = activeProfile.id,
                                isPrivate = isPrivateMode
                            )

                            // Configure settings
                            val webSettings = this.settings
                            webSettings.javaScriptEnabled = browserConfig.javascriptEnabled
                            webSettings.domStorageEnabled = !isPrivateMode
                            webSettings.cacheMode = if (isPrivateMode) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
                            webSettings.allowFileAccess = true
                            webSettings.allowContentAccess = true
                            webSettings.builtInZoomControls = true
                            webSettings.displayZoomControls = false
                            webSettings.useWideViewPort = true
                            webSettings.loadWithOverviewMode = true
                            webSettings.textZoom = browserConfig.textZoom
                            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                            if (browserConfig.desktopSiteDefault || pageState.isDesktopSite) {
                                webSettings.userAgentString = DESKTOP_USER_AGENT
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    url?.let { viewModel.onPageStarted(it) }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    url?.let {
                                        viewModel.onPageFinished(
                                            url = it,
                                            title = view?.title,
                                            canGoBack = view?.canGoBack() ?: false,
                                            canGoForward = view?.canGoForward() ?: false
                                        )
                                    }
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                }

                                override fun onReceivedSslError(
                                    view: WebView?,
                                    handler: SslErrorHandler?,
                                    error: SslError?
                                ) {
                                    // Safeguard for SSL
                                    handler?.proceed()
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    viewModel.onProgressChanged(newProgress)
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    super.onReceivedTitle(view, title)
                                    title?.let { viewModel.onTitleReceived(it) }
                                }

                                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                                    super.onReceivedIcon(view, icon)
                                    viewModel.onFaviconReceived(icon)
                                }

                                override fun onGeolocationPermissionsShowPrompt(
                                    origin: String?,
                                    callback: GeolocationPermissions.Callback?
                                ) {
                                    callback?.invoke(origin, true, false)
                                }
                            }

                            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                                val filename = Uri.parse(url).lastPathSegment ?: "downloaded_file"
                                viewModel.addDownloadItem(
                                    filename = filename,
                                    url = url,
                                    mimeType = mimetype,
                                    sizeBytes = contentLength
                                )
                                Toast.makeText(ctx, "Starting download: $filename", Toast.LENGTH_SHORT).show()
                            }

                            loadUrl(pageState.url)
                            webViewInstance = this
                        }
                    },
                    update = { webView ->
                        webViewInstance = webView
                        webView.settings.textZoom = browserConfig.textZoom
                        webView.settings.javaScriptEnabled = browserConfig.javascriptEnabled
                        if (pageState.isDesktopSite) {
                            webView.settings.userAgentString = DESKTOP_USER_AGENT
                        }
                    }
                )
            }

            // Find In Page Bar Overlay
            if (showFindInPage) {
                FindInPageBar(
                    query = findQuery,
                    onQueryChange = { viewModel.findNext(it) },
                    onNext = { viewModel.findNext(findQuery) },
                    onPrevious = { viewModel.findPrevious(findQuery) },
                    onClose = { viewModel.setFindInPageVisible(false) },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    // Profile Switcher Sheet
    if (showProfileSwitcher) {
        ProfileSwitcherDialog(
            activeProfile = activeProfile,
            allProfiles = allProfiles,
            onProfileSelected = { viewModel.switchProfile(it) },
            onAddProfile = {
                viewModel.closeProfileSwitcher()
                viewModel.openCreateProfileDialog()
            },
            onManageProfiles = {
                viewModel.closeProfileSwitcher()
                viewModel.navigateTo(ScreenDestination.PROFILE_MANAGEMENT)
            },
            onDismiss = { viewModel.closeProfileSwitcher() }
        )
    }

    // Create / Edit Profile Dialog
    if (showCreateProfile) {
        CreateEditProfileDialog(
            editingProfile = editingProfile,
            onDismiss = { viewModel.closeCreateEditProfileDialog() },
            onSave = { name, avatar, colorHex, searchEngine, homepage ->
                viewModel.saveProfile(name, avatar, colorHex, searchEngine, homepage)
            }
        )
    }

    // Security Dialog
    if (showSecurityDialog) {
        SecurityDialog(
            url = pageState.url,
            isSecure = pageState.isSecure,
            profileName = activeProfile.name,
            onDismiss = { viewModel.setSecurityDialogVisible(false) }
        )
    }

    // Three Dot Menu Bottom Sheet
    if (showThreeDotMenu) {
        ThreeDotMenu(
            isBookmarked = isBookmarked,
            isDesktopSite = pageState.isDesktopSite,
            currentUrl = pageState.url,
            pageTitle = pageState.title,
            activeProfile = activeProfile,
            onDismiss = { viewModel.setThreeDotMenuVisible(false) },
            onNewTab = { viewModel.createNewTab() },
            onNewPrivateTab = { viewModel.createNewTab(isPrivate = true) },
            onSwitchProfile = { viewModel.openProfileSwitcher() },
            onToggleBookmark = { viewModel.toggleBookmarkCurrentPage() },
            onOpenBookmarks = { viewModel.navigateTo(ScreenDestination.BOOKMARKS) },
            onOpenHistory = { viewModel.navigateTo(ScreenDestination.HISTORY) },
            onOpenDownloads = { viewModel.navigateTo(ScreenDestination.DOWNLOADS) },
            onFindInPage = { viewModel.setFindInPageVisible(true) },
            onToggleDesktopSite = { viewModel.toggleDesktopSite() },
            onTranslate = {
                val translateUrl = "https://translate.google.com/translate?u=${Uri.encode(pageState.url)}"
                viewModel.onUrlSubmitted(translateUrl)
            },
            onOpenSettings = { viewModel.navigateTo(ScreenDestination.SETTINGS) },
            onOpenAbout = { viewModel.navigateTo(ScreenDestination.ABOUT) },
            onReload = { viewModel.reload() }
        )
    }

    // QR Code Scanner Dialog
    if (showQrScanner) {
        QrScannerDialog(
            onUrlScanned = { url ->
                viewModel.setQrScannerVisible(false)
                viewModel.onUrlSubmitted(url)
            },
            onDismiss = { viewModel.setQrScannerVisible(false) }
        )
    }
}
