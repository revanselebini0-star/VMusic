package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppleDarkElevated
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleRed
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@Composable
fun SettingsDialog(
    isOfflineMode: Boolean,
    onToggleOfflineMode: () -> Unit,
    autoShowLyrics: Boolean,
    onToggleAutoShowLyrics: (Boolean) -> Unit,
    searchBaseUrl: String = "https://server-kappa-black-72.vercel.app/api/search",
    onUpdateSearchBaseUrl: (String) -> Unit = {},
    streamBaseUrl: String = "https://vercel.app",
    onUpdateStreamBaseUrl: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    var searchInput by remember(searchBaseUrl) { mutableStateOf(searchBaseUrl) }
    var streamInput by remember(streamBaseUrl) { mutableStateOf(streamBaseUrl) }
    var searchSavedMsg by remember { mutableStateOf(false) }
    var streamSavedMsg by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("settings_dialog"),
            colors = CardDefaults.cardColors(containerColor = AppleDarkElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vanz Music Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = AppleTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search API Endpoint
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = AppleRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Search API Endpoint",
                                color = AppleTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "GET ?q=[keyword] (id, judul, artis, album, coverArt)",
                                color = AppleTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = {
                            searchInput = it
                            searchSavedMsg = false
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleRed,
                            unfocusedBorderColor = AppleDivider,
                            focusedTextColor = AppleTextPrimary,
                            unfocusedTextColor = AppleTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_api_input")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (searchSavedMsg) {
                            Text("Saved!", color = Color(0xFF34C759), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Button(
                            onClick = {
                                onUpdateSearchBaseUrl(searchInput)
                                searchSavedMsg = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Search URL", fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider(color = AppleDivider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                // Stream Endpoint Base
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CloudQueue,
                            contentDescription = null,
                            tint = AppleRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Stream Audio Endpoint",
                                color = AppleTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "GET /[id] -> returns urlAudioMurni",
                                color = AppleTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = streamInput,
                        onValueChange = {
                            streamInput = it
                            streamSavedMsg = false
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleRed,
                            unfocusedBorderColor = AppleDivider,
                            focusedTextColor = AppleTextPrimary,
                            unfocusedTextColor = AppleTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stream_api_input")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (streamSavedMsg) {
                            Text("Saved!", color = Color(0xFF34C759), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Button(
                            onClick = {
                                onUpdateStreamBaseUrl(streamInput)
                                streamSavedMsg = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Stream URL", fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider(color = AppleDivider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                // Offline Mode Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OfflinePin,
                            contentDescription = null,
                            tint = AppleRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = "Offline Mode",
                                color = AppleTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Play downloaded tracks without internet",
                                color = AppleTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = { onToggleOfflineMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppleRed,
                            uncheckedTrackColor = Color(0xFF333336)
                        )
                    )
                }

                HorizontalDivider(color = AppleDivider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                // Auto Synchronized Lyrics Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = null,
                            tint = AppleRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = "Auto-Show Lyrics",
                                color = AppleTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Open synchronized lyrics automatically",
                                color = AppleTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = autoShowLyrics,
                        onCheckedChange = onToggleAutoShowLyrics,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppleRed,
                            uncheckedTrackColor = Color(0xFF333336)
                        )
                    )
                }

                HorizontalDivider(color = AppleDivider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                // Audio Quality preset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = AppleRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(
                            text = "Audio Player Engine",
                            color = AppleTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Media3 ExoPlayer • Full Background Audio",
                            color = AppleTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
