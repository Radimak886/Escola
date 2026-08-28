package com.radimak.tv.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.radimak.tv.model.IptvContentType
import com.radimak.tv.model.IptvItem
import com.radimak.tv.ui.screens.IptvCatalogScreen
import com.radimak.tv.ui.screens.PlayerScreen
import com.radimak.tv.ui.theme.RadimakBackground
import com.radimak.tv.ui.theme.RadimakOrange
import com.radimak.tv.ui.theme.RadimakSurface

private object Routes {
    const val TV = "iptv_tv"
    const val MOVIES = "iptv_movies"
    const val SERIES = "iptv_series"
    const val PLAYER = "player"
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val type: IptvContentType,
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.TV, "TV", Icons.Default.LiveTv, IptvContentType.LIVE),
    BottomDestination(Routes.MOVIES, "Filmes", Icons.Default.Movie, IptvContentType.MOVIE),
    BottomDestination(Routes.SERIES, "Séries", Icons.Default.VideoLibrary, IptvContentType.SERIES),
)

@Composable
fun RadimakTvApp(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomDestinations.any { it.route == currentRoute }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessage()
    }

    fun play(item: IptvItem) {
        viewModel.setIptvPlayback(item)
        navController.navigate(Routes.PLAYER)
    }

    Scaffold(
        containerColor = RadimakBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = RadimakSurface, tonalElevation = 0.dp) {
                    bottomDestinations.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RadimakOrange,
                                selectedTextColor = RadimakOrange,
                                indicatorColor = Color(0xFF3A2115),
                                unselectedIconColor = Color(0xFFA1A1AA),
                                unselectedTextColor = Color(0xFFA1A1AA),
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TV,
            modifier = Modifier.padding(innerPadding),
        ) {
            bottomDestinations.forEach { destination ->
                composable(destination.route) {
                    IptvCatalogScreen(
                        state = state,
                        contentType = destination.type,
                        onLoadUrl = viewModel::configureIptvUrl,
                        onImportFile = viewModel::configureIptvFile,
                        onSelectServer = viewModel::selectIptvServer,
                        onRefresh = viewModel::refreshIptv,
                        onClear = viewModel::clearIptv,
                        onPlay = ::play,
                    )
                }
            }
            composable(Routes.PLAYER) {
                PlayerScreen(
                    uri = state.playbackUri,
                    title = state.playbackTitle,
                    requestHeaders = state.playbackHeaders,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
