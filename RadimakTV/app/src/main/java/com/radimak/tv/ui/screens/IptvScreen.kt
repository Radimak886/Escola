package com.radimak.tv.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.radimak.tv.model.IptvContentType
import com.radimak.tv.model.IptvItem
import com.radimak.tv.model.IptvServer
import com.radimak.tv.ui.AppUiState
import com.radimak.tv.ui.components.RadimakLogo
import com.radimak.tv.ui.theme.RadimakOrange
import com.radimak.tv.ui.theme.RadimakSurface
import com.radimak.tv.ui.theme.RadimakSurfaceHigh
import com.radimak.tv.ui.theme.RadimakTextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun IptvCatalogScreen(
    state: AppUiState,
    contentType: IptvContentType,
    onLoadUrl: (String) -> Unit,
    onImportFile: (Uri) -> Unit,
    onSelectServer: (String) -> Unit,
    onRefresh: () -> Unit,
    onClear: () -> Unit,
    onPlay: (IptvItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember(contentType) { mutableStateOf("") }
    var selectedGroup by remember(contentType, state.selectedIptvServerId) { mutableStateOf<String?>(null) }
    var filteredItems by remember(contentType) { mutableStateOf<List<IptvItem>>(emptyList()) }
    var isFiltering by remember(contentType) { mutableStateOf(true) }
    var showSourceSheet by remember { mutableStateOf(false) }

    val groups = remember(state.iptvItems, contentType) {
        state.iptvItems.asSequence()
            .filter { it.contentType == contentType }
            .map { it.group }
            .distinct()
            .sorted()
            .toList()
    }

    LaunchedEffect(state.iptvItems, contentType, query, selectedGroup) {
        isFiltering = true
        if (query.isNotBlank()) delay(180)
        val search = query.trim()
        filteredItems = withContext(Dispatchers.Default) {
            state.iptvItems.filter { item ->
                item.contentType == contentType &&
                    (selectedGroup == null || item.group == selectedGroup) &&
                    (search.isBlank() || item.name.contains(search, ignoreCase = true) ||
                        item.group.contains(search, ignoreCase = true))
            }
        }
        isFiltering = false
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            CatalogHeader(
                title = contentType.screenTitle,
                subtitle = listOf(state.selectedIptvServerLabel, contentType.screenSubtitle)
                    .filter { it.isNotBlank() }
                    .joinToString(" • "),
                isLoading = state.isLoadingIptv,
                onSettings = { showSourceSheet = true },
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            IptvServerSelector(
                servers = state.iptvServers,
                selectedId = state.selectedIptvServerId,
                enabled = !state.isLoadingIptv,
                onSelect = { serverId ->
                    if (serverId == "server_2_radimak" && !state.hasPrivateIptvSource) {
                        showSourceSheet = true
                    } else {
                        onSelectServer(serverId)
                    }
                },
            )
        }

        if (groups.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                IptvGroupSelector(
                    groups = groups,
                    selectedGroup = selectedGroup,
                    onSelect = { selectedGroup = it },
                )
            }
        }

        if (state.iptvItems.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(contentType.searchHint) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (query.isBlank()) "${filteredItems.size} itens" else "${filteredItems.size} resultados",
                        color = RadimakTextMuted,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f),
                    )
                    if (isFiltering) {
                        CircularProgressIndicator(
                            color = RadimakOrange,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        when {
            state.isLoadingIptv && state.iptvItems.isEmpty() -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LoadingState()
                }
            }

            !state.hasIptvSource -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyCatalog(
                        title = "Adicione sua lista M3U",
                        description = "A lista será organizada automaticamente em TV, Filmes e Séries.",
                        actionLabel = "Configurar lista",
                        onAction = { showSourceSheet = true },
                    )
                }
            }

            state.iptvItems.isEmpty() -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyCatalog(
                        title = "A lista está vazia",
                        description = "Tente atualizar ou alterne entre os servidores disponíveis.",
                        actionLabel = "Tentar novamente",
                        onAction = onRefresh,
                    )
                }
            }

            filteredItems.isEmpty() && !isFiltering -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyCatalog(
                        title = if (query.isBlank()) "Nenhum item nesta seção" else "Nada encontrado",
                        description = if (query.isBlank()) {
                            "A lista não possui conteúdo classificado como ${contentType.label.lowercase()}."
                        } else {
                            "Tente outro nome ou grupo."
                        },
                        actionLabel = null,
                        onAction = null,
                    )
                }
            }
        }

        items(filteredItems, key = IptvItem::key) { item ->
            IptvSquareCard(item = item, onPlay = { onPlay(item) })
        }
    }

    if (showSourceSheet) {
        IptvSourceSheet(
            state = state,
            onDismiss = { showSourceSheet = false },
            onLoadUrl = onLoadUrl,
            onImportFile = onImportFile,
            onSelectServer = onSelectServer,
            onRefresh = onRefresh,
            onClear = {
                onClear()
                showSourceSheet = false
            },
        )
    }
}

