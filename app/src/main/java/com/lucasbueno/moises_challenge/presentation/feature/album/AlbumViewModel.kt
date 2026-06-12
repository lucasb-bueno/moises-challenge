package com.lucasbueno.moises_challenge.presentation.feature.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucasbueno.moises_challenge.domain.repository.MusicRepository
import com.lucasbueno.moises_challenge.presentation.common.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AlbumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MusicRepository,
) : ViewModel() {
    private val albumId = checkNotNull(savedStateHandle.get<Long>(ALBUM_ID_KEY))

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState = _uiState.asStateFlow()

    init {
        collectAlbum()
        onRefreshAlbum()
    }

    fun onRefreshAlbum() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    screenState = ScreenState.Loading,
                    isRefreshing = true,
                )
            }

            val result = repository.refreshAlbum(albumId)

            if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(
                        screenState = ScreenState.Error(result.exceptionOrNull()?.message),
                        isRefreshing = false,
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        screenState = ScreenState.Show,
                        isRefreshing = false,
                    )
                }
            }
        }
    }

    private fun collectAlbum() {
        viewModelScope.launch {
            repository.getAlbumFlow(albumId)
                .catch { error ->
                    _uiState.update { state ->
                        state.copy(
                            screenState = ScreenState.Error(error.message),
                            isRefreshing = false,
                        )
                    }
                }
                .collect { album ->
                    _uiState.update { state ->
                        if (album == null) {
                            state.copy(album = null)
                        } else {
                            state.copy(
                                screenState = ScreenState.Show,
                                album = album,
                            )
                        }
                    }
                }
        }
    }

    companion object {
        const val ALBUM_ID_KEY = "albumId"
    }
}
