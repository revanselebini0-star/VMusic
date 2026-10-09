package com.example.lyrics

import com.example.data.model.LyricLine

object LyricsParser {

    private val LRC_REGEX = Regex("""\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?](.*)""")

    fun parse(lrcContent: String): List<LyricLine> {
        if (lrcContent.isBlank()) return emptyList()

        val lines = mutableListOf<LyricLine>()
        val rawLines = lrcContent.lineSequence()

        for (line in rawLines) {
            val trimmed = line.trim()
            val match = LRC_REGEX.find(trimmed)
            if (match != null) {
                val (minStr, secStr, fracStr, text) = match.destructured
                val minutes = minStr.toLongOrNull() ?: 0L
                val seconds = secStr.toLongOrNull() ?: 0L
                val fraction = when {
                    fracStr.isEmpty() -> 0L
                    fracStr.length == 2 -> (fracStr.toLongOrNull() ?: 0L) * 10
                    else -> fracStr.toLongOrNull() ?: 0L
                }
                val timeMs = (minutes * 60 + seconds) * 1000 + fraction
                val lyricText = text.trim()
                if (lyricText.isNotEmpty()) {
                    lines.add(LyricLine(timeMs = timeMs, text = lyricText))
                }
            }
        }

        return lines.sortedBy { it.timeMs }
    }

    fun findActiveLineIndex(lyrics: List<LyricLine>, currentPositionMs: Long): Int {
        if (lyrics.isEmpty()) return -1
        if (currentPositionMs < lyrics.first().timeMs) return 0

        for (i in lyrics.indices.reversed()) {
            if (currentPositionMs >= lyrics[i].timeMs) {
                return i
            }
        }
        return 0
    }
}
