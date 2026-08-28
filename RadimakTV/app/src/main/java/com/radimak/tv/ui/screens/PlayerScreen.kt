package com.radimak.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

@Composable
@androidx.annotation.OptIn(UnstableApi::class)
fun PlayerScreen(
    uri: String?,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    requestHeaders: Map<String, String> = emptyMap(),
) {
    BackHandler(onBack = onBack)
    if (uri.isNullOrBlank()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhuma mídia foi selecionada.")
        }
        return
    }

    val context = LocalContext.current
    var playbackError by remember(uri) { mutableStateOf<String?>(null) }
    val player = remember(uri, requestHeaders) {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(20_000)
            .setReadTimeoutMs(30_000)
            .setUserAgent("RadimakTV/0.6.0")
            .setDefaultRequestProperties(requestHeaders)
        val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                playWhenReady = true
            }
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                playbackError = error.toUserMessage()
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { PlayerView(it).apply { this.player = player; keepScreenOn = true } },
            update = { it.player = player },
            modifier = Modifier.fillMaxSize(),
        )
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp).background(Color(0x88000000), androidx.compose.foundation.shape.CircleShape),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
        }
        Text(
            title,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 22.dp),
        )
        playbackError?.let { message ->
            Surface(
                color = Color(0xE61E1E22),
                modifier = Modifier.align(Alignment.BottomCenter).padding(18.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Não foi possível abrir este canal", color = Color.White)
                    Spacer(Modifier.height(6.dp))
                    Text(message, color = Color(0xFFD4D4D8))
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            playbackError = null
                            player.prepare()
                            player.playWhenReady = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Tentar novamente")
                    }
                }
            }
        }
    }
}

private fun PlaybackException.toUserMessage(): String = when (errorCode) {
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
    -> "O servidor não respondeu. Ele pode estar fora do ar ou bloqueado para esta região."

    PlaybackException.ERROR_CODE_DRM_UNSPECIFIED,
    PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED,
    PlaybackException.ERROR_CODE_DRM_CONTENT_ERROR
    -> "Este canal exige DRM ou o aplicativo oficial da emissora."

    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
    PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED
    -> "O formato ou codec deste canal não é compatível com este aparelho."

    else -> "Tente novamente ou alterne para o outro servidor."
}
