package com.lucasbueno.moises_challenge.data.repository

import app.cash.turbine.test
import com.lucasbueno.moises_challenge.data.local.CachedSearchMetadata
import com.lucasbueno.moises_challenge.data.local.MusicLocalDataSource
import com.lucasbueno.moises_challenge.data.remote.MusicRemoteDataSource
import com.lucasbueno.moises_challenge.domain.model.Song
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MusicRepositoryImplTest {
    private lateinit var localDataSource: MusicLocalDataSource
    private lateinit var remoteDataSource: MusicRemoteDataSource
    private lateinit var repository: MusicRepositoryImpl

    @Before
    fun setUp() {
        localDataSource = mockk()
        remoteDataSource = mockk()
        repository = MusicRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
        )
    }

    @Test
    fun `getSearchResultsFlow uses normalized query from local cache`() = runTest {
        val songs = listOf(song(id = 1L))

        every { localDataSource.getSearchResultsFlow("beatles") } returns flowOf(songs)

        repository.getSearchResultsFlow(" beatles ").test {
            assertEquals(songs, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `refreshSearch fetches first page and replaces cached results`() = runTest {
        val songs = listOf(song(id = 1L), song(id = 2L))

        coEvery {
            remoteDataSource.searchSongs(query = "daft punk", offset = 0, limit = 20)
        } returns songs
        coJustRun {
            localDataSource.replaceSearchResults(
                query = "daft punk",
                songs = songs,
                nextOffset = 2,
                reachedEnd = true,
                updatedAtMillis = any(),
            )
        }

        val result = repository.refreshSearch(query = " daft punk ", limit = 20)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().nextOffset)
        assertTrue(result.getOrThrow().reachedEnd)
        coVerify {
            localDataSource.replaceSearchResults(
                query = "daft punk",
                songs = songs,
                nextOffset = 2,
                reachedEnd = true,
                updatedAtMillis = any(),
            )
        }
    }

    @Test
    fun `refreshSearch returns failure when remote search fails`() = runTest {
        val exception = IllegalStateException("network failed")

        coEvery {
            remoteDataSource.searchSongs(query = "radiohead", offset = 0, limit = 20)
        } throws exception

        val result = repository.refreshSearch(query = "radiohead", limit = 20)

        assertTrue(result.isFailure)
        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 0) {
            localDataSource.replaceSearchResults(
                query = any(),
                songs = any(),
                nextOffset = any(),
                reachedEnd = any(),
                updatedAtMillis = any(),
            )
        }
    }

    @Test
    fun `loadNextSearchPage uses cached next offset and appends results`() = runTest {
        val songs = listOf(song(id = 21L), song(id = 22L), song(id = 23L))

        coEvery {
            localDataSource.getSearchMetadata("queen")
        } returns CachedSearchMetadata(nextOffset = 20, reachedEnd = false)
        coEvery {
            remoteDataSource.searchSongs(query = "queen", offset = 20, limit = 20)
        } returns songs
        coJustRun {
            localDataSource.appendSearchResults(
                query = "queen",
                songs = songs,
                nextOffset = 23,
                reachedEnd = true,
                updatedAtMillis = any(),
            )
        }

        val result = repository.loadNextSearchPage(query = "queen", limit = 20)

        assertTrue(result.isSuccess)
        assertEquals(23, result.getOrThrow().nextOffset)
        assertTrue(result.getOrThrow().reachedEnd)
        coVerify {
            localDataSource.appendSearchResults(
                query = "queen",
                songs = songs,
                nextOffset = 23,
                reachedEnd = true,
                updatedAtMillis = any(),
            )
        }
    }

    @Test
    fun `loadNextSearchPage starts at first page when metadata is missing`() = runTest {
        val songs = List(size = 20) { index -> song(id = index.toLong()) }

        coEvery { localDataSource.getSearchMetadata("phoenix") } returns null
        coEvery {
            remoteDataSource.searchSongs(query = "phoenix", offset = 0, limit = 20)
        } returns songs
        coJustRun {
            localDataSource.appendSearchResults(
                query = "phoenix",
                songs = songs,
                nextOffset = 20,
                reachedEnd = false,
                updatedAtMillis = any(),
            )
        }

        val result = repository.loadNextSearchPage(query = "phoenix", limit = 20)

        assertTrue(result.isSuccess)
        assertEquals(20, result.getOrThrow().nextOffset)
        assertFalse(result.getOrThrow().reachedEnd)
        coVerify {
            remoteDataSource.searchSongs(query = "phoenix", offset = 0, limit = 20)
        }
        coVerify {
            localDataSource.appendSearchResults(
                query = "phoenix",
                songs = songs,
                nextOffset = 20,
                reachedEnd = false,
                updatedAtMillis = any(),
            )
        }
    }

    @Test
    fun `loadNextSearchPage does nothing when cached metadata reached end`() = runTest {
        coEvery {
            localDataSource.getSearchMetadata("nirvana")
        } returns CachedSearchMetadata(nextOffset = 40, reachedEnd = true)

        val result = repository.loadNextSearchPage(query = "nirvana", limit = 20)

        assertTrue(result.isSuccess)
        assertEquals(40, result.getOrThrow().nextOffset)
        assertTrue(result.getOrThrow().reachedEnd)
        coVerify(exactly = 0) {
            remoteDataSource.searchSongs(query = any(), offset = any(), limit = any())
        }
        coVerify(exactly = 0) {
            localDataSource.appendSearchResults(
                query = any(),
                songs = any(),
                nextOffset = any(),
                reachedEnd = any(),
                updatedAtMillis = any(),
            )
        }
    }

    @Test
    fun `markAsRecentlyPlayed stores played timestamp in local cache`() = runTest {
        coJustRun {
            localDataSource.markAsRecentlyPlayed(songId = 10L, playedAtMillis = any())
        }

        val result = repository.markAsRecentlyPlayed(songId = 10L)

        assertTrue(result.isSuccess)
        coVerify {
            localDataSource.markAsRecentlyPlayed(songId = 10L, playedAtMillis = any())
        }
    }

    @Test
    fun `refreshAlbum fetches album songs and caches them`() = runTest {
        val songs = listOf(song(id = 1L, albumId = 100L), song(id = 2L, albumId = 100L))

        coEvery { remoteDataSource.lookupAlbumSongs(albumId = 100L) } returns songs
        coJustRun { localDataSource.cacheSongs(songs) }

        val result = repository.refreshAlbum(albumId = 100L)

        assertTrue(result.isSuccess)
        coVerify { localDataSource.cacheSongs(songs) }
    }

    @Test
    fun `refreshAlbum returns failure when remote lookup fails`() = runTest {
        val exception = IllegalStateException("lookup failed")

        coEvery { remoteDataSource.lookupAlbumSongs(albumId = 100L) } throws exception

        val result = repository.refreshAlbum(albumId = 100L)

        assertFalse(result.isSuccess)
        assertSame(exception, result.exceptionOrNull())
        coVerify(exactly = 0) { localDataSource.cacheSongs(any()) }
    }

    private fun song(
        id: Long,
        albumId: Long? = 100L,
    ): Song {
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
