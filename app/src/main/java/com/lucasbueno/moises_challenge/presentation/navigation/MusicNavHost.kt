package com.lucasbueno.moises_challenge.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.lucasbueno.moises_challenge.presentation.feature.album.AlbumScreen
import com.lucasbueno.moises_challenge.presentation.feature.player.SongDetailsScreen
import com.lucasbueno.moises_challenge.presentation.feature.songs.SongsScreen
import com.lucasbueno.moises_challenge.presentation.mock.PreviewMusicData

@Composable
fun MusicNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: SongsRoute = SongsRoute,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<SongsRoute> {
            var query by rememberSaveable { mutableStateOf("") }

            SongsScreen(
                uiState = PreviewMusicData.songsUiState(query),
                onQueryChanged = { query = it },
                onSongClick = { songId ->
                    navController.navigate(SongDetailsRoute(songId))
                },
                onAlbumClick = { albumId ->
                    navController.navigate(AlbumRoute(albumId))
                },
            )
        }

        composable<SongDetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<SongDetailsRoute>()

            SongDetailsScreen(
                uiState = PreviewMusicData.songDetailsUiState(route.songId),
                onBackClick = navController::popBackStack,
                onAlbumClick = { albumId ->
                    navController.navigate(AlbumRoute(albumId))
                },
                onRetryClick = {},
            )
        }

        composable<AlbumRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AlbumRoute>()

            AlbumScreen(
                uiState = PreviewMusicData.albumUiState(route.albumId),
                onBackClick = navController::popBackStack,
                onSongClick = { songId ->
                    navController.navigate(SongDetailsRoute(songId))
                },
                onRetryClick = {},
            )
        }
    }
}
