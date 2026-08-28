package com.radimak.tv.data

import com.radimak.tv.model.IptvContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader

class M3uParserTest {
    @Test
    fun `organiza canais abertos e exclui canais de assinatura`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1 group-title="Brazil",SBT
            https://example.com/sbt.m3u8
            #EXTINF:-1 group-title="Brazil",Premiere 1
            https://example.com/premiere.m3u8
        """.trimIndent()

        val items = M3uParser.parse(playlist)

        assertEquals(1, items.size)
        assertEquals("Canais abertos", items.single().group)
    }

    @Test
    fun `le metadados e organiza tipos da lista`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1 tvg-logo="https://example.com/tv.png" group-title="TV Ao Vivo",Canal Público, Maceió
            https://example.com/live.m3u8
            #EXTINF:-1 group-title="Filmes",Filme de Domínio Público
            https://example.com/movie.mp4
            #EXTINF:-1 group-title="Séries",Série Exemplo S01E01
            https://example.com/episode.m3u8
        """.trimIndent()

        val items = M3uParser.parse(playlist)

        assertEquals(3, items.size)
        assertEquals(IptvContentType.LIVE, items[0].contentType)
        assertEquals(IptvContentType.MOVIE, items[1].contentType)
        assertEquals(IptvContentType.SERIES, items[2].contentType)
        assertEquals("Canal Público, Maceió", items[0].name)
        assertEquals("https://example.com/tv.png", items[0].logoUrl)
    }

    @Test
    fun `le grupo alternativo e cabecalhos vlc`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1,Canal Teste
            #EXTGRP:Canais locais
            #EXTVLCOPT:http-user-agent=Radimak Player
            #EXTVLCOPT:http-referrer=https://example.com/
            https://example.com/channel.m3u8
        """.trimIndent()

        val item = M3uParser.parse(playlist).single()

        assertEquals("Canais locais", item.group)
        assertEquals("Radimak Player", item.userAgent)
        assertEquals("https://example.com/", item.referrer)
        assertNull(item.logoUrl)
    }

    @Test
    fun `le cabecalhos anexados ao endereco do stream`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1 group-title="TV Ao Vivo",Canal com cabeçalho
            https://example.com/live.m3u8|User-Agent=Radimak%20TV&Referer=https%3A%2F%2Fexample.com%2F
        """.trimIndent()

        val item = M3uParser.parse(playlist.reader().buffered()).single()

        assertEquals("https://example.com/live.m3u8", item.streamUrl)
        assertEquals("Radimak TV", item.userAgent)
        assertEquals("https://example.com/", item.referrer)
    }

    @Test
    fun `preserva itens recebidos quando a conexao e interrompida`() {
        val playlist = buildString {
            appendLine("#EXTM3U")
            repeat(50) { index ->
                appendLine("#EXTINF:-1 group-title=\"TV Ao Vivo\",Canal $index")
                appendLine("https://example.com/live/$index.m3u8")
            }
        }
        val reader = object : BufferedReader(StringReader(playlist)) {
            private var calls = 0

            override fun readLine(): String? {
                if (calls++ == 41) throw IOException("conexão interrompida")
                return super.readLine()
            }
        }

        val result = M3uParser.parseResult(reader)

        assertEquals(20, result.items.size)
        assertTrue(result.isPartial)
    }

    @Test
    fun `ignora paginas web que o player interno nao reproduz`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1 group-title="Movies",Cine Público
            https://example.com/cine.m3u8
            #EXTINF:-1 group-title="Ao vivo",Canal em página do YouTube
            https://www.youtube.com/@canal/live
        """.trimIndent()

        val items = M3uParser.parse(playlist)

        assertEquals(1, items.size)
        assertEquals(IptvContentType.MOVIE, items.single().contentType)
    }
}
