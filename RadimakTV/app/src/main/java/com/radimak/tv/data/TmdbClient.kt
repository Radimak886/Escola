package com.radimak.tv.data

import com.radimak.tv.model.CatalogItem
import com.radimak.tv.model.MediaType
import com.radimak.tv.model.ProviderAccess
import com.radimak.tv.model.ProviderInfo
import com.radimak.tv.model.StreamingService
import com.radimak.tv.model.WatchAvailability
import com.radimak.tv.util.ProviderMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TmdbClient(private val tokenProvider: () -> String) {
    suspend fun trending(): List<CatalogItem> = parseCatalog(
        request("/trending/all/week?language=pt-BR"),
    )

    suspend fun popularMovies(): List<CatalogItem> = parseCatalog(
        request("/movie/popular?language=pt-BR&region=BR"),
        forcedType = MediaType.MOVIE,
    )

    suspend fun popularSeries(): List<CatalogItem> = parseCatalog(
        request("/tv/popular?language=pt-BR"),
        forcedType = MediaType.SERIES,
    )

    suspend fun freeWithAds(services: List<StreamingService>): List<CatalogItem> = coroutineScope {
        val enabledFreeServices = services.filter { it.enabled && it.isFree }
        if (enabledFreeServices.isEmpty()) return@coroutineScope emptyList()

        val movieProviderIds = async { providerIds(MediaType.MOVIE, enabledFreeServices) }
        val seriesProviderIds = async { providerIds(MediaType.SERIES, enabledFreeServices) }
        val movieIds = movieProviderIds.await()
        val seriesIds = seriesProviderIds.await()

        val movies = async { discoverFree(MediaType.MOVIE, movieIds) }
        val series = async { discoverFree(MediaType.SERIES, seriesIds) }
        (movies.await() + series.await())
            .distinctBy { it.favoriteKey }
            .sortedByDescending { it.rating }
            .take(30)
    }

    suspend fun search(query: String): List<CatalogItem> {
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        return parseCatalog(request("/search/multi?language=pt-BR&region=BR&query=$encoded"))
    }

    suspend fun watchAvailability(
        item: CatalogItem,
        services: List<StreamingService>,
    ): WatchAvailability {
        if (item.id < 0) return WatchAvailability()
        val response = request("/${item.mediaType.apiValue}/${item.id}/watch/providers")
        val brazil = response.optJSONObject("results")?.optJSONObject("BR") ?: return WatchAvailability()
        val providers = buildList {
            addAll(parseProviders(brazil.optJSONArray("ads"), services, ProviderAccess.FREE_WITH_ADS))
            addAll(parseProviders(brazil.optJSONArray("free"), services, ProviderAccess.FREE))
            addAll(parseProviders(brazil.optJSONArray("flatrate"), services, ProviderAccess.SUBSCRIPTION))
        }.distinctBy { it.id }
        return WatchAvailability(
            link = brazil.optString("link").takeIf { it.isNotBlank() },
            providers = providers.sortedBy {
                when (it.access) {
                    ProviderAccess.FREE_WITH_ADS -> 0
                    ProviderAccess.FREE -> 1
                    ProviderAccess.SUBSCRIPTION -> 2
                    ProviderAccess.OTHER -> 3
                }
            },
        )
    }

    private suspend fun providerIds(
        mediaType: MediaType,
        services: List<StreamingService>,
    ): List<Int> {
        val root = request("/watch/providers/${mediaType.apiValue}?language=pt-BR&watch_region=BR")
        val results = root.optJSONArray("results") ?: JSONArray()
        return buildList {
            for (index in 0 until results.length()) {
                val provider = results.optJSONObject(index) ?: continue
                val name = provider.optString("provider_name")
                if (ProviderMatcher.matchingService(name, services) != null) {
                    add(provider.optInt("provider_id"))
                }
            }
        }.filter { it > 0 }.distinct()
    }

    private suspend fun discoverFree(mediaType: MediaType, providerIds: List<Int>): List<CatalogItem> {
        if (providerIds.isEmpty()) return emptyList()
        val providerFilter = providerIds.joinToString("%7C")
        val common = "language=pt-BR&watch_region=BR&sort_by=popularity.desc" +
            "&with_watch_monetization_types=free%7Cads&with_watch_providers=$providerFilter"
        val path = when (mediaType) {
            MediaType.MOVIE -> "/discover/movie?$common&region=BR&include_adult=false&include_video=false"
            MediaType.SERIES -> "/discover/tv?$common&include_adult=false"
        }
        return parseCatalog(request(path), forcedType = mediaType)
    }

    private suspend fun request(path: String): JSONObject = withContext(Dispatchers.IO) {
        val credential = tokenProvider().trim()
        require(credential.isNotBlank()) { "Configure a chave do TMDB no Perfil." }

        val isReadToken = credential.startsWith("eyJ") || credential.length > 80
        val separator = if (path.contains('?')) '&' else '?'
        val resolvedPath = if (isReadToken) path else "$path${separator}api_key=${URLEncoder.encode(credential, "UTF-8")}" 
        val connection = (URL("$BASE_URL$resolvedPath").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
            if (isReadToken) setRequestProperty("Authorization", "Bearer $credential")
        }

        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val apiMessage = runCatching { JSONObject(body).optString("status_message") }.getOrNull()
                throw IllegalStateException(apiMessage?.takeIf { it.isNotBlank() } ?: "TMDB respondeu com código $status")
            }
            JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseCatalog(root: JSONObject, forcedType: MediaType? = null): List<CatalogItem> {
        val results = root.optJSONArray("results") ?: JSONArray()
        return buildList {
            for (index in 0 until results.length()) {
                val item = results.optJSONObject(index) ?: continue
                val type = forcedType ?: when (item.optString("media_type")) {
                    "movie" -> MediaType.MOVIE
                    "tv" -> MediaType.SERIES
                    else -> continue
                }
                val title = if (type == MediaType.MOVIE) item.optString("title") else item.optString("name")
                if (title.isBlank()) continue
                add(
                    CatalogItem(
                        id = item.optLong("id"),
                        title = title,
                        overview = item.optString("overview").ifBlank { "Sinopse ainda não disponível em português." },
                        posterPath = item.optNullableString("poster_path"),
                        backdropPath = item.optNullableString("backdrop_path"),
                        rating = item.optDouble("vote_average", 0.0),
                        releaseDate = if (type == MediaType.MOVIE) item.optString("release_date") else item.optString("first_air_date"),
                        mediaType = type,
                        accentColor = accentFor(item.optLong("id")),
                    ),
                )
            }
        }
    }

    private fun parseProviders(
        array: JSONArray?,
        services: List<StreamingService>,
        offeredAccess: ProviderAccess,
    ): List<ProviderInfo> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val name = item.optString("provider_name")
                val matchedService = ProviderMatcher.matchingService(name, services)
                add(
                    ProviderInfo(
                        id = item.optInt("provider_id"),
                        name = name,
                        logoPath = item.optNullableString("logo_path"),
                        access = when (offeredAccess) {
                            ProviderAccess.FREE, ProviderAccess.FREE_WITH_ADS -> offeredAccess
                            ProviderAccess.SUBSCRIPTION -> if (matchedService != null) {
                                ProviderAccess.SUBSCRIPTION
                            } else {
                                ProviderAccess.OTHER
                            }
                            ProviderAccess.OTHER -> ProviderAccess.OTHER
                        },
                        officialUrl = matchedService?.homeUrl,
                    ),
                )
            }
        }
    }

    private fun JSONObject.optNullableString(key: String): String? =
        optString(key).takeIf { it.isNotBlank() && it != "null" }

    private fun accentFor(id: Long): Long {
        val colors = longArrayOf(0xFFF97316, 0xFFDC2626, 0xFF7C3AED, 0xFF2563EB, 0xFF059669, 0xFFB45309)
        return colors[(kotlin.math.abs(id) % colors.size).toInt()]
    }

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/3"
    }
}
