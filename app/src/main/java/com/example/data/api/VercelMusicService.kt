package com.example.data.api

import android.content.Context
import android.util.Log
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object VercelMusicService {

    private const val TAG = "VercelMusicService"
    private const val PREFS_NAME = "vanz_vercel_server_v2_prefs"
    private const val KEY_SEARCH_BASE_URL = "search_base_url"
    private const val KEY_STREAM_BASE_URL = "stream_base_url"

    // Default endpoints pointing directly to the user's active Vercel server
    const val DEFAULT_SEARCH_URL = "https://server-kappa-black-72.vercel.app/api/search"
    const val DEFAULT_STREAM_URL = "https://server-kappa-black-72.vercel.app/api/stream"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    fun getSearchBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SEARCH_BASE_URL, DEFAULT_SEARCH_URL) ?: DEFAULT_SEARCH_URL
    }

    fun setSearchBaseUrl(context: Context, newUrl: String) {
        val cleanUrl = newUrl.trim()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(
            KEY_SEARCH_BASE_URL,
            if (cleanUrl.isNotBlank()) cleanUrl else DEFAULT_SEARCH_URL
        ).apply()
        Log.d(TAG, "Search base URL updated to: $cleanUrl")
    }

    fun getStreamBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_STREAM_BASE_URL, DEFAULT_STREAM_URL) ?: DEFAULT_STREAM_URL
    }

    fun setStreamBaseUrl(context: Context, newUrl: String) {
        val cleanUrl = newUrl.trim().trimEnd('/')
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(
            KEY_STREAM_BASE_URL,
            if (cleanUrl.isNotBlank()) cleanUrl else DEFAULT_STREAM_URL
        ).apply()
        Log.d(TAG, "Stream base URL updated to: $cleanUrl")
    }

    /**
     * 1. Kolom Search:
     * WAJIB mengambil data real-time dengan menembak via HTTP GET murni ke server Vercel:
     * https://server-kappa-black-72.vercel.app/api/search?q={searchQuery} (serta mendukung format https://vercel.app{searchQuery})
     * Memetakan: id, judul, artis, album, dan coverArt ke daftar UI.
     */
    suspend fun searchSongs(query: String, searchApiEndpoint: String = DEFAULT_SEARCH_URL): List<Song> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
        val cleanEndpoint = searchApiEndpoint.trim().trimEnd('/')

        val candidateUrls = listOf(
            "https://server-kappa-black-72.vercel.app/api/search?q=$encodedQuery",
            if (cleanEndpoint.contains("?")) "$cleanEndpoint&q=$encodedQuery" else "$cleanEndpoint?q=$encodedQuery",
            "$cleanEndpoint/$encodedQuery",
            "https://vercel.app/$encodedQuery"
        ).distinct()

        for (url in candidateUrls) {
            try {
                Log.d(TAG, "HTTP GET Search to Vercel: $url")
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .header("Accept", "application/json")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()?.trim()
                        if (!body.isNullOrBlank()) {
                            val songs = parseSongsJson(body)
                            if (songs.isNotEmpty()) {
                                Log.d(TAG, "Successfully loaded ${songs.size} real songs from Vercel: $url")
                                return@withContext songs
                            }
                        }
                    } else {
                        Log.d(TAG, "Search HTTP ${response.code}: $url")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Search request error on $url: ${e.message}")
            }
        }

        return@withContext emptyList()
    }

    /**
     * 2. Tombol Play/Klik Lagu:
     * Saat salah satu lagu di list di-klik, ambil 'id' lagunya, lalu tembak dulu ke endpoint stream:
     * https://server-kappa-black-72.vercel.app/api/stream?id={selectedTrackId} (serta mendukung https://vercel.app{selectedTrackId})
     *
     * 3. Audio Engine:
     * Ambil string 'urlAudioMurni' dari respon JSON stream itu.
     */
    suspend fun fetchPureAudioStream(selectedTrackId: String, streamBaseUrl: String = DEFAULT_STREAM_URL): String? = withContext(Dispatchers.IO) {
        if (selectedTrackId.isBlank()) return@withContext null

        val cleanBase = streamBaseUrl.trim().trimEnd('/')
        val encodedId = URLEncoder.encode(selectedTrackId.trim(), "UTF-8")

        val candidateUrls = listOf(
            "https://server-kappa-black-72.vercel.app/api/stream?id=$encodedId",
            if (cleanBase.contains("?")) "$cleanBase&id=$encodedId"
            else if (cleanBase.endsWith("/api/stream")) "$cleanBase?id=$encodedId"
            else "$cleanBase/$encodedId",
            "$cleanBase/api/stream?id=$encodedId"
        ).distinct()

        for (url in candidateUrls) {
            repeat(3) { attempt ->
                try {
                    Log.d(TAG, "HTTP GET Stream to Vercel (Attempt ${attempt + 1}): $url")
                    val request = Request.Builder()
                        .url(url)
                        .get()
                        .header("Accept", "application/json")
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        val body = response.body?.string()?.trim()
                        if (response.isSuccessful && !body.isNullOrBlank()) {
                            val pureUrl = parsePureAudioUrl(body)
                            if (!pureUrl.isNullOrBlank()) {
                                Log.d(TAG, "Successfully obtained urlAudioMurni from $url: $pureUrl")
                                return@withContext pureUrl
                            }
                        } else {
                            Log.d(TAG, "Stream HTTP ${response.code} on $url: $body")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Stream request error on $url (Attempt ${attempt + 1}): ${e.message}")
                }
            }
        }

        return@withContext null
    }

    /**
     * Mengambil string 'urlAudioMurni' dari respon JSON endpoint stream Vercel.
     */
    private fun parsePureAudioUrl(body: String): String? {
        try {
            if (body.startsWith("{")) {
                val json = JSONObject(body)
                val urlAudioMurni = json.optString("urlAudioMurni", "").trim()
                if (urlAudioMurni.startsWith("http://") || urlAudioMurni.startsWith("https://")) {
                    return urlAudioMurni
                }
                val dataObj = json.optJSONObject("data") ?: json.optJSONObject("result")
                if (dataObj != null) {
                    val nestedUrl = dataObj.optString("urlAudioMurni", "").trim()
                    if (nestedUrl.startsWith("http://") || nestedUrl.startsWith("https://")) {
                        return nestedUrl
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing stream JSON for urlAudioMurni: ${e.message}")
        }
        return null
    }

    /**
     * Memetakan hasil JSON dari server Vercel ke daftar UI:
     * id, judul, artis, album, dan coverArt
     */
    private fun parseSongsJson(body: String): List<Song> {
        val result = mutableListOf<Song>()
        try {
            val jsonArray: JSONArray = when {
                body.startsWith("[") -> JSONArray(body)
                body.startsWith("{") -> {
                    val obj = JSONObject(body)
                    obj.optJSONArray("data")
                        ?: obj.optJSONArray("results")
                        ?: obj.optJSONArray("songs")
                        ?: JSONArray()
                }
                else -> return emptyList()
            }

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i) ?: continue

                val id = item.optString("id").trim()
                if (id.isBlank()) continue

                val judul = item.optString("judul").ifBlank {
                    item.optString("title")
                }.trim()

                val artis = item.optString("artis").ifBlank {
                    item.optString("artist")
                }.trim()

                val album = item.optString("album").trim()

                val coverArt = item.optString("coverArt").ifBlank {
                    item.optString("image")
                }.trim()

                result.add(
                    Song(
                        id = id,
                        title = judul.ifBlank { "Untitled" },
                        artist = artis.ifBlank { "Unknown Artist" },
                        album = album.ifBlank { "Single" },
                        durationMs = 0L,
                        isExplicit = false,
                        albumArtUrl = coverArt.ifBlank { null },
                        audioPathOrUrl = "", // Akan selalu diambil dari urlAudioMurni saat lagu di-klik
                        isDownloaded = false,
                        isFavorite = false,
                        genre = "Music",
                        lyricsLrc = ""
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing songs JSON from Vercel: ${e.message}", e)
        }
        return result
    }
}