@Composable
private fun IptvServerSelector(
    servers: List<IptvServer>,
    selectedId: String?,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        servers.forEach { server ->
            FilterChip(
                selected = server.id == selectedId,
                onClick = { onSelect(server.id) },
                enabled = enabled,
                label = { Text(server.label) },
            )
        }
    }
}

@Composable
private fun IptvGroupSelector(
    groups: List<String>,
    selectedGroup: String?,
    onSelect: (String?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedGroup == null,
            onClick = { onSelect(null) },
            label = { Text("Todos") },
        )
        groups.forEach { group ->
            FilterChip(
                selected = group == selectedGroup,
                onClick = { onSelect(group) },
                label = { Text(group) },
            )
        }
    }
}

@Composable
private fun CatalogHeader(
    title: String,
    subtitle: String,
    isLoading: Boolean,
    onSettings: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadimakLogo()
            Spacer(Modifier.weight(1f))
            Surface(color = RadimakSurface, shape = CircleShape) {
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Ajustes da lista M3U")
                }
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, color = RadimakTextMuted, style = MaterialTheme.typography.bodyMedium)
            }
            if (isLoading) {
                CircularProgressIndicator(color = RadimakOrange, strokeWidth = 3.dp, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun IptvSquareCard(item: IptvItem, onPlay: () -> Unit) {
    Surface(
        onClick = onPlay,
        color = RadimakSurfaceHigh,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.logoUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(RadimakSurfaceHigh, Color(0xFF301A10))),
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = item.contentType.placeholderIcon,
                        contentDescription = null,
                        tint = RadimakOrange,
                        modifier = Modifier.size(48.dp),
                    )
                }
            } else {
                AsyncImage(
                    model = item.logoUrl,
                    contentDescription = item.name,
                    contentScale = if (item.contentType == IptvContentType.LIVE) ContentScale.Fit else ContentScale.Crop,
                    modifier = if (item.contentType == IptvContentType.LIVE) {
                        Modifier.fillMaxSize().padding(22.dp)
                    } else {
                        Modifier.fillMaxSize()
                    },
                )
            }
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.25f to Color.Transparent,
                            0.68f to Color(0x55000000),
                            1f to Color(0xF2000000),
                        ),
                    ),
                ),
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    item.group,
                    color = Color(0xFFD4D4D8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 46.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(color = RadimakOrange, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(12.dp))
        Text("Carregando e organizando a lista…", color = RadimakTextMuted)
    }
}

