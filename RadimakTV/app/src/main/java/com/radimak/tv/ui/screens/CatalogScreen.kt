package com.radimak.tv.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.radimak.tv.model.CatalogItem
import com.radimak.tv.ui.components.MediaPosterCard
import com.radimak.tv.ui.components.RadimakLogo
import com.radimak.tv.ui.theme.RadimakOrange
import com.radimak.tv.ui.theme.RadimakTextMuted

@Composable
fun CatalogScreen(
    title: String,
    subtitle: String,
    items: List<CatalogItem>,
    isLoading: Boolean,
    onSelect: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            RadimakLogo()
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 18.dp),
            )
            Text(subtitle, color = RadimakTextMuted, style = MaterialTheme.typography.bodyMedium)
        }

        if (isLoading && items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RadimakOrange)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(132.dp),
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(items, key = { "${it.mediaType.apiValue}-${it.id}" }) { item ->
                    MediaPosterCard(item = item, onClick = { onSelect(item) }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
