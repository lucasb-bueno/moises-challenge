package com.lucasbueno.moises_challenge.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lucasbueno.moises_challenge.presentation.feature.album.AlbumScreen
import com.lucasbueno.moises_challenge.presentation.feature.album.AlbumViewModel
import com.lucasbueno.moises_challenge.presentation.feature.player.SongDetailsScreen
import com.lucasbueno.moises_challenge.presentation.feature.player.SongDetailsViewModel
import com.lucasbueno.moises_challenge.presentation.feature.splash.SplashScreen
import com.lucasbueno.moises_challenge.presentation.feature.songs.SongsScreen
import com.lucasbueno.moises_challenge.presentation.feature.songs.SongsViewModel
import kotlinx.coroutines.delay

@Composable
fun MusicNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Any = SplashRoute,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<SplashRoute> {
            LaunchedEffect(Unit) {
                delay(SPLASH_DURATION_MILLIS)
                navController.navigate(SongsRoute) {
                    popUpTo<SplashRoute> {
                        inclusive = true
                    }
                }
            }

            SplashScreen()
        }

        composable<SongsRoute> {
            val viewModel = hiltViewModel<SongsViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.query) {
                if (uiState.query.isBlank()) {
                    viewModel.onSearch()
                    return@LaunchedEffect
                }

                delay(SEARCH_DEBOUNCE_MILLIS)
                viewModel.onSearch()
            }

            SongsScreen(
                uiState = uiState,
                onQueryChanged = viewModel::onQueryChanged,
                onSongClick = { songId ->
                    navController.navigate(SongDetailsRoute(songId))
                },
                onAlbumClick = { albumId ->
                    navController.navigate(AlbumRoute(albumId))
                },
                onLoadNextPage = viewModel::onLoadNextPage,
                onRetryClick = viewModel::onSearch,
            )
        }

        composable<SongDetailsRoute> {
            val viewModel = hiltViewModel<SongDetailsViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.song?.id) {
                if (uiState.song != null) {
                    viewModel.onPlaybackStarted()
                }
            }

            SongDetailsScreen(
                uiState = uiState,
                onBackClick = navController::popBackStack,
                onAlbumClick = { albumId ->
                    navController.navigate(AlbumRoute(albumId))
                },
                onRetryClick = viewModel::onRetry,
            )
        }

        composable<AlbumRoute> {
            val viewModel = hiltViewModel<AlbumViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            AlbumScreen(
                uiState = uiState,
                onBackClick = navController::popBackStack,
                onSongClick = { songId ->
                    navController.navigate(SongDetailsRoute(songId))
                },
                onRetryClick = viewModel::onRefreshAlbum,
            )
        }
    }
}

private const val SPLASH_DURATION_MILLIS = 1_200L
private const val SEARCH_DEBOUNCE_MILLIS = 350L
