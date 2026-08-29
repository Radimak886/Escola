package com.radimak.tv.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.radimak.tv.data.CatalogRepository
import com.radimak.tv.data.DemoCatalog
import com.radimak.tv.data.IptvRepository
import com.radimak.tv.data.TmdbClient
import com.radimak.tv.data.UserPreferences
import com.radimak.tv.model.CatalogItem
import com.radimak.tv.model.IptvServer
import com.radimak.tv.model.IptvItem
import com.radimak.tv.model.StreamingService
import com.radimak.tv.model.WatchAvailability
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val trending: List<CatalogItem> = DemoCatalog.all.take(10),
    val movies: List<CatalogItem> = DemoCatalog.movies,
    val series: List<CatalogItem> = DemoCatalog.series,
    val freeWithAds: List<CatalogItem> = emptyList(),
    val iptvItems: List<IptvItem> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<CatalogItem> = emptyList(),
    val selectedItem: CatalogItem? = null,
    val availability: WatchAvailability = WatchAvailability(),
    val services: List<StreamingService> = UserPreferences.defaultServices,
    val favorites: Set<String> = emptySet(),
    val tmdbToken: String = "",
    val isDemoMode: Boolean = true,
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val isLoadingAvailability: Boolean = false,
    val isLoadingIptv: Boolean = false,
    val hasIptvSource: Boolean = false,
    val hasBundledIptvSource: Boolean = false,
    val hasPrivateIptvSource: Boolean = false,
    val iptvServers: List<IptvServer> = emptyList(),
    val selectedIptvServerId: String? = null,
    val selectedIptvServerLabel: String = "",
    val playbackUri: String? = null,
    val playbackTitle: String = "Minha mídia",
    val playbackHeaders: Map<String, String> = emptyMap(),
    val message: String? = null,
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = UserPreferences(application)
    private val bundledSourceChanged = preferences.applyBundledIptvSourceIfNeeded()
    private val client = TmdbClient { preferences.tmdbToken }
    private val repository = CatalogRepository(client)
    private val iptvRepository = IptvRepository(application)
    private val _uiState = MutableStateFlow(
        AppUiState(
            services = preferences.services(),
            favorites = preferences.favoriteKeys(),
            tmdbToken = preferences.tmdbToken,
            isDemoMode = preferences.tmdbToken.isBlank(),
            hasIptvSource = preferences.iptvSource.isNotBlank(),
            hasBundledIptvSource = preferences.hasBundledIptvSource,
            hasPrivateIptvSource = preferences.hasPrivateIptvSource,
            iptvServers = preferences.availableIptvServers,
            selectedIptvServerId = if (preferences.usesBundledIptvSource) preferences.selectedBundledIptvServer?.id else UserPreferences.PRIVATE_SERVER_ID,
            selectedIptvServerLabel = if (preferences.usesBundledIptvSource) preferences.selectedBundledIptvServer?.label.orEmpty() else preferences.privateIptvServer.label,
            isLoadingIptv = preferences.iptvSource.isNotBlank(),
        ),
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var availabilityJob: Job? = null
    private var iptvJob: Job? = null

    init {
        if (bundledSourceChanged) iptvRepository.clearCache()
        loadCachedIptvThenRefresh()
    }

    fun refreshCatalog() {
        if (preferences.tmdbToken.isBlank()) {
            useDemoCatalog("Adicione sua chave gratuita do TMDB no Perfil para ativar o catálogo completo.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            runCatching { repository.loadCatalog(_uiState.value.services) }
                .onSuccess { bundle ->
                    _uiState.update {
                        it.copy(
                            trending = bundle.trending.ifEmpty { bundle.movies + bundle.series },
                            movies = bundle.movies,
                            series = bundle.series,
                            freeWithAds = bundle.freeWithAds,
                            isDemoMode = false,
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    useDemoCatalog("Não foi possível carregar o TMDB: ${error.userMessage()}")
                }
        }
    }

    fun saveTmdbToken(value: String) {
        preferences.tmdbToken = value
        _uiState.update {
            it.copy(
                tmdbToken = preferences.tmdbToken,
                isDemoMode = preferences.tmdbToken.isBlank(),
                message = if (preferences.tmdbToken.isBlank()) "Catálogo de demonstração ativado." else "Chave salva. Atualizando o catálogo…",
            )
        }
        refreshCatalog()
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            val normalized = query.trim()
            if (preferences.tmdbToken.isBlank()) {
                val results = DemoCatalog.all.filter {
                    it.title.contains(normalized, ignoreCase = true) ||
                        it.overview.contains(normalized, ignoreCase = true)
                }
                _uiState.update { it.copy(searchResults = results, isSearching = false) }
                return@launch
            }

            _uiState.update { it.copy(isSearching = true) }
            runCatching { repository.search(normalized) }
                .onSuccess { results -> _uiState.update { it.copy(searchResults = results, isSearching = false) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isSearching = false, message = "Erro na busca: ${error.userMessage()}") }
                }
        }
    }

    fun selectItem(item: CatalogItem) {
        _uiState.update { it.copy(selectedItem = item, availability = WatchAvailability()) }
        loadAvailability(item)
    }

    private fun loadAvailability(item: CatalogItem) {
        availabilityJob?.cancel()
        if (preferences.tmdbToken.isBlank() || item.id < 0) return
        availabilityJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAvailability = true) }
            runCatching { repository.providers(item, _uiState.value.services) }
                .onSuccess { availability ->
                    _uiState.update { it.copy(availability = availability, isLoadingAvailability = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingAvailability = false,
                            message = "Não foi possível consultar onde assistir: ${error.userMessage()}",
                        )
                    }
                }
        }
    }

    fun toggleFavorite(item: CatalogItem) {
        _uiState.update { it.copy(favorites = preferences.toggleFavorite(item.favoriteKey)) }
    }

    fun toggleService(id: String, enabled: Boolean) {
        preferences.setServiceEnabled(id, enabled)
        val services = preferences.services()
        _uiState.update { it.copy(services = services) }
        _uiState.value.selectedItem?.let(::loadAvailability)
        if (preferences.tmdbToken.isNotBlank()) refreshCatalog()
    }

    fun setPlayback(uri: Uri, title: String = "Minha mídia") {
        runCatching {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        _uiState.update {
            it.copy(playbackUri = uri.toString(), playbackTitle = title, playbackHeaders = emptyMap())
        }
    }

    fun configureIptvUrl(value: String) {
        val url = value.trim()
        if (!url.startsWith("https://", true) && !url.startsWith("http://", true)) {
            _uiState.update { it.copy(message = "Informe um endereço M3U iniciado por HTTP ou HTTPS.") }
            return
        }
        val newSource = "url:$url"
        val sourceChanged = preferences.iptvSource != newSource
        if (sourceChanged) iptvRepository.clearCache()
        preferences.iptvSource = newSource
        preferences.usesBundledIptvSource = false
        preferences.privateIptvSource = newSource
        _uiState.update {
            it.copy(
                iptvItems = if (sourceChanged) emptyList() else it.iptvItems,
                hasIptvSource = true,
                hasPrivateIptvSource = true,
                iptvServers = preferences.availableIptvServers,
                selectedIptvServerId = UserPreferences.PRIVATE_SERVER_ID,
                selectedIptvServerLabel = preferences.privateIptvServer.label,
            )
        }
        refreshIptv()
    }

    fun configureIptvFile(uri: Uri) {
        runCatching {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        val newSource = "file:$uri"
        val sourceChanged = preferences.iptvSource != newSource
        if (sourceChanged) iptvRepository.clearCache()
        preferences.iptvSource = newSource
        preferences.usesBundledIptvSource = false
        preferences.privateIptvSource = newSource
        _uiState.update {
            it.copy(
                iptvItems = if (sourceChanged) emptyList() else it.iptvItems,
                hasIptvSource = true,
                hasPrivateIptvSource = true,
                iptvServers = preferences.availableIptvServers,
                selectedIptvServerId = UserPreferences.PRIVATE_SERVER_ID,
                selectedIptvServerLabel = preferences.privateIptvServer.label,
            )
        }
        refreshIptv()
    }

    fun selectIptvServer(serverId: String) {
        if (serverId == UserPreferences.PRIVATE_SERVER_ID) {
            if (!preferences.hasPrivateIptvSource) {
                _uiState.update { it.copy(message = "Configure o Servidor 2 — Radimak TV primeiro.") }
                return
            }
            val changed = preferences.selectPrivateIptvServer()
            _uiState.update {
                it.copy(
                    iptvItems = if (changed) emptyList() else it.iptvItems,
                    hasIptvSource = true,
                    hasPrivateIptvSource = true,
                    iptvServers = preferences.availableIptvServers,
                    selectedIptvServerId = UserPreferences.PRIVATE_SERVER_ID,
                    selectedIptvServerLabel = preferences.privateIptvServer.label,
                    isLoadingIptv = true,
                    message = null,
                )
            }
            if (changed) loadCachedIptvThenRefresh() else refreshIptv()
            return
        }
        val changed = preferences.selectBundledIptvServer(serverId)
        val selectedServer = preferences.selectedBundledIptvServer ?: return
        _uiState.update {
            it.copy(
                iptvItems = if (changed) emptyList() else it.iptvItems,
                hasIptvSource = true,
                hasBundledIptvSource = true,
                iptvServers = preferences.availableIptvServers,
                selectedIptvServerId = selectedServer.id,
                selectedIptvServerLabel = selectedServer.label,
                isLoadingIptv = true,
                message = null,
            )
        }
        if (changed) loadCachedIptvThenRefresh() else refreshIptv()
    }

    fun refreshIptv() {
        val source = preferences.iptvSource
        if (source.isBlank()) {
            _uiState.update { it.copy(message = "Adicione uma URL ou arquivo M3U primeiro.") }
            return
        }
        iptvJob?.cancel()
        iptvJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingIptv = true, message = null) }
            runCatching {
                when {
                    source.startsWith("url:") -> iptvRepository.loadUrl(source.removePrefix("url:"))
                    source.startsWith("file:") -> iptvRepository.loadFile(Uri.parse(source.removePrefix("file:")))
                    else -> error("Origem M3U desconhecida.")
                }
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        iptvItems = result.items,
                        isLoadingIptv = false,
                        hasIptvSource = true,
                        message = if (result.isPartial) {
                            "${it.selectedIptvServerLabel.ifBlank { "Lista" }}: conexão interrompida, mas ${result.items.size} itens foram recuperados."
                        } else {
                            "${it.selectedIptvServerLabel.ifBlank { "Lista" }} carregado: ${result.items.size} itens disponíveis."
                        },
                    )
                }
            }.onFailure { error ->
                if (error is CancellationException) return@onFailure
                _uiState.update {
                    val hasSavedCatalog = it.iptvItems.isNotEmpty()
                    it.copy(
                        isLoadingIptv = false,
                        message = if (hasSavedCatalog) {
                            "${IptvRepository.safeMessage(error)} Exibindo a última lista salva."
                        } else {
                            IptvRepository.safeMessage(error)
                        },
                    )
                }
            }
        }
    }

    fun clearIptv() {
        iptvJob?.cancel()
        if (!preferences.usesBundledIptvSource) preferences.privateIptvSource = ""
        preferences.iptvSource = ""
        iptvRepository.clearCache()
        _uiState.update {
            it.copy(
                iptvItems = emptyList(),
                isLoadingIptv = false,
                hasIptvSource = false,
                selectedIptvServerId = null,
                selectedIptvServerLabel = "",
                message = "Lista M3U removida deste aparelho.",
            )
        }
    }

    fun setIptvPlayback(item: IptvItem) {
        _uiState.update {
            it.copy(
                playbackUri = item.streamUrl,
                playbackTitle = item.name,
                playbackHeaders = item.requestHeaders,
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun loadCachedIptvThenRefresh() {
        val source = preferences.iptvSource
        if (source.isBlank()) return
        viewModelScope.launch {
            val cachedItems = runCatching { iptvRepository.loadCached(source) }.getOrDefault(emptyList())
            if (source == preferences.iptvSource && cachedItems.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        iptvItems = cachedItems,
                        hasIptvSource = true,
                        isLoadingIptv = true,
                    )
                }
            }
            if (source == preferences.iptvSource) refreshIptv()
        }
    }

    private fun useDemoCatalog(message: String) {
        _uiState.update {
            it.copy(
                trending = DemoCatalog.all.take(10),
                movies = DemoCatalog.movies,
                series = DemoCatalog.series,
                freeWithAds = emptyList(),
                isDemoMode = true,
                isLoading = false,
                message = message,
            )
        }
    }

    private fun Throwable.userMessage(): String = message?.take(140) ?: "erro desconhecido"
}
