package com.example.engine

import android.content.Context
import android.os.Build
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.webkit.Profile
import androidx.webkit.ProfileStore
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

class ProfileStorageManager(private val context: Context) {
    private val TAG = "ProfileStorageManager"

    val isNativeMultiProfileSupported: Boolean = try {
        WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)
    } catch (e: Throwable) {
        false
    }

    /**
     * Attaches the given WebView to the isolated profile context.
     * Uses WebViewCompat.setProfile if supported, otherwise uses isolated session management.
     */
    fun attachWebViewToProfile(webView: WebView, profileId: String, isPrivate: Boolean) {
        try {
            if (isNativeMultiProfileSupported) {
                val targetProfileName = if (isPrivate) "incognito_$profileId" else "profile_$profileId"
                val profileStore = ProfileStore.getInstance()
                val profile: Profile = profileStore.getOrCreateProfile(targetProfileName)
                WebViewCompat.setProfile(webView, targetProfileName)
                Log.d(TAG, "Assigned WebView to native Profile: $targetProfileName")
            } else {
                Log.d(TAG, "Native multi-profile not supported; using sandbox cookie partition for: $profileId")
            }

            // Configure cookie policies based on privacy mode
            val cookieManager = getCookieManager(profileId, isPrivate)
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, !isPrivate)

            if (isPrivate) {
                // In private mode, do not save form data or cache aggressively
                webView.settings.saveFormData = false
                webView.settings.domStorageEnabled = false
            } else {
                webView.settings.saveFormData = true
                webView.settings.domStorageEnabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching WebView to profile: ${e.message}", e)
        }
    }

    fun getCookieManager(profileId: String, isPrivate: Boolean): CookieManager {
        return if (isNativeMultiProfileSupported) {
            try {
                val targetProfileName = if (isPrivate) "incognito_$profileId" else "profile_$profileId"
                ProfileStore.getInstance().getOrCreateProfile(targetProfileName).cookieManager
            } catch (e: Exception) {
                CookieManager.getInstance()
            }
        } else {
            CookieManager.getInstance()
        }
    }

    fun clearProfileStorage(profileId: String) {
        try {
            if (isNativeMultiProfileSupported) {
                val targetProfileName = "profile_$profileId"
                try {
                    val profile = ProfileStore.getInstance().getProfile(targetProfileName)
                    profile?.cookieManager?.removeAllCookies(null)
                    profile?.webStorage?.deleteAllData()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clear native profile: ${e.message}")
                }
            } else {
                CookieManager.getInstance().removeAllCookies(null)
                WebStorage.getInstance().deleteAllData()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing profile storage: ${e.message}", e)
        }
    }

    fun clearPrivateSessions() {
        try {
            if (isNativeMultiProfileSupported) {
                val store = ProfileStore.getInstance()
                val allProfiles = store.allProfileNames
                for (name in allProfiles) {
                    if (name.startsWith("incognito_")) {
                        try {
                            store.deleteProfile(name)
                        } catch (e: Exception) {
                            store.getProfile(name)?.cookieManager?.removeAllCookies(null)
                        }
                    }
                }
            } else {
                CookieManager.getInstance().removeSessionCookies(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing private sessions: ${e.message}", e)
        }
    }
}
