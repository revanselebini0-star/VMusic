package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlaylistAdd
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
import com.example.data.model.Song
import com.example.ui.theme.AppleDarkElevated
import com.example.ui.theme.AppleExplicitBadge
import com.example.ui.theme.AppleExplicitText
import com.example.ui.theme.AppleRed
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@Composable
fun SongListItem(
    song: Song,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleDownload: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppAlbumArt(
            albumArtUrl = song.albumArtUrl,
            albumArtDrawableId = song.albumArtDrawableId,
            contentDescription = song.title,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = song.title,
                    color = if (isCurrentSong) AppleRed else AppleTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (song.isExplicit) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(AppleExplicitBadge)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "E",
                            color = AppleExplicitText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isDownloaded) {
                    Icon(
                        imageVector = Icons.Filled.CloudDone,
                        contentDescription = "Downloaded",
                        tint = AppleRed,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = "${song.artist} • ${song.album}",
                    color = AppleTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // More options dropdown
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = "Options",
                    tint = AppleTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(AppleDarkElevated)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (song.isFavorite) "Remove from Favorites" else "Favorite Song",
                            color = AppleTextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = if (song.isFavorite) AppleRed else AppleTextSecondary
                        )
                    },
                    onClick = {
                        showMenu = false
                        onToggleFavorite()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (song.isDownloaded) "Remove Download" else "Download Offline",
                            color = AppleTextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (song.isDownloaded) Icons.Filled.CloudDone else Icons.Filled.CloudDownload,
                            contentDescription = null,
                            tint = if (song.isDownloaded) AppleRed else AppleTextSecondary
                        )
                    },
                    onClick = {
                        showMenu = false
                        onToggleDownload()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Add to Playlist", color = AppleTextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.PlaylistAdd,
                            contentDescription = null,
                            tint = AppleTextSecondary
                        )
                    },
                    onClick = {
                        showMenu = false
                        onAddToPlaylist()
                    }
                )
            }
        }
    }
}
