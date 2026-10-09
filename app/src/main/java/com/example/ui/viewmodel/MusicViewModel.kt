package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.model.LyricLine
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.data.api.LyricsService
import com.example.lyrics.LyricsParser
import com.example.player.AudioPlayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF, ALL, ONE
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MusicRepository(application, database)
    val playerManager = AudioPlayerManager(application)

    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedSongs: StateFlow<List<Song>> = repository.downloadedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = repository.recentlyPlayed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Player states - starting clean without mock data
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentPositionMs: StateFlow<Long> = playerManager.currentPositionMs
    val durationMs: StateFlow<Long> = playerManager.durationMs
    val errorMessage: StateFlow<String?> = playerManager.errorMessage

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    // Full screen & Lyrics states
    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _isLyricsVisible = MutableStateFlow(false)
    val isLyricsVisible: StateFlow<Boolean> = _isLyricsVisible.asStateFlow()

    private val _autoShowLyrics = MutableStateFlow(false)
    val autoShowLyrics: StateFlow<Boolean> = _autoShowLyrics.asStateFlow()

    // Offline mode toggle
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    // Loading & sync indicator
    private val _isSyncingServer = MutableStateFlow(false)
    val isSyncingServer: StateFlow<Boolean> = _isSyncingServer.asStateFlow()
    val isSyncingYouTube: StateFlow<Boolean> = _isSyncingServer // alias for backward-compat

    // Lyrics parsing
    private val _parsedLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val parsedLyrics: StateFlow<List<LyricLine>> = _parsedLyrics.asStateFlow()

    // Active lyric line index based on position
    val activeLyricIndex: StateFlow<Int> = combine(
        parsedLyrics,
        currentPositionMs
    ) { lyrics, posMs ->
        LyricsParser.findActiveLineIndex(lyrics, posMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Search query & results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _apiSearchResults = MutableStateFlow<List<Song>>(emptyList())
    private var searchJob: Job? = null

    val searchResults: StateFlow<List<Song>> = combine(
        _apiSearchResults,
        searchQuery
    ) { apiSongs, query ->
        if (query.isBlank()) emptyList() else apiSongs
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Playlist Detail (for browsing playlist tracks)
    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    private val _selectedPlaylistSongs = MutableStateFlow<List<Song>>(emptyList())
    val selectedPlaylistSongs: StateFlow<List<Song>> = _selectedPlaylistSongs.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeIfNeeded()
        }

        playerManager.onCompletionListener = {
            onTrackFinished()
        }

        playerManager.onAudioResolvedListener = { song, resolvedUrl ->
            viewModelScope.launch {
                repository.importLocalSong(song.copy(audioPathOrUrl = resolvedUrl))
            }
        }
    }

    fun refreshFromServer() {
        viewModelScope.launch {
            _isSyncingServer.value = true
            repository.syncWithMusicServer()
            _isSyncingServer.value = false
        }
    }

    fun refreshFromYouTube() {
        refreshFromServer()
    }

    fun playSong(song: Song, newQueue: List<Song>? = null) {
        if (newQueue != null && newQueue.isNotEmpty()) {
            _queue.value = newQueue
        } else if (!_queue.value.any { it.id == song.id }) {
            _queue.value = _queue.value + song
        }

        _currentSong.value = song
        _parsedLyrics.value = LyricsParser.parse(song.lyricsLrc)
        playerManager.playSong(song)

        viewModelScope.launch {
            repository.recordPlayed(song.id)
            val realLyrics = LyricsService.fetchRealLyrics(song.title, song.artist)
            if (!realLyrics.isNullOrBlank()) {
                _parsedLyrics.value = LyricsParser.parse(realLyrics)
                repository.importLocalSong(song.copy(lyricsLrc = realLyrics))
            }
        }

        if (_autoShowLyrics.value) {
            _isLyricsVisible.value = true
        }
    }

    fun togglePlayPause() {
        val current = _currentSong.value
        if (current == null) {
            val first = _queue.value.firstOrNull() ?: allSongs.value.firstOrNull()
            if (first != null) {
                playSong(first)
            }
            return
        }

        if (playerManager.isPlaying.value) {
            playerManager.togglePlayPause()
        } else {
            if (playerManager.currentPositionMs.value == 0L && currentPositionMs.value == 0L) {
                playerManager.playSong(current)
            } else {
                playerManager.togglePlayPause()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun skipNext() {
        val currentList = _queue.value
        if (currentList.isEmpty()) return

        val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
        val nextSong = if (_isShuffle.value) {
            currentList.random()
        } else {
            val nextIndex = (currentIndex + 1) % currentList.size
            currentList[nextIndex]
        }
        playSong(nextSong)
    }

    fun skipPrevious() {
        val currentList = _queue.value
        if (currentList.isEmpty()) return

        if (currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }

        val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
        val prevIndex = if (currentIndex <= 0) currentList.lastIndex else currentIndex - 1
        playSong(currentList[prevIndex])
    }

    private fun onTrackFinished() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                _currentSong.value?.let { playSong(it) }
            }
            RepeatMode.ALL -> {
                skipNext()
            }
            RepeatMode.OFF -> {
                val currentList = _queue.value
                val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
                if (currentIndex < currentList.lastIndex) {
                    skipNext()
                } else {
                    playerManager.stop()
                }
            }
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun setLyricsVisible(visible: Boolean) {
        _isLyricsVisible.value = visible
    }

    fun toggleLyricsVisible() {
        _isLyricsVisible.value = !_isLyricsVisible.value
    }

    fun setAutoShowLyrics(autoShow: Boolean) {
        _autoShowLyrics.value = autoShow
    }

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = _currentSong.value?.copy(isFavorite = !song.isFavorite)
            }
        }
    }

    fun toggleDownload(song: Song) {
        viewModelScope.launch {
            repository.toggleDownload(song.id, song.isDownloaded, song)
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = _currentSong.value?.copy(isDownloaded = !song.isDownloaded)
            }
        }
    }

    private val _searchBaseUrl = MutableStateFlow(repository.getSearchBaseUrl())
    val searchBaseUrl: StateFlow<String> = _searchBaseUrl.asStateFlow()

    private val _streamBaseUrl = MutableStateFlow(repository.getStreamBaseUrl())
    val streamBaseUrl: StateFlow<String> = _streamBaseUrl.asStateFlow()

    val serverBaseUrl: StateFlow<String> = _searchBaseUrl // alias

    fun updateSearchBaseUrl(url: String) {
        repository.setSearchBaseUrl(url)
        _searchBaseUrl.value = repository.getSearchBaseUrl()
    }

    fun updateStreamBaseUrl(url: String) {
        repository.setStreamBaseUrl(url)
        _streamBaseUrl.value = repository.getStreamBaseUrl()
    }

    fun updateServerBaseUrl(url: String) {
        updateSearchBaseUrl(url)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.trim().isNotEmpty()) {
            searchJob = viewModelScope.launch {
                _isSearching.value = true
                delay(250) // fast debounce
                val results = repository.searchSongsOnline(query.trim())
                _apiSearchResults.value = results
                _isSearching.value = false
            }
        } else {
            _apiSearchResults.value = emptyList()
            _isSearching.value = false
        }
    }

    fun searchImmediate(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.trim().isNotEmpty()) {
            searchJob = viewModelScope.launch {
                _isSearching.value = true
                val results = repository.searchSongsOnline(query.trim())
                _apiSearchResults.value = results
                _isSearching.value = false
            }
        } else {
            _apiSearchResults.value = emptyList()
            _isSearching.value = false
        }
    }

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
        if (playlist != null) {
            viewModelScope.launch {
                repository.getSongsForPlaylist(playlist.id).collect { songs ->
                    _selectedPlaylistSongs.value = songs
                }
            }
        } else {
            _selectedPlaylistSongs.value = emptyList()
        }
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun importLocalAudio(uriString: String, title: String, artist: String, durationMs: Long) {
        viewModelScope.launch {
            val song = Song(
                id = "local_${System.currentTimeMillis()}",
                title = title,
                artist = artist,
                album = "Local Audio",
                durationMs = durationMs,
                audioPathOrUrl = uriString,
                isDownloaded = true,
                lyricsLrc = "[00:00.00] Local file: $title\n[00:05.00] Imported from device storage"
            )
            repository.importLocalSong(song)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
