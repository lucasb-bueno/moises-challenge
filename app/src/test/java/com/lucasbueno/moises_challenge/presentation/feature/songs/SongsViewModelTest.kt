package com.lucasbueno.moises_challenge.presentation.feature.songs

import com.lucasbueno.moises_challenge.MainDispatcherRule
import com.lucasbueno.moises_challenge.domain.model.SearchPagination
import com.lucasbueno.moises_challenge.domain.model.Song
import com.lucasbueno.moises_challenge.domain.repository.MusicRepository
import com.lucasbueno.moises_challenge.presentation.common.ScreenState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: MusicRepository
    private lateinit var recentlyPlayedSongs: MutableStateFlow<List<Song>>

    @Before
    fun setUp() {
        repository = mockk()
        recentlyPlayedSongs = MutableStateFlow(emptyList())
        every { repository.getRecentlyPlayedSongsFlow(limit = 10) } returns recentlyPlayedSongs
    }

    @Test
    fun `recently played songs are exposed in ui state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = SongsViewModel(repository)
        val songs = listOf(song(id = 1L), song(id = 2L))

        recentlyPlayedSongs.value = songs
        advanceUntilIdle()

        assertEquals(ScreenState.Show, viewModel.uiState.value.recentlyPlayedState)
        assertEquals(songs, viewModel.uiState.value.recentlyPlayedSongs)
    }

    @Test
    fun `search refresh uses trimmed query and keeps data outside screen state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val searchResults = MutableStateFlow(listOf(song(id = 10L)))
            every { repository.getSearchResultsFlow("radiohead") } returns searchResults
            coEvery {
                repository.refreshSearch(query = "radiohead", limit = 20)
            } returns Result.success(SearchPagination(nextOffset = 20, reachedEnd = false))

            val viewModel = SongsViewModel(repository)

            viewModel.onQueryChanged(" radiohead ")
            viewModel.onSearch()
            advanceUntilIdle()

            assertEquals(" radiohead ", viewModel.uiState.value.query)
            assertEquals(ScreenState.Show, viewModel.uiState.value.searchResultsState)
            assertEquals(searchResults.value, viewModel.uiState.value.searchResults)
            assertFalse(viewModel.uiState.value.hasReachedSearchEnd)
            coVerify { repository.refreshSearch(query = "radiohead", limit = 20) }
        }

    @Test
    fun `search refresh exposes reached end in ui state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { repository.getSearchResultsFlow("prince") } returns MutableStateFlow(listOf(song(id = 30L)))
            coEvery {
                repository.refreshSearch(query = "prince", limit = 20)
            } returns Result.success(SearchPagination(nextOffset = 1, reachedEnd = true))

            val viewModel = SongsViewModel(repository)

            viewModel.onQueryChanged("prince")
            viewModel.onSearch()
            advanceUntilIdle()

            assertEquals(ScreenState.Show, viewModel.uiState.value.searchResultsState)
            assertEquals(true, viewModel.uiState.value.hasReachedSearchEnd)
        }

    @Test
    fun `search refresh failure updates search state without clearing cached data`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val cachedSongs = listOf(song(id = 20L))
            every { repository.getSearchResultsFlow("phoenix") } returns MutableStateFlow(cachedSongs)
            coEvery {
                repository.refreshSearch(query = "phoenix", limit = 20)
            } returns Result.failure(IllegalStateException("network failed"))

            val viewModel = SongsViewModel(repository)

            viewModel.onQueryChanged("phoenix")
            viewModel.onSearch()
            advanceUntilIdle()

            assertEquals(ScreenState.Error("network failed"), viewModel.uiState.value.searchResultsState)
            assertEquals(cachedSongs, viewModel.uiState.value.searchResults)
        }

    @Test
    fun `empty local search results do not replace retry error with blank content`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val searchResults = MutableSharedFlow<List<Song>>()
            every { repository.getSearchResultsFlow("phoenix") } returns searchResults
            coEvery {
                repository.refreshSearch(query = "phoenix", limit = 20)
            } returns Result.failure(IllegalStateException("network failed"))

            val viewModel = SongsViewModel(repository)

            viewModel.onQueryChanged("phoenix")
            viewModel.onSearch()
            advanceUntilIdle()
            searchResults.emit(emptyList())
            runCurrent()

            assertEquals(ScreenState.Error("network failed"), viewModel.uiState.value.searchResultsState)
            assertEquals(emptyList<Song>(), viewModel.uiState.value.searchResults)
        }

    @Test
    fun `empty local search results keep retry loading while refresh is running`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val searchResults = MutableStateFlow(emptyList<Song>())
            every { repository.getSearchResultsFlow("phoenix") } returns searchResults
            coEvery {
                repository.refreshSearch(query = "phoenix", limit = 20)
            } coAnswers {
                delay(1_000)
                Result.failure(IllegalStateException("network failed"))
            }

            val viewModel = SongsViewModel(repository)

            viewModel.onQueryChanged("phoenix")
            viewModel.onSearch()
            runCurrent()

            assertEquals(ScreenState.Loading, viewModel.uiState.value.searchResultsState)
            assertEquals(emptyList<Song>(), viewModel.uiState.value.searchResults)
        }

    @Test
    fun `load next page failure stops loading and marks search component as error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { repository.getSearchResultsFlow("queen") } returns MutableStateFlow(
                listOf(song(id = 1L), song(id = 2L)),
            )
            coEvery {
                repository.refreshSearch(query = "queen", limit = 20)
            } returns Result.success(SearchPagination(nextOffset = 20, reachedEnd = false))
            coEvery {
                repository.loadNextSearchPage(query = "queen", limit = 20)
            } returns Result.failure(IllegalStateException("next page failed"))

            val viewModel = SongsViewModel(repository)

            viewModel.onQueryChanged("queen")
            viewModel.onSearch()
            advanceUntilIdle()
            viewModel.onLoadNextPage()
            advanceUntilIdle()

            assertEquals(ScreenState.Error("next page failed"), viewModel.uiState.value.searchResultsState)
            assertFalse(viewModel.uiState.value.isLoadingNextPage)
        }

    @Test
    fun `load next page is called once for the same query and unchanged result count`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val searchResults = MutableStateFlow(listOf(song(id = 1L), song(id = 2L)))
            every { repository.getSearchResultsFlow("queen") } returns searchResults
            coEvery {
                repository.refreshSearch(query = "queen", limit = 20)
            } returns Result.success(SearchPagination(nextOffset = 2, reachedEnd = false))
            coEvery {
                repository.loadNextSearchPage(query = "queen", limit = 20)
            } returns Result.failure(IllegalStateException("next page failed"))

            val viewModel = SongsViewModel(repository)

            viewModel.onQueryChanged("queen")
            viewModel.onSearch()
            advanceUntilIdle()
            viewModel.onLoadNextPage()
            advanceUntilIdle()
            viewModel.onLoadNextPage()
            advanceUntilIdle()

            coVerify(exactly = 1) {
                repository.loadNextSearchPage(query = "queen", limit = 20)
            }
        }

    @Test
    fun `load next page is skipped after search reaches end`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { repository.getSearchResultsFlow("queen") } returns MutableStateFlow(emptyList())
            coEvery {
                repository.refreshSearch(query = "queen", limit = 20)
            } returns Result.success(SearchPagination(nextOffset = 1, reachedEnd = true))

            val viewModel = SongsViewModel(repository)

            viewModel.onQueryChanged("queen")
            viewModel.onSearch()
            advanceUntilIdle()
            viewModel.onLoadNextPage()
            advanceUntilIdle()

            assertEquals(true, viewModel.uiState.value.hasReachedSearchEnd)
            coVerify(exactly = 0) {
                repository.loadNextSearchPage(query = "queen", limit = 20)
            }
        }

    private fun song(id: Long): Song {
        return Song(
            id = id,
            name = "Song $id",
            artistName = "Artist",
            albumId = 100L,
            albumName = "Album",
            artworkUrl = "https://example.com/artwork.jpg",
            previewUrl = "https://example.com/preview.m4a",
            durationMillis = 30_000L,
            genre = "Pop",
            releaseDate = "2024-01-01T00:00:00Z",
        )
    }
}
