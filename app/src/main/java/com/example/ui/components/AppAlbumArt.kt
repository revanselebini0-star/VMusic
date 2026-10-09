package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.AppleDarkCard

@Composable
fun AppAlbumArt(
    albumArtUrl: String?,
    albumArtDrawableId: Int?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(
        modifier = modifier.background(AppleDarkCard),
        contentAlignment = Alignment.Center
    ) {
        if (!albumArtUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(albumArtUrl)
                    .crossfade(true)
                    .placeholder(albumArtDrawableId ?: R.drawable.motive_album_art_1790846205472)
                    .error(albumArtDrawableId ?: R.drawable.motive_album_art_1790846205472)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else if (albumArtDrawableId != null) {
            Image(
                painter = painterResource(id = albumArtDrawableId),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxSize(0.5f)
            )
        }
    }
}