@Composable
private fun EmptyCatalog(
    title: String,
    description: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    Surface(color = RadimakSurface, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(description, color = RadimakTextMuted, style = MaterialTheme.typography.bodyMedium)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = RadimakOrange),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IptvSourceSheet(
    state: AppUiState,
    onDismiss: () -> Unit,
    onLoadUrl: (String) -> Unit,
    onImportFile: (Uri) -> Unit,
    onSelectServer: (String) -> Unit,
    onRefresh: () -> Unit,
    onClear: () -> Unit,
) {
    var urlDraft by remember { mutableStateOf("") }
    var showUrl by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            onImportFile(it)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = RadimakSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Fontes da Radimak TV", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            if (state.hasBundledIptvSource) {
                Text(
                    "Escolha um servidor. O catálogo será atualizado automaticamente, sem precisar colar links.",
                    color = RadimakTextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                state.iptvServers.filter { it.url.isNotBlank() }.forEach { server ->
                    val selected = server.id == state.selectedIptvServerId
                    Button(
                        onClick = {
                            onSelectServer(server.id)
                            onDismiss()
                        },
                        enabled = !state.isLoadingIptv,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) RadimakOrange else RadimakSurfaceHigh,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(server.label, fontWeight = FontWeight.Bold)
                            Text(server.description, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                OutlinedButton(
                    onClick = {
                        onRefresh()
                        onDismiss()
                    },
                    enabled = !state.isLoadingIptv,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("Atualizar ${state.selectedIptvServerLabel.ifBlank { "servidor" }}")
                }
                Text(
                    "As fontes são públicas e não usam usuário ou senha.",
                    color = RadimakTextMuted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            HorizontalDivider(color = Color(0xFF313136), modifier = Modifier.padding(vertical = 2.dp))
            Text("Servidor 2 — Radimak TV", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Para testar uma fonte particular, informe a URL somente neste aparelho. Ela não é incluída no APK.",
                color = RadimakTextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
                OutlinedTextField(
                value = urlDraft,
                onValueChange = { urlDraft = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("URL M3U") },
                placeholder = { Text("https://servidor/lista.m3u") },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showUrl = !showUrl }) {
                        Icon(
                            if (showUrl) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showUrl) "Ocultar URL" else "Mostrar URL",
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (showUrl) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = RoundedCornerShape(16.dp),
            )
            Button(
                onClick = {
                    onLoadUrl(urlDraft.trim())
                    urlDraft = ""
                    showUrl = false
                    onDismiss()
                },
                enabled = urlDraft.isNotBlank() && !state.isLoadingIptv,
                colors = ButtonDefaults.buttonColors(containerColor = RadimakOrange),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Link, contentDescription = null)
                Spacer(Modifier.width(7.dp))
                Text("Carregar URL")
            }
            OutlinedButton(
                onClick = {
                    filePicker.launch(
                        arrayOf(
                            "audio/x-mpegurl",
                            "application/vnd.apple.mpegurl",
                            "text/plain",
                            "application/octet-stream",
                        ),
                    )
                },
                enabled = !state.isLoadingIptv,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(Modifier.width(7.dp))
                Text("Importar arquivo M3U")
            }
            if (state.hasIptvSource) {
                HorizontalDivider(color = Color(0xFF313136), modifier = Modifier.padding(vertical = 2.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onRefresh()
                            onDismiss()
                        },
                        enabled = !state.isLoadingIptv,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Atualizar")
                    }
                    OutlinedButton(
                        onClick = onClear,
                        enabled = !state.isLoadingIptv,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Remover")
                    }
                }
            }
                Text(
                    "Prefira HTTPS. Em endereços HTTP, a lista e as credenciais trafegam sem criptografia.",
                    color = RadimakTextMuted,
                    style = MaterialTheme.typography.labelSmall,
                )
        }
    }
}

private val IptvContentType.screenTitle: String
    get() = when (this) {
        IptvContentType.LIVE -> "TV ao vivo"
        IptvContentType.MOVIE -> "Filmes"
        IptvContentType.SERIES -> "Séries"
    }

private val IptvContentType.screenSubtitle: String
    get() = when (this) {
        IptvContentType.LIVE -> "Canais da sua lista M3U"
        IptvContentType.MOVIE -> "Filmes da sua lista M3U"
        IptvContentType.SERIES -> "Episódios da sua lista M3U"
    }

private val IptvContentType.searchHint: String
    get() = when (this) {
        IptvContentType.LIVE -> "Buscar canal ou grupo"
        IptvContentType.MOVIE -> "Buscar filme ou grupo"
        IptvContentType.SERIES -> "Buscar série, episódio ou grupo"
    }

private val IptvContentType.placeholderIcon: ImageVector
    get() = when (this) {
        IptvContentType.LIVE -> Icons.Default.LiveTv
        IptvContentType.MOVIE -> Icons.Default.Movie
        IptvContentType.SERIES -> Icons.Default.VideoLibrary
    }
