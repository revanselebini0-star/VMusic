package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LyricLine
import com.example.data.model.Song
import com.example.ui.theme.AppleRed
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import java.util.Locale

@Composable
fun LyricsView(
    song: Song,
    lyrics: List<LyricLine>,
    activeLineIndex: Int,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    autoShowLyrics: Boolean,
    onAutoShowLyricsChange: (Boolean) -> Unit,
    onSeekTo: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Smooth auto-scroll to keep active line centered
    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex in lyrics.indices) {
            val targetIndex = (activeLineIndex - 1).coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex)
        }
    }

    // Dynamic Apple Music ambient backdrop
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2E0814),
            Color(0xFF19060B),
            Color(0xFF090305),
            Color(0xFF000000)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("lyrics_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("lyrics_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Lyrics",
                        tint = AppleTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        color = AppleTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = song.artist,
                        color = AppleTextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }

                // Auto-sync lyrics toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Auto",
                        color = if (autoShowLyrics) AppleRed else AppleTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = autoShowLyrics,
                        onCheckedChange = onAutoShowLyricsChange,
                        modifier = Modifier
                            .scale(0.7f)
                            .testTag("auto_lyrics_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppleRed,
                            uncheckedTrackColor = Color(0xFF333333)
                        )
                    )
                }
            }

            // Synchronized Lyrics Body
            if (lyrics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No synchronized lyrics available for this song",
                        color = AppleTextSecondary,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("lyrics_list"),
                    contentPadding = PaddingValues(vertical = 60.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    itemsIndexed(lyrics) { index, line ->
                        val isActive = index == activeLineIndex
                        val isPassed = index < activeLineIndex

                        val alpha by animateFloatAsState(
                            targetValue = if (isActive) 1f else if (isPassed) 0.5f else 0.35f,
                            animationSpec = tween(durationMillis = 250),
                            label = "lyric_alpha"
                        )

                        val fontSize = if (isActive) 28.sp else 22.sp
                        val fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSeekTo(line.timeMs) }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                                .testTag("lyric_line_$index")
                        ) {
                            Text(
                                text = line.text,
                                color = AppleTextPrimary,
                                fontSize = fontSize,
                                fontWeight = fontWeight,
                                lineHeight = if (isActive) 34.sp else 28.sp,
                                modifier = Modifier.alpha(alpha)
                            )
                        }
                    }
                }
            }

            // Bottom Player Mini-Controls inside Lyrics Mode
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // Scrub slider
                val progress = if (durationMs > 0) {
                    (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Slider(
                    value = progress,
                    onValueChange = { frac ->
                        onSeekTo((frac * durationMs).toLong())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPositionMs),
                        fontSize = 11.sp,
                        color = AppleTextSecondary
                    )
                    val remainingMs = (durationMs - currentPositionMs).coerceAtLeast(0L)
                    Text(
                        text = "-${formatTime(remainingMs)}",
                        fontSize = 11.sp,
                        color = AppleTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Transport Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FastRewind,
                            contentDescription = "Previous Track",
                            tint = AppleTextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(onClick = onTogglePlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FastForward,
                            contentDescription = "Next Track",
                            tint = AppleTextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
