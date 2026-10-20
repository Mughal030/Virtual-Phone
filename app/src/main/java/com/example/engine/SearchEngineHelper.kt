package com.example.engine

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object SearchEngineHelper {
    private val URL_PATTERN = Pattern.compile(
        "^(https?://)?" + // optional scheme
        "(([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}|localhost|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})" + // domain or IP
        "(:\\d{1,5})?" + // port
        "(/.*)?$" // path
    )

    fun resolveInputToUrl(input: String, searchEngine: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return "prism://home"
        if (trimmed == "prism://home" || trimmed.startsWith("about:")) return trimmed

        // Check if input looks like a URL
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }

        // If it doesn't contain spaces and matches domain pattern or has a dot with known TLD
        if (!trimmed.contains(" ") && (URL_PATTERN.matcher(trimmed).matches() || trimmed.contains("."))) {
            return "https://$trimmed"
        }

        // Otherwise treat as search query
        return buildSearchUrl(trimmed, searchEngine)
    }

    fun buildSearchUrl(query: String, searchEngine: String): String {
        val encoded = try {
            URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            query
        }

        return when (searchEngine.lowercase()) {
            "duckduckgo" -> "https://duckduckgo.com/?q=$encoded"
            "bing" -> "https://www.bing.com/search?q=$encoded"
            "yahoo" -> "https://search.yahoo.com/search?p=$encoded"
            "ecosia" -> "https://www.ecosia.org/search?q=$encoded"
            else -> "https://www.google.com/search?q=$encoded"
        }
    }

    fun extractDomain(url: String): String {
        return try {
            if (url == "prism://home") return "New Tab"
            val uri = java.net.URI(url)
            val host = uri.host ?: url
            host.removePrefix("www.")
        } catch (e: Exception) {
            url
        }
    }

    val defaultQuickSearchSuggestions = listOf(
        "Artificial Intelligence news",
        "Android Jetpack Compose",
        "Kotlin Multiplatform",
        "World News today",
        "Web development trends",
        "NASA James Webb discoveries",
        "Open source projects GitHub"
    )
}
