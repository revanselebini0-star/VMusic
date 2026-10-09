package com.example

import com.example.data.api.VercelMusicService
import com.example.lyrics.LyricsParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun lyricsParser_parsesCorrectly() {
        val lrc = """
            [00:03.50] Tell me that kind of thing
            [00:07.20] 'Cause I've been thinkin' 'bout you
            [00:10.50] Say what you want
        """.trimIndent()

        val parsed = LyricsParser.parse(lrc)
        assertEquals(3, parsed.size)
        assertEquals(3500L, parsed[0].timeMs)
        assertEquals("Tell me that kind of thing", parsed[0].text)

        val activeIndex1 = LyricsParser.findActiveLineIndex(parsed, 5000L)
        assertEquals(0, activeIndex1)

        val activeIndex2 = LyricsParser.findActiveLineIndex(parsed, 8000L)
        assertEquals(1, activeIndex2)

        val activeIndex3 = LyricsParser.findActiveLineIndex(parsed, 12000L)
        assertEquals(2, activeIndex3)
    }

    @Test
    fun vercelMusicService_defaultUrlsConfigured() {
        assertEquals("https://server-kappa-black-72.vercel.app/api/search", VercelMusicService.DEFAULT_SEARCH_URL)
        assertEquals("https://vercel.app", VercelMusicService.DEFAULT_STREAM_URL)
    }
}
