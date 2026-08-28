package com.radimak.tv.data

import android.content.Context
import com.radimak.tv.BuildConfig
import com.radimak.tv.model.IptvServer
import com.radimak.tv.model.StreamingService

class UserPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("radimak_tv_preferences", Context.MODE_PRIVATE)

    var tmdbToken: String
        get() = preferences.getString(KEY_TMDB_TOKEN, "").orEmpty()
        set(value) = preferences.edit().putString(KEY_TMDB_TOKEN, value.trim()).apply()

    var iptvSource: String
        get() = preferences.getString(KEY_IPTV_SOURCE, "").orEmpty()
        set(value) = preferences.edit().putString(KEY_IPTV_SOURCE, value).apply()

    val bundledIptvServers: List<IptvServer>
        get() = listOf(
            IptvServer(
                id = "server_1",
                label = "Servidor 1",
                description = "Brasil — TV, filmes e séries",
                url = BuildConfig.BUNDLED_M3U_URL_1.trim(),
            ),
            IptvServer(
                id = "server_2",
                label = "Servidor 2",
                description = "Canais gratuitos internacionais",
                url = BuildConfig.BUNDLED_M3U_URL_2.trim(),
            ),
        ).filter { it.url.isNotBlank() }

    val hasBundledIptvSource: Boolean
        get() = bundledIptvServers.isNotEmpty()

    val selectedBundledIptvServer: IptvServer?
        get() {
            val selectedId = preferences.getString(KEY_SELECTED_BUNDLED_IPTV_SERVER, null)
            return bundledIptvServers.firstOrNull { it.id == selectedId } ?: bundledIptvServers.firstOrNull()
        }

    fun applyBundledIptvSourceIfNeeded(): Boolean {
        val server = selectedBundledIptvServer ?: return false
        val bundledVersion = BuildConfig.BUNDLED_M3U_VERSION
        val bundledSource = "url:${server.url}"
        if (
            preferences.getInt(KEY_BUNDLED_IPTV_VERSION, 0) >= bundledVersion &&
            iptvSource == bundledSource
        ) {
            return false
        }
        val sourceChanged = iptvSource != bundledSource
        preferences.edit()
            .putString(KEY_IPTV_SOURCE, bundledSource)
            .putString(KEY_SELECTED_BUNDLED_IPTV_SERVER, server.id)
            .putInt(KEY_BUNDLED_IPTV_VERSION, bundledVersion)
            .apply()
        return sourceChanged
    }

    fun selectBundledIptvServer(serverId: String): Boolean {
        val server = bundledIptvServers.firstOrNull { it.id == serverId } ?: return false
        val source = "url:${server.url}"
        val changed = iptvSource != source
        preferences.edit()
            .putString(KEY_SELECTED_BUNDLED_IPTV_SERVER, server.id)
            .putString(KEY_IPTV_SOURCE, source)
            .putInt(KEY_BUNDLED_IPTV_VERSION, BuildConfig.BUNDLED_M3U_VERSION)
            .apply()
        return changed
    }

    fun services(): List<StreamingService> = defaultServices.map { service ->
        service.copy(enabled = preferences.getBoolean("service_${service.id}", service.enabled))
    }

    fun setServiceEnabled(id: String, enabled: Boolean) {
        preferences.edit().putBoolean("service_$id", enabled).apply()
    }

    fun favoriteKeys(): Set<String> = preferences.getStringSet(KEY_FAVORITES, emptySet()).orEmpty()

    fun toggleFavorite(key: String): Set<String> {
        val updated = favoriteKeys().toMutableSet().apply {
            if (!add(key)) remove(key)
        }
        preferences.edit().putStringSet(KEY_FAVORITES, updated).apply()
        return updated
    }

    companion object {
        private const val KEY_TMDB_TOKEN = "tmdb_token"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_IPTV_SOURCE = "iptv_source"
        private const val KEY_BUNDLED_IPTV_VERSION = "bundled_iptv_version"
        private const val KEY_SELECTED_BUNDLED_IPTV_SERVER = "selected_bundled_iptv_server"

        val defaultServices = listOf(
            StreamingService("netflix", "Netflix", listOf("Netflix"), enabled = true),
            StreamingService("prime", "Prime Video", listOf("Amazon Prime Video", "Prime Video"), enabled = true),
            StreamingService("disney", "Disney+", listOf("Disney Plus", "Disney+"), enabled = true),
            StreamingService("max", "Max", listOf("Max", "HBO Max"), enabled = true),
            StreamingService("globoplay", "Globoplay", listOf("Globoplay"), enabled = true),
            StreamingService("paramount", "Paramount+", listOf("Paramount Plus", "Paramount+"), enabled = false),
            StreamingService(
                id = "plex",
                displayName = "Plex",
                matchTerms = listOf("Plex", "Plex Channel"),
                enabled = true,
                isFree = true,
                homeUrl = "https://www.plex.tv/pt-br/watch-free/",
            ),
            StreamingService(
                id = "vix",
                displayName = "ViX",
                matchTerms = listOf("ViX", "Vix Gratis", "Vix Premium"),
                enabled = true,
                isFree = true,
                homeUrl = "https://vix.com/pt-br",
            ),
            StreamingService(
                id = "pluto",
                displayName = "Pluto TV",
                matchTerms = listOf("Pluto TV", "Pluto"),
                enabled = true,
                isFree = true,
                homeUrl = "https://pluto.tv/br/",
            ),
        )
    }
}
