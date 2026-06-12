package com.lucasbueno.moises_challenge.presentation.feature.player

import androidx.lifecycle.SavedStateHandle
import com.lucasbueno.moises_challenge.MainDispatcherRule
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
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `song flow is exposed in ui state`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = mockk<MusicRepository>()
        val song = song(id = 1L)

        every { repository.getSongFlow(songId = 1L) } returns MutableStateFlow(song)

        val viewModel = SongDetailsViewModel(
            savedStateHandle = SavedStateHandle(mapOf(SongDetailsViewModel.SONG_ID_KEY to 1L)),
            repository = repository,
        )
        advanceUntilIdle()

        assertEquals(ScreenState.Show, viewModel.uiState.value.screenState)
        assertEquals(song, viewModel.uiState.value.song)
    }

    @Test
    fun `null song marks screen as error`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = mockk<MusicRepository>()

        every { repository.getSongFlow(songId = 1L) } returns MutableStateFlow(null)

        val viewModel = SongDetailsViewModel(
            savedStateHandle = SavedStateHandle(mapOf(SongDetailsViewModel.SONG_ID_KEY to 1L)),
            repository = repository,
        )
        advanceUntilIdle()

        assertEquals(ScreenState.Error("Song not found"), viewModel.uiState.value.screenState)
        assertNull(viewModel.uiState.value.song)
    }

    @Test
    fun `playback started marks song as recently played`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = mockk<MusicRepository>()

        every { repository.getSongFlow(songId = 1L) } returns MutableStateFlow(song(id = 1L))
        coEvery { repository.markAsRecentlyPlayed(songId = 1L) } returns Result.success(Unit)

        val viewModel = SongDetailsViewModel(
            savedStateHandle = SavedStateHandle(mapOf(SongDetailsViewModel.SONG_ID_KEY to 1L)),
            repository = repository,
        )

        viewModel.onPlaybackStarted()
        advanceUntilIdle()

        assertEquals(ScreenState.Show, viewModel.uiState.value.screenState)
        coVerify { repository.markAsRecentlyPlayed(songId = 1L) }
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
