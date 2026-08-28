package com.radimak.tv.data

import com.radimak.tv.model.IptvContentType
import com.radimak.tv.model.IptvItem
import java.io.BufferedReader
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale

object M3uParser {
    private val attributeRegex = Regex("""([\w-]+)=[\"']([^\"']*)[\"']""")
    private val seriesEpisodeRegex = Regex("""\bs\d{1,2}e\d{1,3}\b""", RegexOption.IGNORE_CASE)

    data class ParseResult(
        val items: List<IptvItem>,
        val isPartial: Boolean,
    )

    fun parse(content: String): List<IptvItem> = content.reader().buffered().use(::parse)

    fun parse(reader: BufferedReader): List<IptvItem> = parseResult(reader).items

    fun parseResult(reader: BufferedReader): ParseResult {
        var metadata: PendingMetadata? = null
        var extGroup: String? = null
        var userAgent: String? = null
        var referrer: String? = null
        var linesRead = 0
        val seenUrls = HashSet<String>()
        val items = ArrayList<IptvItem>()

        var interrupted = false
        try {
            while (linesRead < MAX_LINES && items.size < MAX_ITEMS) {
                val rawLine = reader.readLine() ?: break
                linesRead += 1
                val line = rawLine.trim().removePrefix("\uFEFF")
                when {
                    line.startsWith("#EXTINF", ignoreCase = true) -> {
                        metadata = parseMetadata(line)
                        extGroup = null
                        userAgent = null
                        referrer = null
                    }

                    line.startsWith("#EXTGRP:", ignoreCase = true) -> {
                        extGroup = line.substringAfter(':').trim().takeIf { it.isNotBlank() }
                    }

                    line.startsWith("#EXTVLCOPT:http-user-agent=", ignoreCase = true) -> {
                        userAgent = line.substringAfter('=').trim().takeIf { it.isNotBlank() }
                    }

                    line.startsWith("#EXTVLCOPT:http-referrer=", ignoreCase = true) ||
                        line.startsWith("#EXTVLCOPT:http-referer=", ignoreCase = true) -> {
                        referrer = line.substringAfter('=').trim().takeIf { it.isNotBlank() }
                    }

                    line.isNotBlank() && !line.startsWith('#') -> {
                        val pending = metadata ?: PendingMetadata()
                        val target = parseStreamTarget(line, userAgent, referrer)
                        if (target != null && seenUrls.add(target.url)) {
                            val name = pending.name.ifBlank { pending.attributes["tvg-name"].orEmpty() }
                                .ifBlank { "Canal sem nome" }
                            val group = pending.attributes["group-title"]
                                ?.takeIf { it.isNotBlank() }
                                ?: extGroup
                                ?: "Sem categoria"
                            items += IptvItem(
                                name = name,
                                streamUrl = target.url,
                                logoUrl = pending.attributes["tvg-logo"]?.takeIf { it.isNotBlank() },
                                group = group,
                                contentType = classify(name, group, target.url),
                                userAgent = target.userAgent,
                                referrer = target.referrer,
                            )
                        }
                        metadata = null
                        extGroup = null
                        userAgent = null
                        referrer = null
                    }
                }
            }
        } catch (error: IOException) {
            if (items.isEmpty()) throw error
            interrupted = true
        }
        return ParseResult(
            items = items,
            isPartial = interrupted || linesRead >= MAX_LINES || items.size >= MAX_ITEMS,
        )
    }

    private fun parseStreamTarget(
        line: String,
        declaredUserAgent: String?,
        declaredReferrer: String?,
    ): StreamTarget? {
        val separator = line.indexOf('|')
        val url = if (separator >= 0) line.substring(0, separator).trim() else line
        if (!url.startsWith("https://", true) && !url.startsWith("http://", true)) return null
        if (isUnsupportedWebPage(url)) return null
        if (separator < 0) return StreamTarget(url, declaredUserAgent, declaredReferrer)

        var userAgent = declaredUserAgent
        var referrer = declaredReferrer
        line.substring(separator + 1).split('&').forEach { entry ->
            val key = entry.substringBefore('=', "").trim().lowercase(Locale.ROOT)
            val value = entry.substringAfter('=', "").decodeHeaderValue().takeIf { it.isNotBlank() }
            when (key) {
                "user-agent" -> userAgent = value ?: userAgent
                "referer", "referrer" -> referrer = value ?: referrer
            }
        }
        return StreamTarget(url, userAgent, referrer)
    }

    private fun isUnsupportedWebPage(url: String): Boolean {
        val normalized = url.lowercase(Locale.ROOT)
        return listOf("youtube.com/", "youtu.be/", "twitch.tv/").any(normalized::contains)
    }

    private fun String.decodeHeaderValue(): String = runCatching {
        URLDecoder.decode(this, StandardCharsets.UTF_8.name())
    }.getOrDefault(this)
    private fun parseMetadata(line: String): PendingMetadata {
        val attributes = attributeRegex.findAll(line).associate { match ->
            match.groupValues[1].lowercase(Locale.ROOT) to match.groupValues[2].trim()
        }
        val name = displayName(line)
        return PendingMetadata(name, attributes)
    }

    private fun displayName(line: String): String {
        var quote: Char? = null
        line.forEachIndexed { index, char ->
            when {
                char == quote -> quote = null
                quote == null && (char == '\"' || char == '\'') -> quote = char
                quote == null && char == ',' -> return line.substring(index + 1).trim()
            }
        }
        return ""
    }

    private fun classify(name: String, group: String, url: String): IptvContentType {
        val descriptor = "$group $name".lowercase(Locale.ROOT)
        return when {
            listOf("serie", "série", "series", "temporada", "season", "episodio", "episódio")
                .any(descriptor::contains) || seriesEpisodeRegex.containsMatchIn(descriptor) -> IptvContentType.SERIES

            listOf("filme", "filmes", "movie", "movies", "cinema", "cine", "vod")
                .any(descriptor::contains) || url.substringBefore('?').lowercase(Locale.ROOT)
                .endsWithAny(".mp4", ".mkv", ".avi", ".mov", ".webm") -> IptvContentType.MOVIE

            else -> IptvContentType.LIVE
        }
    }

    private fun String.endsWithAny(vararg suffixes: String): Boolean = suffixes.any(::endsWith)

    private data class PendingMetadata(
        val name: String = "",
        val attributes: Map<String, String> = emptyMap(),
    )

    private data class StreamTarget(
        val url: String,
        val userAgent: String?,
        val referrer: String?,
    )

    private const val MAX_LINES = 1_250_000
    private const val MAX_ITEMS = 250_000
}
