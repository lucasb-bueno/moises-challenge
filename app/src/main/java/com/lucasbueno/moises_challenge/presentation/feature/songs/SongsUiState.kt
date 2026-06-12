package com.lucasbueno.moises_challenge.presentation.feature.songs

import com.lucasbueno.moises_challenge.domain.model.Song
import com.lucasbueno.moises_challenge.presentation.common.ScreenState

data class SongsUiState(
    val query: String = "",
    val searchResultsState: ScreenState = ScreenState.Show,
    val searchResults: List<Song> = emptyList(),
    val recentlyPlayedState: ScreenState = ScreenState.Loading,
    val recentlyPlayedSongs: List<Song> = emptyList(),
    val isLoadingNextPage: Boolean = false,
)
