package com.lucasbueno.moises_challenge.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lucasbueno.moises_challenge.presentation.feature.album.AlbumScreen
import com.lucasbueno.moises_challenge.presentation.feature.player.SongDetailsScreen
import com.lucasbueno.moises_challenge.presentation.feature.songs.SongsScreen

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
            SongsScreen(
                onSongClick = { songId ->
                    navController.navigate(SongDetailsRoute(songId))
                },
                onAlbumClick = { albumId ->
                    navController.navigate(AlbumRoute(albumId))
                },
            )
        }

        composable<SongDetailsRoute> {
            SongDetailsScreen(
                onBackClick = navController::popBackStack,
                onAlbumClick = { albumId ->
                    navController.navigate(AlbumRoute(albumId))
                },
            )
        }

        composable<AlbumRoute> {
            AlbumScreen(
                onBackClick = navController::popBackStack,
                onSongClick = { songId ->
                    navController.navigate(SongDetailsRoute(songId))
                },
            )
        }
    }
}
