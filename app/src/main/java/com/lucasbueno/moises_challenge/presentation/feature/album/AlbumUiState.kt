package com.lucasbueno.moises_challenge.presentation.feature.album

import com.lucasbueno.moises_challenge.domain.model.Album
import com.lucasbueno.moises_challenge.presentation.common.ScreenState

data class AlbumUiState(
    val screenState: ScreenState = ScreenState.Loading,
    val album: Album? = null,
    val isRefreshing: Boolean = false,
)
