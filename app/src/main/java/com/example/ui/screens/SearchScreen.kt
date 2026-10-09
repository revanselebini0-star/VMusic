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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.AppleBlack
import com.example.ui.theme.AppleDarkElevated
import com.example.ui.theme.AppleRed
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

data class CategoryCard(
    val title: String,
    val color1: Color,
    val color2: Color
)

@Composable
fun SearchScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchImmediate: (String) -> Unit,
    searchResults: List<Song>,
    isSearching: Boolean,
    currentSong: Song?,
    isPlaying: Boolean,
    onPlaySong: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onToggleDownload: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val categories = listOf(
        CategoryCard("Pop Hits", Color(0xFFFF2D55), Color(0xFFFF6987)),
        CategoryCard("R&B & Soul", Color(0xFF5856D6), Color(0xFF8987E8)),
        CategoryCard("Funk / Groove", Color(0xFFFF9500), Color(0xFFFFB74D)),
        CategoryCard("K-Pop", Color(0xFFAF52DE), Color(0xFFD291F0)),
        CategoryCard("Chill & Lo-Fi", Color(0xFF34C759), Color(0xFF6ED888)),
        CategoryCard("Acoustic", Color(0xFFFF3B30), Color(0xFFFF7269)),
        CategoryCard("Hip-Hop", Color(0xFF007AFF), Color(0xFF5AC8FA)),
        CategoryCard("Dance / Club", Color(0xFFFFCC00), Color(0xFFFFE066))
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppleBlack)
            .testTag("search_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Text(
                text = "Search",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = AppleTextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // Apple Music search bar with keyboard action support
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Artists, Songs, Lyrics and More",
                            color = AppleTextSecondary,
                            fontSize = 15.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = AppleTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    color = AppleRed,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(end = 4.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear",
                                        tint = AppleTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                        onSearchImmediate(searchQuery)
                    }),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppleDarkElevated,
                        unfocusedContainerColor = AppleDarkElevated,
                        disabledContainerColor = AppleDarkElevated,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = AppleTextPrimary,
                        unfocusedTextColor = AppleTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("search_text_field")
                )
            }
        }

        if (searchQuery.isBlank()) {
            // Browse Categories
            item {
                Text(
                    text = "Browse Categories",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { cat ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(96.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(cat.color1, cat.color2)
                                            )
                                        )
                                        .clickable {
                                            focusManager.clearFocus()
                                            onSearchImmediate(cat.title)
                                        }
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = cat.title,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else {
            // Search Results Header & Status
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSearching) "Searching Music Server..." else "Top Results (${searchResults.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary
                    )

                    if (!isSearching && searchResults.isEmpty()) {
                        Text(
                            text = "Search Online",
                            color = AppleRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onSearchImmediate(searchQuery) }
                        )
                    }
                }
            }

            if (searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, bottom = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSearching) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = AppleRed, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Searching server for \"$searchQuery\"...",
                                    color = AppleTextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onSearchImmediate(searchQuery) }
                            ) {
                                Text(
                                    text = "No matches found for \"$searchQuery\"",
                                    color = AppleTextSecondary,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to search online server now ➔",
                                    color = AppleRed,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                items(searchResults, key = { it.id }) { song ->
                    // Check if matched by lyrics
                    val lyricMatch = if (song.lyricsLrc.contains(searchQuery, ignoreCase = true)) {
                        song.lyricsLrc.lineSequence()
                            .firstOrNull { it.contains(searchQuery, ignoreCase = true) }
                            ?.replace(Regex("""\[\d{2}:\d{2}(?:\.\d{2,3})?]"""), "")
                            ?.trim()
                    } else null

                    Column {
                        SongListItem(
                            song = song,
                            isCurrentSong = song.id == currentSong?.id,
                            isPlaying = isPlaying && song.id == currentSong?.id,
                            onClick = { onPlaySong(song) },
                            onToggleFavorite = { onToggleFavorite(song) },
                            onToggleDownload = { onToggleDownload(song) },
                            onAddToPlaylist = { }
                        )

                        if (lyricMatch != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 76.dp, end = 16.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lyrics: \"$lyricMatch\"",
                                    color = AppleRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
