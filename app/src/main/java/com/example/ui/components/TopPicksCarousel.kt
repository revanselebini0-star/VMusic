import com.example.R

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@Composable
fun TopPicksCarousel(
    songs: List<Song>,
    onPlaySong: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    if (songs.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Top Picks for You",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppleTextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // First item: Apple Music "New Music - Made for You" hero card featuring real artist names
            item {
                val featuredArtists = songs.take(6).map { it.artist }.distinct().joinToString(", ")
                val firstSong = songs.first()
                GradientNewMusicCard(
                    artistListText = if (featuredArtists.isNotBlank()) featuredArtists else "Taylor Swift, Bruno Mars, Ariana Grande, The Weeknd",
                    onClick = { onPlaySong(firstSong) }
                )
            }

            // Subsequent items: Real YouTube trending music cards
            itemsIndexed(songs, key = { _, s -> "top_pick_${s.id}" }) { index, song ->
                RealSongFeaturedCard(
                    song = song,
                    badgeLabel = when (index) {
                        0 -> "TOP HIT"
                        1 -> "NEW RELEASE"
                        2 -> "TRENDING NOW"
                        3 -> "VIRAL CHART"
                        else -> "POPULAR MUSIC"
                    },
                    onClick = { onPlaySong(song) }
                )
            }
        }
    }
}

@Composable
private fun GradientNewMusicCard(
    artistListText: String,
    onClick: () -> Unit
) {
    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFF2A54),
            Color(0xFFFF4B72),
            Color(0xFFFF7E98),
            Color(0xFFFF9BAE)
        )
    )

    Box(
        modifier = Modifier
            .width(260.dp)
            .height(340.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardGradient)
            .clickable(onClick = onClick)
            .testTag("top_pick_gradient_card")
            .padding(18.dp)
    ) {
        // Top Apple Music badge
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Music",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Center Bold Title: "New \n Music"
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "New",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Music",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        // Bottom text: "Made for You \n [Real artists from YouTube]"
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = "Made for You",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = artistListText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun RealSongFeaturedCard(
    song: Song,
    badgeLabel: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(340.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("top_pick_real_card_${song.id}")
    ) {
        AppAlbumArt(
            albumArtUrl = song.albumArtUrl,
            albumArtDrawableId = song.albumArtDrawableId,
            contentDescription = song.title,
            modifier = Modifier.fillMaxSize()
        )

        // Vignette gradient overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.95f)
                        ),
                        startY = 120f
                    )
                )
        )

        // Bottom labels directly from the real YouTube track
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = badgeLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF2D55),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppleTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = AppleTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
