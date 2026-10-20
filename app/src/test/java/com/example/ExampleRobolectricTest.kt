package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.SearchEngineHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Prism", appName)
    }

    @Test
    fun `test url resolver for domain name`() {
        val resolved = SearchEngineHelper.resolveInputToUrl("wikipedia.org", "Google")
        assertEquals("https://wikipedia.org", resolved)
    }

    @Test
    fun `test url resolver for search query`() {
        val resolved = SearchEngineHelper.resolveInputToUrl("jetpack compose", "Google")
        assertTrue(resolved.startsWith("https://www.google.com/search?q="))
    }

    @Test
    fun `test domain extraction`() {
        val domain = SearchEngineHelper.extractDomain("https://www.github.com/torvalds/linux")
        assertEquals("github.com", domain)
    }
}
