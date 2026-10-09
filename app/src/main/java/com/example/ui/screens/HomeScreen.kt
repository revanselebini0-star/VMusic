package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.PlaylistEntity
import com.example.data.model.Song
import com.example.ui.components.RecentlyPlayedRow
import com.example.ui.components.SongListItem
import com.example.ui.components.TopPicksCarousel
import com.example.ui.theme.AppleBlack
import com.example.ui.theme.AppleDarkCard
import com.example.ui.theme.AppleDarkElevated
import com.example.ui.theme.AppleRed
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@Composable
fun HomeScreen(
    allSongs: List<Song>,
    recentlyPlayed: List<Song>,
    playlists: List<PlaylistEntity>,
    currentSong: Song?,
    isPlaying: Boolean,
    isOfflineMode: Boolean,
    onToggleOfflineMode: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onPlaySongById: (String) -> Unit,
    onSelectPlaylist: (PlaylistEntity) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onToggleDownload: (Song) -> Unit,
    onOpenSettings: () -> Unit,
    isSyncing: Boolean = false,
    onRefreshYouTube: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showOverflowMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppleBlack)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Header matching the screenshot:
        // Left: "Home" (bold 34sp)
        // Right: Red 3-dots and Avatar photo
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Home",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Red 3-dots overflow menu (exactly matching the red 3-dots in screenshot)
                    Box {
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            modifier = Modifier.testTag("home_overflow_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Menu",
                                tint = AppleRed,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                            modifier = Modifier.background(AppleDarkElevated)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isOfflineMode) "Disable Offline Mode" else "Enable Offline Mode",
                                        color = AppleTextPrimary
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isOfflineMode) Icons.Filled.OfflinePin else Icons.Filled.CloudOff,
                                        contentDescription = null,
                                        tint = AppleRed
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onToggleOfflineMode()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Sync with Music Server", color = AppleTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        tint = AppleRed
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onRefreshYouTube()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Audio Settings & Lyrics", color = AppleTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Tune,
                                        contentDescription = null,
                                        tint = AppleTextSecondary
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenSettings()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Circular User Profile Avatar (matching the screenshot's user avatar)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0x66FFFFFF), CircleShape)
                            .clickable(onClick = onOpenSettings)
                            .testTag("user_avatar_button")
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.user_avatar_pic_1790846314178),
                            contentDescription = "User Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // YouTube Syncing Banner if active
        if (isSyncing) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Syncing",
                        tint = AppleRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Syncing trending tracks with Music Server...",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Offline Mode Banner if active
        if (isOfflineMode) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2E1018))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.OfflinePin,
                        contentDescription = "Offline Mode",
                        tint = AppleRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Offline Mode Active • Playing downloaded tracks only",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (allSongs.isEmpty()) {
            item {
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudQueue,
                            contentDescription = null,
                            tint = AppleRed,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Connected to Vercel Server",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Semua lagu diambil secara real-time langsung dari server Vercel Anda tanpa mock data. Buka tab Search untuk mencari dan memutar lagu!",
                            fontSize = 13.sp,
                            color = AppleTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // 1. Top Picks for You (Real Music Carousel)
        item {
            TopPicksCarousel(
                songs = allSongs,
                onPlaySong = onPlaySong,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 2. Recently Played > (Screenshot replica row)
        item {
            RecentlyPlayedRow(
                songs = if (recentlyPlayed.isNotEmpty()) recentlyPlayed else allSongs.take(4),
                onSongClick = onPlaySong,
                onSeeAllClick = { /* see all */ },
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // 3. Personalized Playlists ("Made for You")
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Text(
                    text = "Personalized Playlists",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        PersonalizedPlaylistCard(
                            playlist = playlist,
                            onClick = { onSelectPlaylist(playlist) }
                        )
                    }
                }
            }
        }

        // 4. Quick Hits / Popular Songs
        item {
            Text(
                text = "Trending Songs",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AppleTextPrimary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
            )
        }

        items(allSongs, key = { it.id }) { song ->
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

@Composable
private fun PersonalizedPlaylistCard(
    playlist: PlaylistEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick)
            .testTag("playlist_card_${playlist.id}")
    ) {
        val artRes = playlist.coverArtDrawableId ?: R.drawable.favorites_star_art_1790846296914
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppleDarkCard)
        ) {
            Image(
                painter = painterResource(id = artRes),
                contentDescription = playlist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = playlist.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppleTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = playlist.description,
            fontSize = 12.sp,
            color = AppleTextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 15.sp
        )
    }
}
