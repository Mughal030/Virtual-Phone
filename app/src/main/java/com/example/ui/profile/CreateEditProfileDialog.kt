package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProfileEntity
import com.example.ui.components.PredefinedProfileAvatars
import com.example.ui.components.PredefinedProfileColors
import com.example.ui.components.getProfileAvatarVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditProfileDialog(
    editingProfile: ProfileEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, avatar: String, colorHex: Long, searchEngine: String, homepage: String) -> Unit
) {
    val isEditing = editingProfile != null
    var name by remember { mutableStateOf(editingProfile?.name ?: "") }
    var selectedAvatar by remember { mutableStateOf(editingProfile?.avatar ?: "person") }
    var selectedColor by remember { mutableLongStateOf(editingProfile?.colorHex ?: 0xFF2563EBL) }
    var selectedSearchEngine by remember { mutableStateOf(editingProfile?.searchEngine ?: "Google") }
    var homepageUrl by remember { mutableStateOf(editingProfile?.homepageUrl ?: "prism://home") }

    val searchEngines = listOf("Google", "DuckDuckGo", "Bing", "Yahoo", "Ecosia")
    var searchEngineExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Profile" else "Create New Profile",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
                    placeholder = { Text("e.g. Work, Research, Personal") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Icon Picker
                Text(
                    text = "Choose Icon",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PredefinedProfileAvatars.take(4).forEach { (key, label) ->
                        val isSelected = selectedAvatar == key
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(selectedColor)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .then(
                                    if (isSelected) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else Modifier
                                )
                                .clickable { selectedAvatar = key }
                                .testTag("avatar_option_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getProfileAvatarVector(key),
                                contentDescription = label,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PredefinedProfileAvatars.drop(4).take(4).forEach { (key, label) ->
                        val isSelected = selectedAvatar == key
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(selectedColor)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .then(
                                    if (isSelected) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else Modifier
                                )
                                .clickable { selectedAvatar = key }
                                .testTag("avatar_option_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getProfileAvatarVector(key),
                                contentDescription = label,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color Palette Picker
                Text(
                    text = "Profile Theme Color",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PredefinedProfileColors.take(4).forEach { (colorHex, _) ->
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .clickable { selectedColor = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    PredefinedProfileColors.drop(4).take(4).forEach { (colorHex, _) ->
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .clickable { selectedColor = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Default Search Engine dropdown
                ExposedDropdownMenuBox(
                    expanded = searchEngineExpanded,
                    onExpandedChange = { searchEngineExpanded = !searchEngineExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSearchEngine,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Search Engine") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = searchEngineExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = searchEngineExpanded,
                        onDismissRequest = { searchEngineExpanded = false }
                    ) {
                        searchEngines.forEach { engine ->
                            DropdownMenuItem(
                                text = { Text(engine) },
                                onClick = {
                                    selectedSearchEngine = engine
                                    searchEngineExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Homepage URL
                OutlinedTextField(
                    value = homepageUrl,
                    onValueChange = { homepageUrl = it },
                    label = { Text("Homepage URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedAvatar, selectedColor, selectedSearchEngine, homepageUrl.trim())
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text(if (isEditing) "Save Changes" else "Create Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
