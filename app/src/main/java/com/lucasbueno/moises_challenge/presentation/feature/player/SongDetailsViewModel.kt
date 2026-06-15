package com.lucasbueno.moises_challenge.presentation.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucasbueno.moises_challenge.domain.repository.MusicRepository
import com.lucasbueno.moises_challenge.presentation.common.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SongDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MusicRepository,
) : ViewModel() {
    private val songId = checkNotNull(savedStateHandle.get<Long>(SONG_ID_KEY))

    private val _uiState = MutableStateFlow(SongDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private var songJob: Job? = null

    init {
        collectSong()
    }

    fun onRetry() {
        songJob?.cancel()
        _uiState.update { state ->
            state.copy(screenState = ScreenState.Loading)
        }
        collectSong()
    }

    fun onPlaybackStarted() {
        viewModelScope.launch {
            val result = repository.markAsRecentlyPlayed(songId)

            result.exceptionOrNull()?.let { error ->
                _uiState.update { state ->
                    state.copy(
                        screenState = ScreenState.Error(error.message),
                    )
                }
            }
        }
    }

    private fun collectSong() {
        songJob = viewModelScope.launch {
            repository.getSongFlow(songId)
                .catch { error ->
                    _uiState.update { state ->
                        state.copy(
                            screenState = ScreenState.Error(error.message),
                        )
                    }
                }
                .collect { song ->
                    _uiState.update { state ->
                        if (song == null) {
                            state.copy(
                                screenState = ScreenState.Error("Song not found"),
                                song = null,
                            )
                        } else {
                            state.copy(
                                screenState = ScreenState.Show,
                                song = song,
                            )
                        }
                    }
                }
        }
    }

    companion object {
        const val SONG_ID_KEY = "songId"
    }
}
