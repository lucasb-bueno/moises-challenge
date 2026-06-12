package com.lucasbueno.moises_challenge.presentation.feature.album

import androidx.lifecycle.SavedStateHandle
import com.lucasbueno.moises_challenge.MainDispatcherRule
import com.lucasbueno.moises_challenge.domain.model.Album
import com.lucasbueno.moises_challenge.domain.model.Song
import com.lucasbueno.moises_challenge.domain.repository.MusicRepository
import com.lucasbueno.moises_challenge.presentation.common.ScreenState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `album flow is exposed in ui state and refreshes album`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = mockk<MusicRepository>()
            val album = album(id = 100L)

            every { repository.getAlbumFlow(albumId = 100L) } returns MutableStateFlow(album)
            coEvery { repository.refreshAlbum(albumId = 100L) } returns Result.success(Unit)

            val viewModel = AlbumViewModel(
                savedStateHandle = SavedStateHandle(mapOf(AlbumViewModel.ALBUM_ID_KEY to 100L)),
                repository = repository,
            )
            advanceUntilIdle()

            assertEquals(ScreenState.Show, viewModel.uiState.value.screenState)
            assertEquals(album, viewModel.uiState.value.album)
            assertFalse(viewModel.uiState.value.isRefreshing)
            coVerify { repository.refreshAlbum(albumId = 100L) }
        }

    @Test
    fun `refresh album failure marks album state as error without clearing cached album`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = mockk<MusicRepository>()
            val album = album(id = 100L)

            every { repository.getAlbumFlow(albumId = 100L) } returns MutableStateFlow(album)
            coEvery {
                repository.refreshAlbum(albumId = 100L)
            } returns Result.failure(IllegalStateException("album failed"))

            val viewModel = AlbumViewModel(
                savedStateHandle = SavedStateHandle(mapOf(AlbumViewModel.ALBUM_ID_KEY to 100L)),
                repository = repository,
            )
            advanceUntilIdle()

            assertEquals(ScreenState.Error("album failed"), viewModel.uiState.value.screenState)
            assertEquals(album, viewModel.uiState.value.album)
            assertFalse(viewModel.uiState.value.isRefreshing)
        }

    private fun album(id: Long): Album {
        return Album(
            id = id,
            name = "Album",
            artistName = "Artist",
            artworkUrl = "https://example.com/artwork.jpg",
            songs = listOf(song(id = 1L, albumId = id)),
        )
    }

    private fun song(id: Long, albumId: Long): Song {
        return Song(
            id = id,
            name = "Song $id",
            artistName = "Artist",
            albumId = albumId,
            albumName = "Album",
            artworkUrl = "https://example.com/artwork.jpg",
            previewUrl = "https://example.com/preview.m4a",
            durationMillis = 30_000L,
            genre = "Pop",
            releaseDate = "2024-01-01T00:00:00Z",
        )
    }
}
