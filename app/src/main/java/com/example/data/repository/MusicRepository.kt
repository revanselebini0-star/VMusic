package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.api.VercelMusicService
import com.example.data.local.AppDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistSongCrossRef
import com.example.data.local.SongEntity
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MusicRepository(
    private val context: Context,
    private val database: AppDatabase
) {

    private val TAG = "MusicRepository"
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()

    val allSongs: Flow<List<Song>> = songDao.getAllSongs().map { entities ->
        entities.map { it.toDomain() }
    }

    val downloadedSongs: Flow<List<Song>> = songDao.getDownloadedSongs().map { entities ->
        entities.map { it.toDomain() }
    }

    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs().map { entities ->
        entities.map { it.toDomain() }
    }

    val recentlyPlayed: Flow<List<Song>> = songDao.getRecentlyPlayed().map { entities ->
        entities.map { it.toDomain() }
    }

    val playlists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val allPlaylists: Flow<List<PlaylistEntity>> = playlists

    fun getSongsForPlaylist(playlistId: String): Flow<List<Song>> {
        return playlistDao.getSongsForPlaylist(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        // Sumber data 100% dari VercelMusicService.kt (tanpa SampleData / mock data)
        syncWithMusicServer()
    }

    suspend fun syncWithMusicServer(): Boolean = withContext(Dispatchers.IO) {
        try {
            val searchEndpoint = getSearchBaseUrl()
            val serverSongs = VercelMusicService.searchSongs("pop", searchEndpoint)
            if (serverSongs.isNotEmpty()) {
                val entities = serverSongs.map { SongEntity.fromDomain(it) }
                songDao.insertSongs(entities)
                Log.d(TAG, "Successfully synced ${serverSongs.size} real tracks from VercelMusicService")
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing with Vercel server: ${e.message}", e)
        }
        return@withContext false
    }

    fun getSearchBaseUrl(): String {
        return VercelMusicService.getSearchBaseUrl(context)
    }

    fun setSearchBaseUrl(url: String) {
        VercelMusicService.setSearchBaseUrl(context, url)
    }

    fun getStreamBaseUrl(): String {
        return VercelMusicService.getStreamBaseUrl(context)
    }

    fun setStreamBaseUrl(url: String) {
        VercelMusicService.setStreamBaseUrl(context, url)
    }

    /**
     * 1. Kolom Search: WAJIB mengambil data real-time 100% dari VercelMusicService.searchSongs()
     * Tampilkan judul, artis, album, dan coverArt dari hasil JSON Vercel tersebut ke UI list.
     */
    suspend fun searchSongsOnline(query: String): List<Song> = withContext(Dispatchers.IO) {
        val searchEndpoint = getSearchBaseUrl()
        Log.d(TAG, "Searching Vercel server for '$query' via $searchEndpoint")

        val vercelResults = VercelMusicService.searchSongs(query, searchEndpoint)
        if (vercelResults.isNotEmpty()) {
            val entities = vercelResults.map { SongEntity.fromDomain(it) }
            songDao.insertSongs(entities)
            Log.d(TAG, "Received ${vercelResults.size} real songs from VercelMusicService")
        }
        return@withContext vercelResults
    }

    suspend fun toggleFavorite(songId: String, currentFavorite: Boolean) {
        songDao.updateFavorite(songId, !currentFavorite)
    }

    suspend fun toggleDownload(songId: String, currentDownloaded: Boolean, song: Song? = null) {
        songDao.updateDownloaded(songId, !currentDownloaded)
    }

    suspend fun recordPlayed(songId: String) {
        songDao.recordPlayed(songId, System.currentTimeMillis())
    }

    suspend fun createPlaylist(name: String, description: String): String {
        val id = "playlist_${System.currentTimeMillis()}"
        val entity = PlaylistEntity(
            id = id,
            name = name,
            description = description,
            coverArtDrawableId = null,
            isPersonalized = false
        )
        playlistDao.insertPlaylist(entity)
        return id
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: String) {
        playlistDao.insertPlaylistSong(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                orderIndex = System.currentTimeMillis().toInt()
            )
        )
    }

    suspend fun importLocalSong(song: Song) {
        songDao.insertSong(SongEntity.fromDomain(song))
    }
}
