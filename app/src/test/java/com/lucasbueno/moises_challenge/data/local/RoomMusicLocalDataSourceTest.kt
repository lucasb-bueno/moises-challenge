package com.lucasbueno.moises_challenge.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lucasbueno.moises_challenge.domain.model.Song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomMusicLocalDataSourceTest {
    private lateinit var database: MusicDatabase
    private lateinit var dataSource: RoomMusicLocalDataSource

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MusicDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dataSource = RoomMusicLocalDataSource(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `replaceSearchResults caches songs and exposes metadata`() = runTest {
        dataSource.replaceSearchResults(
            query = "daft punk",
            songs = listOf(
                song(id = 2L, name = "Second"),
                song(id = 1L, name = "First"),
            ),
            nextOffset = 2,
            reachedEnd = false,
            updatedAtMillis = 1_000L,
        )

        val results = dataSource.observeSearchResults("daft punk").first()
        val metadata = dataSource.getSearchMetadata("daft punk")

        assertEquals(listOf("Second", "First"), results.map { it.name })
        assertEquals(CachedSearchMetadata(nextOffset = 2, reachedEnd = false), metadata)
    }

    @Test
    fun `appendSearchResults stores new page after existing results`() = runTest {
        dataSource.replaceSearchResults(
            query = "radiohead",
            songs = listOf(song(id = 1L, name = "First")),
            nextOffset = 1,
            reachedEnd = false,
            updatedAtMillis = 1_000L,
        )
        dataSource.appendSearchResults(
            query = "radiohead",
            songs = listOf(song(id = 2L, name = "Second")),
            nextOffset = 2,
            reachedEnd = true,
            updatedAtMillis = 2_000L,
        )

        val results = dataSource.observeSearchResults("radiohead").first()
        val metadata = dataSource.getSearchMetadata("radiohead")

        assertEquals(listOf("First", "Second"), results.map { it.name })
        assertEquals(CachedSearchMetadata(nextOffset = 2, reachedEnd = true), metadata)
    }

    @Test
    fun `observeAlbum builds album from cached songs`() = runTest {
        dataSource.cacheSongs(
            listOf(
                song(
                    id = 1L,
                    name = "Album song",
                    albumId = 10L,
                    albumName = "Album name",
                    artworkUrl = "https://example.com/art.jpg",
                ),
                song(id = 2L, name = "Other album song", albumId = 20L),
            ),
        )

        val album = dataSource.observeAlbum(albumId = 10L).first()

        assertEquals(10L, album?.id)
        assertEquals("Album name", album?.name)
        assertEquals("Artist", album?.artistName)
        assertEquals("https://example.com/art.jpg", album?.artworkUrl)
        assertEquals(listOf("Album song"), album?.songs?.map { it.name })
    }

    @Test
    fun `observeAlbum emits null when album has no cached songs`() = runTest {
        val album = dataSource.observeAlbum(albumId = 10L).first()

        assertNull(album)
    }

    @Test
    fun `markAsRecentlyPlayed exposes latest songs first`() = runTest {
        dataSource.cacheSongs(
            listOf(
                song(id = 1L, name = "Older"),
                song(id = 2L, name = "Latest"),
            ),
        )
        dataSource.markAsRecentlyPlayed(songId = 1L, playedAtMillis = 1_000L)
        dataSource.markAsRecentlyPlayed(songId = 2L, playedAtMillis = 2_000L)

        val results = dataSource.observeRecentlyPlayedSongs(limit = 2).first()

        assertEquals(listOf("Latest", "Older"), results.map { it.name })
    }

    private fun song(
        id: Long,
        name: String,
        albumId: Long? = null,
        albumName: String? = albumId?.let { "Album $it" },
        artworkUrl: String? = null,
    ): Song {
        return Song(
            id = id,
            name = name,
            artistName = "Artist",
            albumId = albumId,
            albumName = albumName,
            artworkUrl = artworkUrl,
            previewUrl = null,
            durationMillis = null,
            genre = null,
            releaseDate = null,
        )
    }
}
