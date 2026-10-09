package com.example.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AudioPlayerManager(private val context: Context) {

    private val TAG = "AudioPlayerManager"
    private var exoPlayer: ExoPlayer? = null

    private val scope = CoroutineScope(Dispatchers.Main)
    private var positionJob: Job? = null
    private var currentSong: Song? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var onCompletionListener: (() -> Unit)? = null
    var onAudioResolvedListener: ((Song, String) -> Unit)? = null

    // PANGKALAN SERVER VERCEL ASLI KAMU YANG SUDAH FIX
    private val BASE_URL = "https://vercel.app"

    init {
        initializeExoPlayer()
    }

    @OptIn(UnstableApi::class)
    private fun initializeExoPlayer() {
        if (exoPlayer != null) return

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

            val renderersFactory = DefaultRenderersFactory(context).apply {
                setEnableDecoderFallback(true)
                setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            }

            exoPlayer = ExoPlayer.Builder(context, renderersFactory)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .build().apply {
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            when (playbackState) {
                                Player.STATE_READY -> {
                                    val d = if (duration > 0) duration else currentSong?.durationMs ?: 0L
                                    _durationMs.value = d
                                    _isPlaying.value = isPlaying
                                    startPositionTracker()
                                }
                                Player.STATE_ENDED -> {
                                    _isPlaying.value = false
                                    _currentPositionMs.value = 0L
                                    stopPositionTracker()
                                    onCompletionListener?.invoke()
                                }
                                Player.STATE_BUFFERING -> {
                                    Log.d(TAG, "Sedang memuat stream audio asli...")
                                }
                                else -> {}
                            }
                        }

                        override fun onIsPlayingChanged(playing: Boolean) {
                            _isPlaying.value = playing
                            if (playing) startPositionTracker() else stopPositionTracker()
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            Log.e(TAG, "ExoPlayer error: ${error.message}", error)
                            _isPlaying.value = false
                            stopPositionTracker()
                            _errorMessage.value = "Gagal memutar audio: ${error.message}"
                        }
                    })
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed initializing ExoPlayer: ${e.message}")
        }
    }
    fun playSong(song: Song) {
        currentSong = song
        _currentPositionMs.value = 0L
        _isPlaying.value = true
        _errorMessage.value = null

        scope.launch(Dispatchers.IO) {
            try {
                val urlTarget = "$BASE_URL/api/stream?id=${song.id}"
                Log.d(TAG, "Menembak HTTP murni ke: $urlTarget")

                val koneksi = URL(urlTarget).openConnection() as HttpURLConnection
                koneksi.requestMethod = "GET"
                koneksi.connectTimeout = 10000
                koneksi.readTimeout = 10000

                val kodeRespon = koneksi.responseCode
                if (kodeRespon == HttpURLConnection.HTTP_OK) {
                    val pembaca = BufferedReader(InputStreamReader(koneksi.inputStream))
                    val hasilTeks = StringBuilder()
                    var barisData: String?
                    
                    while (pembaca.readLine().also { barisData = it } != null) {
                        hasilTeks.append(barisData)
                    }
                    pembaca.close()

                    val responMentah = hasilTeks.toString()

                    withContext(Dispatchers.Main) {
                        if (!responMentah.isNullOrBlank() && responMentah.contains("http")) {
                            var linkAudioAsli = responMentah
                                .substringAfter("\"urlAudioMurni\":\"")
                                .substringBefore("\"")
                            
                            linkAudioAsli = linkAudioAsli.replace("\\/", "/")

                            Log.d(TAG, "Link stream lagu asli didapatkan: $linkAudioAsli")
                            onAudioResolvedListener?.invoke(song, linkAudioAsli)
                            playPureAudio(linkAudioAsli, song.durationMs)
                        } else {
                            stop()
                            _isPlaying.value = false
                            _errorMessage.value = "Gagal mengambil stream lagu asli."
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        stop()
                        _isPlaying.value = false
                        _errorMessage.value = "Gagal menghubungi server stream."
                    }
                }
                koneksi.disconnect()

            } catch (e: Exception) {
                Log.e(TAG, "Koneksi terputus: ${e.message}")
                withContext(Dispatchers.Main) {
                    stop()
                    _isPlaying.value = false
                    _errorMessage.value = "Gagal menghubungi server stream."
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun playPureAudio(urlAudioMurni: String, defaultDuration: Long) {
        initializeExoPlayer()
        val player = exoPlayer ?: return
        try {
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .setAllowCrossProtocolRedirects(true)

            val extractorsFactory = DefaultExtractorsFactory().apply {
                setConstantBitrateSeekingEnabled(true)
            }

            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
                .createMediaSource(MediaItem.fromUri(urlAudioMurni))

            player.stop()
            player.clearMediaItems()
            player.setMediaSource(mediaSource)
            player.prepare()
            player.play()
            _isPlaying.value = true
            _durationMs.value = if (player.duration > 0) player.duration else defaultDuration
            startPositionTracker()
        } catch (e: Exception) {
            _isPlaying.value = false
            _errorMessage.value = "Error pemutaran: ${e.message}"
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer
        if (player == null) {
            currentSong?.let { playSong(it) }
            return
        }
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopPositionTracker()
        } else {
            player.play()
            _isPlaying.value = true
            startPositionTracker()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun stop() {
        stopPositionTracker()
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        _isPlaying.value = false
    }

    private fun startPositionTracker() {
        stopPositionTracker()
        positionJob = scope.launch {
            while (isActive) {
                if (exoPlayer?.isPlaying == true) {
                    _currentPositionMs.value = exoPlayer?.currentPosition ?: 0L
                    val totalDuration = exoPlayer?.duration ?: 0L
                    if (totalDuration > 0) {
                        _durationMs.value = totalDuration
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopPositionTracker() {
        positionJob?.cancel()
        positionJob = null
    }

    fun release() {
        stopPositionTracker()
        exoPlayer?.release()
        exoPlayer = null
    }
}
