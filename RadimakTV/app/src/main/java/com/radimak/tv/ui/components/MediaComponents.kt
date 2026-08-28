package com.radimak.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.radimak.tv.model.CatalogItem
import com.radimak.tv.model.StreamingService
import com.radimak.tv.ui.theme.RadimakOrange
import com.radimak.tv.ui.theme.RadimakSurfaceHigh
import com.radimak.tv.ui.theme.RadimakTextMuted
import java.util.Locale

@Composable
fun RadimakLogo(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Radimak",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-1).sp,
            ),
        )
        Spacer(Modifier.width(7.dp))
        Surface(color = Color.White, shape = RoundedCornerShape(7.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("TV", color = Color(0xFF111113), fontWeight = FontWeight.Black, fontSize = 13.sp)
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = RadimakOrange,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
fun SearchShortcut(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        color = RadimakSurfaceHigh,
        shape = RoundedCornerShape(26.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = RadimakTextMuted)
            Spacer(Modifier.width(10.dp))
            Text("Buscar filmes ou séries…", color = RadimakTextMuted)
        }
    }
}

@Composable
fun FeaturedHero(item: CatalogItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(245.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(placeholderBrush(item))
            .clickable(onClick = onClick),
    ) {
        item.backdropUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0x66000000), Color(0xF0000000)),
                ),
            ),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text("DESTAQUE", color = RadimakOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(
                item.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingBadge(item.rating)
                Spacer(Modifier.width(9.dp))
                Text(
                    listOf(item.year, item.mediaType.label, item.genres.firstOrNull()).filterNotNull().joinToString(" • "),
                    color = Color(0xFFD4D4D8),
                    fontSize = 12.sp,
                )
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(5.dp))
                Text("Ver detalhes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MediaRow(
    title: String,
    items: List<CatalogItem>,
    onItemClick: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (onSeeAll != null) {
                Row(modifier = Modifier.clickable(onClick = onSeeAll), verticalAlignment = Alignment.CenterVertically) {
                    Text("Ver tudo", color = RadimakTextMuted, fontSize = 12.sp)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = RadimakTextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { "${it.mediaType.apiValue}-${it.id}" }) { item ->
                MediaPosterCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
fun MediaPosterCard(item: CatalogItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.width(132.dp).clickable(onClick = onClick)) {
        PosterArtwork(item, Modifier.fillMaxWidth().aspectRatio(2f / 3f))
        Spacer(Modifier.height(7.dp))
        Text(item.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(3.dp))
            Text(String.format(Locale.getDefault(), "%.1f", item.rating), color = RadimakTextMuted, fontSize = 11.sp)
            Text("  •  ${item.year}", color = RadimakTextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
fun PosterArtwork(item: CatalogItem, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(13.dp)).background(placeholderBrush(item)),
        contentAlignment = Alignment.Center,
    ) {
        item.posterUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } ?: Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(color = Color(0xAA000000), shape = CircleShape) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp).size(24.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                item.title,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun RatingBadge(rating: Double) {
    Surface(color = Color(0xAA000000), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(3.dp))
            Text(String.format(Locale.getDefault(), "%.1f", rating), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun ServicesRow(
    services: List<StreamingService>,
    onServiceClick: (StreamingService) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        items(services.filter { it.enabled }, key = { it.id }) { service ->
            Surface(
                modifier = Modifier.clickable(
                    enabled = service.homeUrl != null,
                    onClick = { onServiceClick(service) },
                ),
                color = if (service.isFree) Color(0xFF153522) else RadimakSurfaceHigh,
                shape = RoundedCornerShape(50),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(service.displayName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    if (service.isFree) {
                        Spacer(Modifier.width(7.dp))
                        Text("GRÁTIS", color = Color(0xFF86EFAC), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

private fun placeholderBrush(item: CatalogItem): Brush {
    val accent = Color(item.accentColor)
    return Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.45f), Color(0xFF111113)))
}
