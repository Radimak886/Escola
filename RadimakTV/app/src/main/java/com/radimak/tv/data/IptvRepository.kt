package com.radimak.tv.data

import android.content.Context
import android.net.Uri
import com.radimak.tv.model.IptvItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.GZIPInputStream

class IptvLoadException(message: String, cause: Throwable? = null) : IOException(message, cause)

data class IptvLoadResult(
    val items: List<IptvItem>,
    val isPartial: Boolean = false,
)

class IptvRepository(private val context: Context) {
    suspend fun loadUrl(value: String): IptvLoadResult = withContext(Dispatchers.IO) {
        val url = try {
            URL(value)
        } catch (error: MalformedURLException) {
            throw IptvLoadException("O endereço da lista M3U é inválido.", error)
        }
        if (url.protocol != "https" && url.protocol != "http") {
            throw IptvLoadException("A lista precisa usar um endereço HTTP ou HTTPS.")
        }

        var firstFailure: IptvLoadException? = null
        var partialFallback: IptvLoadResult? = null
        repeat(URL_ATTEMPTS) { attempt ->
            try {
                val result = loadUrlOnce(
                    url = url,
                    originalValue = value,
                    requestGzip = attempt == 0,
                )
                if (attempt == 0 && result.isPartial) {
                    partialFallback = result
                    delay(RETRY_DELAY_MS)
                } else {
                    return@withContext result
                }
            } catch (error: IptvLoadException) {
                if (attempt == 0 && error.canRetry()) {
                    firstFailure = error
                    delay(RETRY_DELAY_MS)
                } else if (partialFallback != null) {
                    return@withContext partialFallback!!
                } else {
                    throw if (attempt > 0) error else firstFailure ?: error
                }
            }
        }
        return@withContext partialFallback
            ?: throw firstFailure ?: IptvLoadException("Falha de conexão ao carregar a lista M3U.")
    }

    private fun loadUrlOnce(
        url: URL,
        originalValue: String,
        requestGzip: Boolean,
    ): IptvLoadResult {
        val connection = try {
            (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty(
                    "Accept",
                    "audio/x-mpegurl, application/vnd.apple.mpegurl, text/plain, application/octet-stream, */*",
                )
                setRequestProperty("Accept-Encoding", if (requestGzip) "gzip" else "identity")
                setRequestProperty("Connection", "close")
                setRequestProperty("User-Agent", "RadimakTV/0.5.0 (Android; M3U)")
                useCaches = false
            }
        } catch (error: Exception) {
            throw error.toSafeLoadException()
        }
        try {
            val status = connection.responseCode
            if (status !in 200..299) throw IptvLoadException(status.safeHttpMessage())
            val rawStream = connection.inputStream
            val stream = if (connection.contentEncoding.equals("gzip", ignoreCase = true)) {
                GZIPInputStream(rawStream)
            } else {
                rawStream
            }
            return parseAndCache(stream, "url:$originalValue")
        } catch (error: IptvLoadException) {
            throw error
        } catch (error: Exception) {
            throw error.toSafeLoadException()
        } finally {
            connection.disconnect()
        }
    }

