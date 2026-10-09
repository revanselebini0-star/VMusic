package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlaylistEntity
import com.example.data.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.AppleBlack
import com.example.ui.theme.AppleDarkCard
import com.example.ui.theme.AppleDarkElevated
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleRed
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@Composable
fun LibraryScreen(
    allSongs: List<Song>,
    downloadedSongs: List<Song>,
    favoriteSongs: List<Song>,
    playlists: List<PlaylistEntity>,
    currentSong: Song?,
    isPlaying: Boolean,
    isOfflineMode: Boolean,
    onToggleOfflineMode: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onSelectPlaylist: (PlaylistEntity) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onToggleDownload: (Song) -> Unit,
    onImportLocalAudio: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportLocalAudio(uri)
        }
    }

    val displaySongs = when (selectedFilter) {
        "DOWNLOADED" -> downloadedSongs
        "FAVORITES" -> favoriteSongs
        else -> if (isOfflineMode) downloadedSongs else allSongs
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppleBlack)
            .testTag("library_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Text(
                text = "Library",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = AppleTextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // Offline Mode Control Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("offline_mode_card"),
                colors = CardDefaults.cardColors(containerColor = AppleDarkElevated),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isOfflineMode) AppleRed else Color(0xFF333336)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.OfflinePin,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Offline Mode",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleTextPrimary
                            )
                            Text(
                                text = "${downloadedSongs.size} tracks saved for offline",
                                fontSize = 13.sp,
                                color = AppleTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = { onToggleOfflineMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppleRed,
                            uncheckedTrackColor = Color(0xFF3A3A3C)
                        )
                    )
                }
            }
        }

        // Apple Music Library Nav list
        item {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                LibraryNavRow(
                    icon = Icons.Filled.QueueMusic,
                    title = "Playlists",
                    count = "${playlists.size}",
                    onClick = { selectedFilter = "ALL" }
                )
                LibraryNavRow(
                    icon = Icons.Filled.CloudDone,
                    title = "Downloaded Music",
                    count = "${downloadedSongs.size}",
                    isSelected = selectedFilter == "DOWNLOADED",
                    onClick = { selectedFilter = "DOWNLOADED" }
                )
                LibraryNavRow(
                    icon = Icons.Filled.Favorite,
                    title = "Favorites",
                    count = "${favoriteSongs.size}",
                    isSelected = selectedFilter == "FAVORITES",
                    onClick = { selectedFilter = "FAVORITES" }
                )
                LibraryNavRow(
                    icon = Icons.Filled.MusicNote,
                    title = "All Songs",
                    count = "${allSongs.size}",
                    isSelected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" }
                )
            }
        }

        // Import Local Music Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("import_audio_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppleDarkCard,
                        contentColor = AppleRed
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Import Music from Device Storage",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // List Header
        item {
            Text(
                text = when (selectedFilter) {
                    "DOWNLOADED" -> "Downloaded Songs (${downloadedSongs.size})"
                    "FAVORITES" -> "Favorite Tracks (${favoriteSongs.size})"
                    else -> "Recently Added Songs (${displaySongs.size})"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppleTextPrimary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            )
        }

        if (displaySongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs found in this category",
                        color = AppleTextSecondary,
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            items(displaySongs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isCurrentSong = song.id == currentSong?.id,
                    isPlaying = isPlaying && song.id == currentSong?.id,
                    onClick = { onPlaySong(song) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onToggleDownload = { onToggleDownload(song) },
                    onAddToPlaylist = { }
                )
            }
        }
    }
}

@Composable
private fun LibraryNavRow(
    icon: ImageVector,
    title: String,
    count: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) AppleRed else AppleRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) AppleRed else AppleTextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = count,
                    fontSize = 15.sp,
                    color = AppleTextSecondary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF48484A),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        HorizontalDivider(color = AppleDivider, thickness = 0.5.dp)
    }
}
