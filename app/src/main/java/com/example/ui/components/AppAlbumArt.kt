package com.example.ui.components

import com.example.player.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AppAlbumArt(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    albumArtUrl: String? = imageUrl,
    albumArtDrawableId: Int? = null,
    contentDescription: String? = null
) {
    val finalUrl = albumArtUrl ?: imageUrl

    when {
        !finalUrl.isNullOrEmpty() -> {
            AsyncImage(
                model = finalUrl,
                contentDescription = contentDescription,
                modifier = modifier.clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_launcher_background),
                placeholder = painterResource(id = R.drawable.ic_launcher_background)
            )
        }
        albumArtDrawableId != null -> {
            Image(
                painter = painterResource(id = albumArtDrawableId),
                contentDescription = contentDescription,
                modifier = modifier.clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = contentDescription,
                modifier = modifier.clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