    suspend fun loadFile(uri: Uri): IptvLoadResult = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                parseAndCache(stream, "file:$uri")
            } ?: throw IptvLoadException("Não foi possível abrir o arquivo selecionado.")
        } catch (error: IptvLoadException) {
            throw error
        } catch (error: Exception) {
            throw error.toSafeLoadException(isFile = true)
        }
    }

    suspend fun loadCached(source: String): List<IptvItem> = withContext(Dispatchers.IO) {
        val cache = cacheFile(source)
        if (!cache.isFile) return@withContext emptyList()
        runCatching {
            cache.inputStream().buffered().use { input ->
                InputStreamReader(input, StandardCharsets.UTF_8).buffered().use { reader ->
                    if (reader.readLine() != cacheHeader(source)) {
                        emptyList()
                    } else {
                        M3uParser.parse(reader)
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun clearCache() {
        context.filesDir.listFiles()
            ?.filter { it.name.startsWith(CACHE_FILE_PREFIX) && it.name.endsWith(CACHE_FILE_SUFFIX) }
            ?.forEach(File::delete)
        context.cacheDir.listFiles()
            ?.filter { it.name.startsWith(CACHE_TEMP_PREFIX) }
            ?.forEach(File::delete)
    }

    private fun parseAndCache(stream: InputStream, source: String): IptvLoadResult {
        val temporary = File.createTempFile(CACHE_TEMP_PREFIX, ".tmp", context.cacheDir)
        return try {
            val parsed = temporary.outputStream().buffered().use { output ->
                output.write((cacheHeader(source) + "\n").toByteArray(StandardCharsets.UTF_8))
                val copyingStream = CopyingInputStream(stream, output)
                InputStreamReader(copyingStream, StandardCharsets.UTF_8).buffered().use(::parseChecked)
            }
            val target = cacheFile(source)
            if (!temporary.renameTo(target)) {
                temporary.copyTo(target, overwrite = true)
                temporary.delete()
            }
            IptvLoadResult(items = parsed.items, isPartial = parsed.isPartial)
        } catch (error: Exception) {
            temporary.delete()
            throw error
        }
    }

    private fun parseChecked(reader: BufferedReader): M3uParser.ParseResult {
        return M3uParser.parseResult(reader).also {
            if (it.items.isEmpty()) {
                throw IptvLoadException(
                    "A resposta não contém itens M3U reproduzíveis. Verifique as credenciais e o formato da lista.",
                )
            }
        }
    }

    private fun cacheFile(source: String): File {
        val fileName = CACHE_FILE_PREFIX + sourceHash(source).take(24) + CACHE_FILE_SUFFIX
        return File(context.filesDir, fileName)
    }

    private fun cacheHeader(source: String): String = CACHE_HEADER_PREFIX + sourceHash(source)

    private fun sourceHash(source: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString(separator = "") {
            String.format(Locale.ROOT, "%02x", it.toInt() and 0xff)
        }
    }

    private fun IptvLoadException.canRetry(): Boolean {
        return cause != null || message?.contains("indisponível", ignoreCase = true) == true
    }

    private fun Int.safeHttpMessage(): String = when (this) {
        HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN ->
            "O servidor recusou o acesso (código $this). Verifique se as credenciais estão ativas."
        HttpURLConnection.HTTP_NOT_FOUND ->
            "O servidor não encontrou a lista (código 404). Verifique o endereço fornecido."
        in 500..599 ->
            "O servidor da lista está indisponível no momento (código $this)."
        else ->
            "O servidor não permitiu carregar a lista (código $this)."
    }

    private fun Throwable.toSafeLoadException(isFile: Boolean = false): IptvLoadException = when (this) {
        is IptvLoadException -> this
        is SocketTimeoutException -> IptvLoadException(
            "O servidor demorou demais para enviar a lista. Tente novamente em uma conexão estável.",
            this,
        )
        is UnknownHostException -> IptvLoadException(
            "Não foi possível localizar o servidor. Verifique a internet ou o endereço da lista.",
            this,
        )
        is ConnectException -> IptvLoadException(
            "Não foi possível conectar ao servidor da lista.",
            this,
        )
        is FileNotFoundException -> IptvLoadException(
            if (isFile) "O arquivo M3U não está mais disponível neste aparelho." else "A lista não foi encontrada no servidor.",
            this,
        )
        is SecurityException -> IptvLoadException(
            "O Android não autorizou a leitura da lista selecionada.",
            this,
        )
        else -> IptvLoadException(
            if (isFile) "Não foi possível ler o arquivo M3U." else "Falha de conexão ao carregar a lista M3U.",
            this,
        )
    }

    private class CopyingInputStream(
        input: InputStream,
        private val copy: OutputStream,
    ) : FilterInputStream(input) {
        override fun read(): Int {
            return super.read().also { value ->
                if (value >= 0) copy.write(value)
            }
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            return super.read(buffer, offset, length).also { count ->
                if (count > 0) copy.write(buffer, offset, count)
            }
        }
    }

    companion object {
        fun safeMessage(error: Throwable): String {
            return (error as? IptvLoadException)?.message
                ?: "Não foi possível carregar a lista M3U. Verifique o endereço, as credenciais e a conexão."
        }

        private const val CONNECT_TIMEOUT_MS = 30_000
        private const val READ_TIMEOUT_MS = 180_000
        private const val URL_ATTEMPTS = 2
        private const val RETRY_DELAY_MS = 900L
        private const val CACHE_HEADER_PREFIX = "#RADIMAK-CACHE:"
        private const val CACHE_FILE_PREFIX = "iptv_catalog_"
        private const val CACHE_FILE_SUFFIX = ".m3u"
        private const val CACHE_TEMP_PREFIX = "iptv_download_"
    }
}
