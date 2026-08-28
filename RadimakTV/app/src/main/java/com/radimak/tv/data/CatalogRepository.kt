package com.radimak.tv.data

import com.radimak.tv.model.CatalogBundle
import com.radimak.tv.model.CatalogItem
import com.radimak.tv.model.StreamingService
import com.radimak.tv.model.WatchAvailability
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CatalogRepository(private val client: TmdbClient) {
    suspend fun loadCatalog(services: List<StreamingService>): CatalogBundle = coroutineScope {
        val trending = async { client.trending() }
        val movies = async { client.popularMovies() }
        val series = async { client.popularSeries() }
        val freeWithAds = async { runCatching { client.freeWithAds(services) }.getOrDefault(emptyList()) }
        CatalogBundle(trending.await(), movies.await(), series.await(), freeWithAds.await())
    }

    suspend fun search(query: String): List<CatalogItem> = client.search(query)

    suspend fun providers(item: CatalogItem, services: List<StreamingService>): WatchAvailability =
        client.watchAvailability(item, services)
}
