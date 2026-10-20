package com.example.ui.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BrowserViewModel
import com.example.ui.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val context = LocalContext.current

    var showClearDataDialog by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateTo(ScreenDestination.BROWSER)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.BROWSER) },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // General
            SectionHeader(title = "General")

            // Default Search Engine
            val searchEngines = listOf("Google", "DuckDuckGo", "Bing", "Yahoo", "Ecosia")
            var engineExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = engineExpanded,
                onExpandedChange = { engineExpanded = !engineExpanded }
            ) {
                OutlinedTextField(
                    value = settings.defaultSearchEngine,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Default Search Engine") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = engineExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = engineExpanded,
                    onDismissRequest = { engineExpanded = false }
                ) {
                    searchEngines.forEach { engine ->
                        DropdownMenuItem(
                            text = { Text(engine) },
                            onClick = {
                                viewModel.repository.updateSettings { it.copy(defaultSearchEngine = engine) }
                                engineExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Homepage URL
            var homepage by remember(settings.defaultHomepage) { mutableStateOf(settings.defaultHomepage) }
            OutlinedTextField(
                value = homepage,
                onValueChange = {
                    homepage = it
                    viewModel.repository.updateSettings { s -> s.copy(defaultHomepage = it) }
                },
                label = { Text("Default Homepage") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy & Security
            SectionHeader(title = "Privacy & Security")

            SwitchSettingItem(
                icon = Icons.Default.Shield,
                title = "Safe Browsing",
                subtitle = "Protects against malicious sites and harmful downloads",
                checked = settings.safeBrowsing,
                onCheckedChange = { checked ->
                    viewModel.repository.updateSettings { it.copy(safeBrowsing = checked) }
                }
            )

            SwitchSettingItem(
                icon = Icons.Default.Security,
                title = "Do Not Track (DNT)",
                subtitle = "Send a request not to track your browsing traffic",
                checked = settings.doNotTrack,
                onCheckedChange = { checked ->
                    viewModel.repository.updateSettings { it.copy(doNotTrack = checked) }
                }
            )

            SwitchSettingItem(
                icon = Icons.Default.Block,
                title = "Block Pop-ups",
                subtitle = "Prevents websites from displaying automatic popup windows",
                checked = settings.blockPopups,
                onCheckedChange = { checked ->
                    viewModel.repository.updateSettings { it.copy(blockPopups = checked) }
                }
            )

            ClickableSettingItem(
                icon = Icons.Default.VpnKey,
                title = "Site Permissions Manager",
                subtitle = "Control Camera, Geolocation, and Notification permissions",
                onClick = { viewModel.navigateTo(ScreenDestination.SITE_PERMISSIONS) }
            )

            ClickableSettingItem(
                icon = Icons.Default.Delete,
                title = "Clear Browsing Data",
                subtitle = "Clear history, cookies, and cache for ${activeProfile.name}",
                onClick = { showClearDataDialog = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Appearance & Layout
            SectionHeader(title = "Appearance")

            // Theme Selection
            val themes = listOf("SYSTEM" to "System Default", "LIGHT" to "Light Mode", "DARK" to "Dark Mode")
            var themeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = themeExpanded,
                onExpandedChange = { themeExpanded = !themeExpanded }
            ) {
                OutlinedTextField(
                    value = themes.firstOrNull { it.first == settings.themeMode }?.second ?: "System Default",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Theme") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = themeExpanded,
                    onDismissRequest = { themeExpanded = false }
                ) {
                    themes.forEach { (mode, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.repository.updateSettings { it.copy(themeMode = mode) }
                                themeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text Zoom
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Text Scaling", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(text = "${settings.textZoom}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = settings.textZoom.toFloat(),
                    onValueChange = { value ->
                        viewModel.repository.updateSettings { it.copy(textZoom = value.toInt()) }
                    },
                    valueRange = 50f..150f,
                    steps = 9
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Advanced
            SectionHeader(title = "Advanced")

            SwitchSettingItem(
                icon = Icons.Default.Javascript,
                title = "JavaScript",
                subtitle = "Enable JavaScript execution on web pages",
                checked = settings.javascriptEnabled,
                onCheckedChange = { checked ->
                    viewModel.repository.updateSettings { it.copy(javascriptEnabled = checked) }
                }
            )

            SwitchSettingItem(
                icon = Icons.Default.DesktopMac,
                title = "Always Request Desktop Site",
                subtitle = "Request desktop version of all websites by default",
                checked = settings.desktopSiteDefault,
                onCheckedChange = { checked ->
                    viewModel.repository.updateSettings { it.copy(desktopSiteDefault = checked) }
                }
            )

            ClickableSettingItem(
                icon = Icons.Default.Info,
                title = "About Prism Browser",
                subtitle = "Version, rendering engine, and architecture details",
                onClick = { viewModel.navigateTo(ScreenDestination.ABOUT) }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear Browsing Data?") },
            text = {
                Text(
                    "This will clear history, cookies, and web storage for the active profile \"${activeProfile.name}\"."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearProfileData(activeProfile.id)
                        showClearDataDialog = false
                        Toast.makeText(context, "Data cleared for ${activeProfile.name}", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SwitchSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ClickableSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
