package com.radimak.tv.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.radimak.tv.ui.AppUiState
import com.radimak.tv.ui.components.RadimakLogo
import com.radimak.tv.ui.theme.RadimakOrange
import com.radimak.tv.ui.theme.RadimakSurface
import com.radimak.tv.ui.theme.RadimakSurfaceHigh
import com.radimak.tv.ui.theme.RadimakTextMuted

@Composable
fun ProfileScreen(
    state: AppUiState,
    onSaveToken: (String) -> Unit,
    onToggleService: (String, Boolean) -> Unit,
    onRefresh: () -> Unit,
    onOpenTmdb: () -> Unit,
    onPlayLocal: (Uri, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tokenDraft by remember { mutableStateOf(state.tmdbToken) }
    var showToken by remember { mutableStateOf(false) }
    LaunchedEffect(state.tmdbToken) { tokenDraft = state.tmdbToken }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { onPlayLocal(it, "Minha mídia") }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            RadimakLogo()
            Text(
                "Perfil e configurações",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 18.dp),
            )
            Text("Controle seu catálogo e suas plataformas.", color = RadimakTextMuted)
        }

        item {
            SettingsCard(title = "Minha biblioteca", icon = Icons.Default.FolderOpen) {
                Text(
                    "Escolha um vídeo armazenado no aparelho ou em um serviço de arquivos compatível.",
                    color = RadimakTextMuted,
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { filePicker.launch(arrayOf("video/*", "application/x-mpegURL")) },
                    colors = ButtonDefaults.buttonColors(containerColor = RadimakOrange),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(Modifier.size(7.dp))
                    Text("Abrir vídeo")
                }
            }
        }

        item {
            SettingsCard(title = "Catálogo TMDB", icon = Icons.Default.Key) {
                Text(
                    "Cole uma chave API v3 ou o token de leitura v4. A chave fica somente neste aparelho.",
                    color = RadimakTextMuted,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = tokenDraft,
                    onValueChange = { tokenDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Chave ou token TMDB") },
                    singleLine = true,
                    visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showToken = !showToken }) {
                            Icon(
                                if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showToken) "Ocultar" else "Mostrar",
                            )
                        }
                    },
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onSaveToken(tokenDraft) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RadimakOrange),
                    ) {
                        Text("Salvar e ativar")
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar catálogo")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenTmdb).padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Como obter uma chave gratuita", color = RadimakOrange, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = RadimakOrange, modifier = Modifier.size(18.dp))
                }
            }
        }

        item {
            SettingsCard(title = "Plataformas", icon = Icons.Default.Info) {
                Text(
                    "Ative as assinaturas que você possui e as fontes gratuitas que deseja ver. Nenhuma senha de streaming é solicitada.",
                    color = RadimakTextMuted,
                )
                Spacer(Modifier.height(8.dp))
                state.services.forEach { service ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.displayName, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (service.isFree) "Grátis com anúncios" else "Assinatura",
                                color = RadimakTextMuted,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                        Switch(
                            checked = service.enabled,
                            onCheckedChange = { onToggleService(service.id, it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = RadimakOrange),
                        )
                    }
                }
            }
        }

        item {
            SettingsCard(title = "Sobre esta versão", icon = Icons.Default.Info) {
                Text("Radimak TV 0.5.0 • Android nativo", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Este produto usa a API do TMDB, mas não é endossado nem certificado pelo TMDB. Informações de disponibilidade são fornecidas em parceria com o JustWatch.",
                    color = RadimakTextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Plex, ViX e Pluto TV são abertos nos players oficiais. O leitor M3U e o player interno são destinados a listas públicas e conteúdos próprios, licenciados ou de domínio público. A origem M3U fica somente no armazenamento privado do app e não entra no backup.",
                    color = RadimakTextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(color = RadimakSurface, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = RadimakSurfaceHigh, shape = RoundedCornerShape(10.dp)) {
                    Icon(icon, contentDescription = null, tint = RadimakOrange, modifier = Modifier.padding(8.dp).size(20.dp))
                }
                Spacer(Modifier.size(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
