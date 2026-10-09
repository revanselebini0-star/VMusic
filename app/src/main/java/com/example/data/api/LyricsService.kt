package com.example.data.api

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class LrcLibResponse(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "trackName") val trackName: String? = null,
    @Json(name = "artistName") val artistName: String? = null,
    @Json(name = "albumName") val albumName: String? = null,
    @Json(name = "duration") val duration: Double? = null,
    @Json(name = "instrumental") val instrumental: Boolean? = null,
    @Json(name = "plainLyrics") val plainLyrics: String? = null,
    @Json(name = "syncedLyrics") val syncedLyrics: String? = null
)

interface LrcLibApi {
    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String
    ): LrcLibResponse?

    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): List<LrcLibResponse>?
}

object LyricsService {
    private const val TAG = "LyricsService"
    private const val BASE_URL = "https://lrclib.net/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "VanzMusic/1.0.0 (Android; Linux; rv:1.0)")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val api: LrcLibApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LrcLibApi::class.java)
    }

    suspend fun fetchRealLyrics(title: String, artist: String): String? = withContext(Dispatchers.IO) {
        try {
            var cleanTitle = title
            if (cleanTitle.contains(" - ")) {
                cleanTitle = cleanTitle.split(" - ").last()
            }
            cleanTitle = cleanTitle
                .replace(Regex("""(?i)\(feat.*?\)|\[feat.*?\]|feat\..*|ft\..*"""), "")
                .replace(Regex("""(?i)\(official\s*(music\s*|lyric\s*)?video\)"""), "")
                .replace(Regex("""(?i)\[official\s*(music\s*|lyric\s*)?video\]"""), "")
                .replace(Regex("""(?i)\[oficial\s*video\]"""), "")
                .replace(Regex("""(?i)\(oficial\s*video\)"""), "")
                .replace(Regex("""(?i)\(official\s*audio\)"""), "")
                .replace(Regex("""(?i)\[official\s*audio\]"""), "")
                .replace(Regex("""(?i)\[mv\]|\(mv\)"""), "")
                .replace(Regex("""(?i)\(live.*?\)"""), "")
                .trim()

            val cleanArtist = artist
                .replace(Regex("""(?i)\s*-\s*Topic$"""), "")
                .replace(Regex("""(?i)VEVO$"""), "")
                .trim()

            // 1. Try exact match first
            try {
                val exact = api.getLyrics(cleanTitle, cleanArtist)
                if (exact?.syncedLyrics?.isNotBlank() == true) {
                    return@withContext exact.syncedLyrics
                }
                if (exact?.plainLyrics?.isNotBlank() == true) {
                    return@withContext convertPlainToTimedLyrics(exact.plainLyrics)
                }
            } catch (e: HttpException) {
                // 404 is normal when song lyrics are not yet registered
                if (e.code() != 404) {
                    Log.d(TAG, "Exact lyrics lookup returned status ${e.code()}")
                }
            }

            // 2. Fallback to search query
            try {
                val searchResults = api.searchLyrics("$cleanTitle $cleanArtist")
                val bestMatch = searchResults?.firstOrNull { it.syncedLyrics?.isNotBlank() == true }
                if (bestMatch?.syncedLyrics?.isNotBlank() == true) {
                    return@withContext bestMatch.syncedLyrics
                }
                val plainMatch = searchResults?.firstOrNull { it.plainLyrics?.isNotBlank() == true }
                if (plainMatch?.plainLyrics?.isNotBlank() == true) {
                    return@withContext convertPlainToTimedLyrics(plainMatch.plainLyrics)
                }
            } catch (e: HttpException) {
                if (e.code() != 404) {
                    Log.d(TAG, "Search lyrics returned status ${e.code()}")
                }
            }

        } catch (e: Exception) {
            Log.d(TAG, "Lyrics not found for '$title' ($artist), using timed track lyrics.")
        }
        return@withContext null
    }

    private fun convertPlainToTimedLyrics(plain: String): String {
        val lines = plain.lines().filter { it.isNotBlank() }
        val sb = StringBuilder()
        var sec = 4L
        for (line in lines) {
            val mm = sec / 60
            val ss = sec % 60
            sb.append(String.format("[%02d:%02d.00] %s\n", mm, ss, line.trim()))
            sec += 5L
        }
        return sb.toString()
    }
}
