package com.radimak.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.radimak.tv.model.CatalogItem
import com.radimak.tv.model.StreamingService
import com.radimak.tv.ui.AppUiState
import com.radimak.tv.ui.components.FeaturedHero
import com.radimak.tv.ui.components.MediaRow
import com.radimak.tv.ui.components.RadimakLogo
import com.radimak.tv.ui.components.SearchShortcut
import com.radimak.tv.ui.components.ServicesRow
import com.radimak.tv.ui.theme.RadimakOrange
import com.radimak.tv.ui.theme.RadimakSurfaceHigh

@Composable
fun HomeScreen(
    state: AppUiState,
    onSearch: () -> Unit,
    onSelect: (CatalogItem) -> Unit,
    onMovies: () -> Unit,
    onSeries: () -> Unit,
    onOpenService: (StreamingService) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(12.dp))
                RadimakLogo()
                Spacer(Modifier.height(18.dp))
                SearchShortcut(onClick = onSearch)
            }
        }

        if (state.isDemoMode) {
            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF3B1C0C), Color(0xFF1D1D21))),
                            RoundedCornerShape(16.dp),
                        )
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RadimakOrange)
                        Text("Modo demonstração", fontWeight = FontWeight.Bold)
                        Text(
                            "Adicione uma chave do TMDB no Perfil para carregar o catálogo e descobrir títulos gratuitos no Brasil.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD4D4D8),
                        )
                    }
                }
            }
        }

        state.trending.firstOrNull()?.let { featured ->
            item {
                FeaturedHero(
                    item = featured,
                    onClick = { onSelect(featured) },
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Plataformas",
                    modifier = Modifier.padding(horizontal = 18.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Plex, ViX e Pluto TV abrem o player oficial.",
                    modifier = Modifier.padding(horizontal = 18.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1A1AA),
                )
                ServicesRow(state.services, onServiceClick = onOpenService)
            }
        }

        if (state.freeWithAds.isNotEmpty()) {
            item {
                MediaRow(
                    title = "Grátis com anúncios",
                    items = state.freeWithAds.take(18),
                    onItemClick = onSelect,
                )
            }
        }

        item {
            MediaRow(
                title = "Séries populares",
                items = state.series.take(12),
                onItemClick = onSelect,
                onSeeAll = onSeries,
            )
        }
        item {
            MediaRow(
                title = "Filmes em destaque",
                items = state.movies.take(12),
                onItemClick = onSelect,
                onSeeAll = onMovies,
            )
        }
        item {
            MediaRow(
                title = "Tendências da semana",
                items = state.trending.drop(1).take(12),
                onItemClick = onSelect,
            )
        }
    }
}
