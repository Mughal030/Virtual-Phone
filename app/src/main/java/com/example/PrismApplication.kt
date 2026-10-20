package com.example

import android.app.Application
import android.os.Build
import android.util.Log
import java.io.File

class PrismApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ensureWebViewDirectories()
    }

    private fun ensureWebViewDirectories() {
        try {
            val baseCache = cacheDir
            if (baseCache != null) {
                baseCache.setReadable(true, false)
                baseCache.setWritable(true, false)
                baseCache.setExecutable(true, false)

                // Pre-create the directory hierarchy Chromium's Simple Cache Backend expects
                val webViewCache = File(baseCache, "WebView")
                val profiles = listOf("Default", "profile_personal", "profile_work", "profile_study")
                
                for (profileName in profiles) {
                    val profileDir = File(webViewCache, profileName)
                    val httpCache = File(profileDir, "HTTP Cache")
                    val codeCache = File(httpCache, "Code Cache")
                    val jsCache = File(codeCache, "js")
                    val wasmCache = File(codeCache, "wasm")

                    listOf(webViewCache, profileDir, httpCache, codeCache, jsCache, wasmCache).forEach { dir ->
                        if (!dir.exists()) {
                            dir.mkdirs()
                        }
                        dir.setReadable(true, false)
                        dir.setWritable(true, false)
                        dir.setExecutable(true, false)
                    }
                }
            }

            // Also ensure dataDir subdirectories exist with proper permissions (API 24+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val data = dataDir
                if (data != null && data.exists()) {
                    data.setReadable(true, false)
                    data.setWritable(true, false)
                    data.setExecutable(true, false)

                    val appWebview = File(data, "app_webview")
                    if (appWebview.exists()) {
                        // Purge any corrupted or un-signed variations seed files
                        val seedFiles = appWebview.listFiles { file ->
                            file.name.startsWith("variations_seed")
                        }
                        seedFiles?.forEach { file ->
                            try {
                                file.delete()
                            } catch (e: Exception) {
                                Log.w("PrismApplication", "Could not delete seed file: ${file.name}")
                            }
                        }
                    }

                    val profiles = listOf("Default", "profile_personal", "profile_work", "profile_study")
                    for (profileName in profiles) {
                        val profileDir = File(appWebview, profileName)
                        val httpCache = File(profileDir, "HTTP Cache")
                        val codeCache = File(httpCache, "Code Cache")
                        val jsCache = File(codeCache, "js")

                        listOf(appWebview, profileDir, httpCache, codeCache, jsCache).forEach { dir ->
                            if (!dir.exists()) {
                                dir.mkdirs()
                            }
                            dir.setReadable(true, false)
                            dir.setWritable(true, false)
                            dir.setExecutable(true, false)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("PrismApplication", "Failed to pre-initialize WebView directories: ${e.message}")
        }
    }
}
