import com.example.ui.components.MiniPlayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.LyricsView
import com.example.ui.components.MiniPlayer
import com.example.ui.components.NowPlayingSheet
import com.example.ui.components.PlaylistDetailSheet
import com.example.ui.navigation.AppleMusicBottomBar
import com.example.ui.navigation.NavTab
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NewScreen
import com.example.ui.screens.RadioScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.theme.AppleBlack
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VanzMusicApp()
            }
        }
    }
}

@Composable
fun VanzMusicApp(viewModel: MusicViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    var showSettings by remember { mutableStateOf(false) }

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()

    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsStateWithLifecycle()
    val isLyricsVisible by viewModel.isLyricsVisible.collectAsStateWithLifecycle()
    val autoShowLyrics by viewModel.autoShowLyrics.collectAsStateWithLifecycle()
    val activeLyricIndex by viewModel.activeLyricIndex.collectAsStateWithLifecycle()
    val parsedLyrics by viewModel.parsedLyrics.collectAsStateWithLifecycle()

    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val isSyncingYouTube by viewModel.isSyncingYouTube.collectAsStateWithLifecycle()

    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
    val downloadedSongs by viewModel.downloadedSongs.collectAsStateWithLifecycle()
    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val selectedPlaylistSongs by viewModel.selectedPlaylistSongs.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    // Back handling for overlays
    BackHandler(enabled = isLyricsVisible) {
        viewModel.setLyricsVisible(false)
    }

    BackHandler(enabled = !isLyricsVisible && isNowPlayingExpanded) {
        viewModel.setNowPlayingExpanded(false)
    }

    BackHandler(enabled = !isLyricsVisible && !isNowPlayingExpanded && selectedPlaylist != null) {
        viewModel.selectPlaylist(null)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBlack)
    ) {
        // Main Screen content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    NavTab.HOME -> {
                        HomeScreen(
                            allSongs = allSongs,
                            recentlyPlayed = recentlyPlayed,
                            playlists = playlists,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            isOfflineMode = isOfflineMode,
                            onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                            onPlaySong = { viewModel.playSong(it, allSongs) },
                            onPlaySongById = { songId ->
                                allSongs.find { it.id == songId }?.let {
                                    viewModel.playSong(it, allSongs)
                                }
                            },
                            onSelectPlaylist = { viewModel.selectPlaylist(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onToggleDownload = { viewModel.toggleDownload(it) },
                            onOpenSettings = { showSettings = true },
                            isSyncing = isSyncingYouTube,
                            onRefreshYouTube = { viewModel.refreshFromYouTube() }
                        )
                    }
                    NavTab.NEW -> {
                        NewScreen(
                            songs = allSongs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onPlaySong = { viewModel.playSong(it, allSongs) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onToggleDownload = { viewModel.toggleDownload(it) }
                        )
                    }
                    NavTab.RADIO -> {
                        RadioScreen(
                            songs = allSongs,
                            onPlaySong = { viewModel.playSong(it, allSongs) }
                        )
                    }
                    NavTab.LIBRARY -> {
                        LibraryScreen(
                            allSongs = allSongs,
                            downloadedSongs = downloadedSongs,
                            favoriteSongs = favoriteSongs,
                            playlists = playlists,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            isOfflineMode = isOfflineMode,
                            onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                            onPlaySong = { viewModel.playSong(it) },
                            onSelectPlaylist = { viewModel.selectPlaylist(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onToggleDownload = { viewModel.toggleDownload(it) },
                            onImportLocalAudio = { uri ->
                                viewModel.importLocalAudio(
                                    uriString = uri.toString(),
                                    title = "Imported Track",
                                    artist = "Local Artist",
                                    durationMs = 180000L
                                )
                            }
                        )
                    }
                    NavTab.SEARCH -> {
                        SearchScreen(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onSearchImmediate = { viewModel.searchImmediate(it) },
                            searchResults = searchResults,
                            isSearching = isSearching,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onPlaySong = { viewModel.playSong(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onToggleDownload = { viewModel.toggleDownload(it) }
                        )
                    }
                }
            }

            // Docked Mini Player
            MiniPlayer(
                song = currentSong,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSkipNext = { viewModel.skipNext() },
                onClick = { viewModel.setNowPlayingExpanded(true) }
            )

            // Apple Music 5-Tab Navigation Bar
            AppleMusicBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }

        // Full Screen Playlist Details Overlay
        selectedPlaylist?.let { playlist ->
            PlaylistDetailSheet(
                playlist = playlist,
                songs = selectedPlaylistSongs,
                currentSong = currentSong,
                isPlaying = isPlaying,
                onBack = { viewModel.selectPlaylist(null) },
                onPlayAll = {
                    if (selectedPlaylistSongs.isNotEmpty()) {
                        viewModel.playSong(selectedPlaylistSongs.first(), selectedPlaylistSongs)
                    }
                },
                onShuffleAll = {
                    if (selectedPlaylistSongs.isNotEmpty()) {
                        viewModel.playSong(selectedPlaylistSongs.random(), selectedPlaylistSongs)
                    }
                },
                onSongClick = { viewModel.playSong(it, selectedPlaylistSongs) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onToggleDownload = { viewModel.toggleDownload(it) }
            )
        }

        // Full Screen Now Playing Sheet Overlay
        AnimatedVisibility(
            visible = isNowPlayingExpanded && !isLyricsVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NowPlayingSheet(
                song = currentSong,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isShuffle = isShuffle,
                repeatMode = repeatMode,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeekTo = { viewModel.seekTo(it) },
                onSkipNext = { viewModel.skipNext() },
                onSkipPrevious = { viewModel.skipPrevious() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onToggleDownload = { viewModel.toggleDownload(it) },
                onOpenLyrics = { viewModel.setLyricsVisible(true) },
                onCollapse = { viewModel.setNowPlayingExpanded(false) }
            )
        }

        // Apple Music Synchronized Lyrics Full Screen Overlay
        AnimatedVisibility(
            visible = isLyricsVisible && currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            currentSong?.let { song ->
                LyricsView(
                    song = song,
                    lyrics = parsedLyrics,
                    activeLineIndex = activeLyricIndex,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    autoShowLyrics = autoShowLyrics,
                    onAutoShowLyricsChange = { viewModel.setAutoShowLyrics(it) },
                    onSeekTo = { viewModel.seekTo(it) },
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSkipNext = { viewModel.skipNext() },
                    onSkipPrevious = { viewModel.skipPrevious() },
                    onClose = { viewModel.setLyricsVisible(false) }
                )
            }
        }

        val searchBaseUrl by viewModel.searchBaseUrl.collectAsStateWithLifecycle()
        val streamBaseUrl by viewModel.streamBaseUrl.collectAsStateWithLifecycle()
        val playerError by viewModel.errorMessage.collectAsStateWithLifecycle()

        playerError?.let { err ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF3B1219))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = err,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Settings Dialog
        if (showSettings) {
            SettingsDialog(
                isOfflineMode = isOfflineMode,
                onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                autoShowLyrics = autoShowLyrics,
                onToggleAutoShowLyrics = { viewModel.setAutoShowLyrics(it) },
                searchBaseUrl = searchBaseUrl,
                onUpdateSearchBaseUrl = { viewModel.updateSearchBaseUrl(it) },
                streamBaseUrl = streamBaseUrl,
                onUpdateStreamBaseUrl = { viewModel.updateStreamBaseUrl(it) },
                onDismiss = { showSettings = false }
            )
        }
    }
}
