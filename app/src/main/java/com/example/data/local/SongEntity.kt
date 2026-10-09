package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val isExplicit: Boolean,
    val albumArtDrawableId: Int?,
    val albumArtUrl: String?,
    val audioPathOrUrl: String,
    val isDownloaded: Boolean,
    val isFavorite: Boolean,
    val lyricsLrc: String,
    val genre: String,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L
) {
    fun toDomain(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            isExplicit = isExplicit,
            albumArtDrawableId = albumArtDrawableId,
            albumArtUrl = albumArtUrl,
            audioPathOrUrl = audioPathOrUrl,
            isDownloaded = isDownloaded,
            isFavorite = isFavorite,
            lyricsLrc = lyricsLrc,
            genre = genre,
            playCount = playCount
        )
    }

    companion object {
        fun fromDomain(song: Song, lastPlayed: Long = 0L): SongEntity {
            return SongEntity(
                id = song.id,
                title = song.title,
                artist = song.artist,
                album = song.album,
                durationMs = song.durationMs,
                isExplicit = song.isExplicit,
                albumArtDrawableId = song.albumArtDrawableId,
                albumArtUrl = song.albumArtUrl,
                audioPathOrUrl = song.audioPathOrUrl,
                isDownloaded = song.isDownloaded,
                isFavorite = song.isFavorite,
                lyricsLrc = song.lyricsLrc,
                genre = song.genre,
                playCount = song.playCount,
                lastPlayedTimestamp = lastPlayed
            )
        }
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val coverArtDrawableId: Int?,
    val isPersonalized: Boolean
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongCrossRef(
    val playlistId: String,
    val songId: String,
    val orderIndex: Int
)
