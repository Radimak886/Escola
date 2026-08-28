package com.radimak.tv.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.radimak.tv.model.ProviderInfo
import com.radimak.tv.model.ProviderAccess
import com.radimak.tv.ui.AppUiState
import com.radimak.tv.ui.components.PosterArtwork
import com.radimak.tv.ui.theme.RadimakOrange
import com.radimak.tv.ui.theme.RadimakSurface
import com.radimak.tv.ui.theme.RadimakSurfaceHigh
import com.radimak.tv.ui.theme.RadimakTextMuted
import java.util.Locale

@Composable
fun DetailsScreen(
    state: AppUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenWatchOptions: (String) -> Unit,
    onPlayLocal: (Uri, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = state.selectedItem
    if (item == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Título não encontrado")
        }
        return
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { onPlayLocal(it, item.title) }
    }
    val isFavorite = item.favoriteKey in state.favorites

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth().height(315.dp).background(Color(item.accentColor)),
            ) {
                item.backdropUrl?.let { backdrop ->
                    AsyncImage(
                        model = backdrop,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color(0x44000000), Color(0xCC09090B), Color(0xFF09090B))),
                    ),
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color(0x99000000), CircleShape),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
                Row(
                    modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    PosterArtwork(item, Modifier.width(112.dp).height(168.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                        Text(item.mediaType.label.uppercase(), color = RadimakOrange, fontWeight = FontWeight.Bold)
                        Text(
                            item.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(7.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(String.format(Locale.getDefault(), "%.1f", item.rating), fontWeight = FontWeight.Bold)
                            Text("  •  ${item.year}", color = RadimakTextMuted)
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { state.availability.link?.let(onOpenWatchOptions) },
                        enabled = state.availability.link != null,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RadimakOrange),
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Onde assistir", maxLines = 1)
                    }
                    OutlinedButton(onClick = onToggleFavorite) {
                        Icon(if (isFavorite) Icons.Default.Check else Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (isFavorite) "Na lista" else "Minha lista")
                    }
                }

                OutlinedButton(
                    onClick = { filePicker.launch(arrayOf("video/*", "application/x-mpegURL")) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("Reproduzir um arquivo meu")
                }

                if (item.genres.isNotEmpty()) {
                    Text(item.genres.joinToString("  •  "), color = RadimakTextMuted, fontWeight = FontWeight.SemiBold)
                }

                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("Sinopse", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(item.overview, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFE4E4E7))
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Disponibilidade no Brasil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    when {
                        state.isLoadingAvailability -> CircularProgressIndicator(color = RadimakOrange, modifier = Modifier.size(28.dp))
                        state.isDemoMode -> Text(
                            "Ative o catálogo TMDB no Perfil para consultar as plataformas deste título.",
                            color = RadimakTextMuted,
                        )
                        state.availability.providers.isEmpty() -> Text(
                            "Nenhuma opção gratuita ou por assinatura foi informada para este título.",
                            color = RadimakTextMuted,
                        )
                        else -> {
                            ProviderRow(state.availability.providers, onOpenWatchOptions)
                            if (state.availability.providers.any { it.officialUrl != null }) {
                                Text(
                                    "Toque no provedor para abrir o app ou site oficial.",
                                    color = RadimakTextMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }

                Surface(color = RadimakSurface, shape = RoundedCornerShape(15.dp)) {
                    Text(
                        "Plex, ViX, Pluto TV e os serviços por assinatura controlam anúncios, sessão e DRM em seus players oficiais. O player interno é destinado aos seus próprios arquivos e conteúdos autorizados.",
                        modifier = Modifier.padding(14.dp),
                        color = RadimakTextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderRow(providers: List<ProviderInfo>, onOpenProvider: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(providers, key = { it.id }) { provider ->
            Surface(
                modifier = Modifier.clickable(
                    enabled = provider.officialUrl != null,
                    onClick = { provider.officialUrl?.let(onOpenProvider) },
                ),
                color = when (provider.access) {
                    ProviderAccess.FREE, ProviderAccess.FREE_WITH_ADS -> Color(0xFF153522)
                    ProviderAccess.SUBSCRIPTION -> Color(0xFF25311D)
                    ProviderAccess.OTHER -> RadimakSurfaceHigh
                },
                shape = RoundedCornerShape(14.dp),
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    provider.logoUrl?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = provider.name,
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(9.dp)),
                        )
                        Spacer(Modifier.width(9.dp))
                    }
                    Column {
                        Text(provider.name, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(
                            provider.access.label,
                            color = if (provider.access != ProviderAccess.OTHER) Color(0xFF86EFAC) else RadimakTextMuted,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}
