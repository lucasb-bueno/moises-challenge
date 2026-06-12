package com.lucasbueno.moises_challenge.presentation.feature.player

import com.lucasbueno.moises_challenge.domain.model.Song
import com.lucasbueno.moises_challenge.presentation.common.ScreenState

data class SongDetailsUiState(
    val screenState: ScreenState = ScreenState.Loading,
    val song: Song? = null,
)
