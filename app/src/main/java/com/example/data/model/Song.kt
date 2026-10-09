package com.example.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val isExplicit: Boolean = false,
    val albumArtDrawableId: Int? = null,
    val albumArtUrl: String? = null,
    val audioPathOrUrl: String = "",
    val isDownloaded: Boolean = false,
    val isFavorite: Boolean = false,
    val lyricsLrc: String = "",
    val genre: String = "Pop",
    val playCount: Int = 0
)

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val coverArtDrawableId: Int? = null,
    val isPersonalized: Boolean = false,
    val songs: List<Song> = emptyList()
)
