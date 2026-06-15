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
    private var lastNextPageRequest: NextPageRequest? = null

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
        lastNextPageRequest = null

        if (query.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    searchResultsState = ScreenState.Show,
                    searchResults = emptyList(),
                    hasReachedSearchEnd = false,
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
        val currentSearchResultCount = uiState.value.searchResults.size
        val request = NextPageRequest(
            query = query,
            searchResultCount = currentSearchResultCount,
        )

        if (
            query.isBlank() ||
            currentSearchResultCount == 0 ||
            uiState.value.isLoadingNextPage ||
            uiState.value.hasReachedSearchEnd ||
            request == lastNextPageRequest
        ) {
            return
        }

        lastNextPageRequest = request

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
                    hasReachedSearchEnd = result.getOrNull()?.reachedEnd ?: state.hasReachedSearchEnd,
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
                    hasReachedSearchEnd = false,
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
                    state.copy(
                        searchResultsState = ScreenState.Show,
                        hasReachedSearchEnd = result.getOrThrow().reachedEnd,
                    )
                }
            }
        }
    }

    private companion object {
        const val SEARCH_PAGE_SIZE = 20
        const val RECENTLY_PLAYED_LIMIT = 10
    }

    private data class NextPageRequest(
        val query: String,
        val searchResultCount: Int,
    )
}
