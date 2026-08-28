package com.radimak.tv.model

enum class MediaType(val apiValue: String, val label: String) {
    MOVIE("movie", "Filme"),
    SERIES("tv", "Série"),
}

data class CatalogItem(
    val id: Long,
    val title: String,
    val overview: String,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val rating: Double = 0.0,
    val releaseDate: String = "",
    val mediaType: MediaType,
    val genres: List<String> = emptyList(),
    val accentColor: Long = 0xFFF97316,
) {
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

    val backdropUrl: String?
        get() = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" }

    val year: String
        get() = releaseDate.take(4).ifBlank { "—" }

    val favoriteKey: String
        get() = "${mediaType.apiValue}:$id"
}

data class ProviderInfo(
    val id: Int,
    val name: String,
    val logoPath: String? = null,
    val access: ProviderAccess = ProviderAccess.OTHER,
    val officialUrl: String? = null,
) {
    val logoUrl: String?
        get() = logoPath?.let { "https://image.tmdb.org/t/p/w92$it" }

    val includedInSubscription: Boolean
        get() = access == ProviderAccess.SUBSCRIPTION
}

enum class ProviderAccess(val label: String) {
    SUBSCRIPTION("Você assina"),
    FREE("Grátis"),
    FREE_WITH_ADS("Grátis com anúncios"),
    OTHER("Outra opção"),
}

data class WatchAvailability(
    val link: String? = null,
    val providers: List<ProviderInfo> = emptyList(),
)

data class StreamingService(
    val id: String,
    val displayName: String,
    val matchTerms: List<String>,
    val enabled: Boolean,
    val isFree: Boolean = false,
    val homeUrl: String? = null,
)

data class CatalogBundle(
    val trending: List<CatalogItem>,
    val movies: List<CatalogItem>,
    val series: List<CatalogItem>,
    val freeWithAds: List<CatalogItem>,
)

enum class IptvContentType(val label: String) {
    LIVE("Ao vivo"),
    MOVIE("Filmes"),
    SERIES("Séries"),
}

data class IptvServer(
    val id: String,
    val label: String,
    val description: String,
    val url: String,
)

data class IptvItem(
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val group: String = "Sem categoria",
    val contentType: IptvContentType = IptvContentType.LIVE,
    val userAgent: String? = null,
    val referrer: String? = null,
) {
    val key: String
        get() = "$name|$streamUrl"

    val requestHeaders: Map<String, String>
        get() = buildMap {
            userAgent?.takeIf { it.isNotBlank() }?.let { put("User-Agent", it) }
            referrer?.takeIf { it.isNotBlank() }?.let { put("Referer", it) }
        }
}
