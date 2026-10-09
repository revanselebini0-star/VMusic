package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.components.AppAlbumArt
import com.example.ui.components.SongListItem
import com.example.ui.theme.AppleBlack
import com.example.ui.theme.AppleRed
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@Composable
fun NewScreen(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onPlaySong: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onToggleDownload: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppleBlack)
            .testTag("new_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Text(
                text = "New",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = AppleTextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // Real Featured Albums & Singles banner row from loaded YouTube songs
        if (songs.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(songs.take(6), key = { _, s -> "new_banner_${s.id}" }) { idx, song ->
                        FeaturedRealSongBanner(
                            song = song,
                            tag = when (idx) {
                                0 -> "LATEST RELEASE"
                                1 -> "FEATURED ALBUM"
                                2 -> "GLOBAL HIT"
                                3 -> "EDITOR'S PICK"
                                else -> "TRENDING TRACK"
                            },
                            onClick = { onPlaySong(song) }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Latest Releases",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AppleTextPrimary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 8.dp)
            )
        }

        items(songs, key = { "new_list_${it.id}" }) { song ->
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
private fun FeaturedRealSongBanner(
    song: Song,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        AppAlbumArt(
            albumArtUrl = song.albumArtUrl,
            albumArtDrawableId = song.albumArtDrawableId,
            contentDescription = song.title,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.95f)
                        ),
                        startY = 60f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = tag,
                color = AppleRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.title,
                color = AppleTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                color = AppleTextSecondary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
