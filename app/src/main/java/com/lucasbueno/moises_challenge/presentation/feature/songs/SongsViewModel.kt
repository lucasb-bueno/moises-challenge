package com.lucasbueno.moises_challenge.presentation.feature.songs

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
class SongsViewModel @Inject constructor(
    private val repository: MusicRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SongsUiState())
    val uiState = _uiState.asStateFlow()

    private var searchResultsJob: Job? = null
    private var searchRefreshJob: Job? = null

    init {
        collectRecentlyPlayedSongs()
    }

    fun onQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(query = query)
        }
    }

    fun onSearch() {
        val query = uiState.value.query.trim()

        searchResultsJob?.cancel()
        searchRefreshJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    searchResultsState = ScreenState.Show,
                    searchResults = emptyList(),
                    isLoadingNextPage = false,
                )
            }
            return
        }

        collectSearchResults(query)
        refreshSearch(query)
    }

    fun onLoadNextPage() {
        val query = uiState.value.query.trim()
        if (query.isBlank() || uiState.value.isLoadingNextPage) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoadingNextPage = true,
                )
            }

            val result = repository.loadNextSearchPage(query = query, limit = SEARCH_PAGE_SIZE)

            _uiState.update { state ->
                state.copy(
                    searchResultsState = if (result.isSuccess) {
                        ScreenState.Show
                    } else {
                        ScreenState.Error(result.exceptionOrNull()?.message)
                    },
                    isLoadingNextPage = false,
                )
            }
        }
    }

    fun onSongPlayed(songId: Long) {
        viewModelScope.launch {
            repository.markAsRecentlyPlayed(songId)
        }
    }

    private fun collectRecentlyPlayedSongs() {
        viewModelScope.launch {
            repository.getRecentlyPlayedSongsFlow(limit = RECENTLY_PLAYED_LIMIT)
                .catch { error ->
                    _uiState.update { state ->
                        state.copy(
                            recentlyPlayedState = ScreenState.Error(error.message),
                        )
                    }
                }
                .collect { songs ->
                    _uiState.update { state ->
                        state.copy(
                            recentlyPlayedState = ScreenState.Show,
                            recentlyPlayedSongs = songs,
                        )
                    }
                }
        }
    }

    private fun collectSearchResults(query: String) {
        searchResultsJob = viewModelScope.launch {
            repository.getSearchResultsFlow(query)
                .catch { error ->
                    _uiState.update { state ->
                        state.copy(
                            searchResultsState = ScreenState.Error(error.message),
                        )
                    }
                }
                .collect { songs ->
                    _uiState.update { state ->
                        state.copy(
                            searchResultsState = ScreenState.Show,
                            searchResults = songs,
                        )
                    }
                }
        }
    }

    private fun refreshSearch(query: String) {
        searchRefreshJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    searchResultsState = ScreenState.Loading,
                    isLoadingNextPage = false,
                )
            }

            val result = repository.refreshSearch(query = query, limit = SEARCH_PAGE_SIZE)

            if (uiState.value.query.trim() != query) return@launch

            if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(
                        searchResultsState = ScreenState.Error(result.exceptionOrNull()?.message),
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(searchResultsState = ScreenState.Show)
                }
            }
        }
    }

    private companion object {
        const val SEARCH_PAGE_SIZE = 20
        const val RECENTLY_PLAYED_LIMIT = 10
    }
}
