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
            songCacheMaxSize = 10,
            searchCacheMaxQueries = 10,
        )

        val results = dataSource.getSearchResultsFlow("daft punk").first()
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
            songCacheMaxSize = 10,
            searchCacheMaxQueries = 10,
        )
        dataSource.appendSearchResults(
            query = "radiohead",
            songs = listOf(song(id = 2L, name = "Second")),
            nextOffset = 2,
            reachedEnd = true,
            updatedAtMillis = 2_000L,
            songCacheMaxSize = 10,
            searchCacheMaxQueries = 10,
        )

        val results = dataSource.getSearchResultsFlow("radiohead").first()
        val metadata = dataSource.getSearchMetadata("radiohead")

        assertEquals(listOf("First", "Second"), results.map { it.name })
        assertEquals(CachedSearchMetadata(nextOffset = 2, reachedEnd = true), metadata)
    }

    @Test
    fun `getAlbumFlow builds album from cached songs`() = runTest {
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
            accessedAtMillis = 1_000L,
            songCacheMaxSize = 10,
        )

        val album = dataSource.getAlbumFlow(albumId = 10L).first()

        assertEquals(10L, album?.id)
        assertEquals("Album name", album?.name)
        assertEquals("Artist", album?.artistName)
        assertEquals("https://example.com/art.jpg", album?.artworkUrl)
        assertEquals(listOf("Album song"), album?.songs?.map { it.name })
    }

    @Test
    fun `getAlbumFlow emits null when album has no cached songs`() = runTest {
        val album = dataSource.getAlbumFlow(albumId = 10L).first()

        assertNull(album)
    }

    @Test
    fun `markAsRecentlyPlayed exposes latest songs first`() = runTest {
        dataSource.cacheSongs(
            listOf(
                song(id = 1L, name = "Older"),
                song(id = 2L, name = "Latest"),
            ),
            accessedAtMillis = 1_000L,
            songCacheMaxSize = 10,
        )
        markAsRecentlyPlayed(songId = 1L, playedAtMillis = 1_000L, recentlyPlayedMaxSize = 10)
        markAsRecentlyPlayed(songId = 2L, playedAtMillis = 2_000L, recentlyPlayedMaxSize = 10)

        val results = dataSource.getRecentlyPlayedSongsFlow(limit = 2).first()

        assertEquals(listOf("Latest", "Older"), results.map { it.name })
    }

    @Test
    fun `markAsRecentlyPlayed prunes oldest songs when cache is full`() = runTest {
        dataSource.cacheSongs(
            listOf(
                song(id = 1L, name = "Oldest"),
                song(id = 2L, name = "Middle"),
                song(id = 3L, name = "Latest"),
            ),
            accessedAtMillis = 1_000L,
            songCacheMaxSize = 10,
        )

        markAsRecentlyPlayed(songId = 1L, playedAtMillis = 1_000L, recentlyPlayedMaxSize = 2)
        markAsRecentlyPlayed(songId = 2L, playedAtMillis = 2_000L, recentlyPlayedMaxSize = 2)
        markAsRecentlyPlayed(songId = 3L, playedAtMillis = 3_000L, recentlyPlayedMaxSize = 2)

        val results = dataSource.getRecentlyPlayedSongsFlow(limit = 10).first()

        assertEquals(listOf("Latest", "Middle"), results.map { it.name })
    }

    @Test
    fun `markAsRecentlyPlayed refreshes replayed song before pruning`() = runTest {
        dataSource.cacheSongs(
            listOf(
                song(id = 1L, name = "Replayed"),
                song(id = 2L, name = "Oldest"),
                song(id = 3L, name = "Latest"),
            ),
            accessedAtMillis = 1_000L,
            songCacheMaxSize = 10,
        )

        markAsRecentlyPlayed(songId = 1L, playedAtMillis = 1_000L, recentlyPlayedMaxSize = 3)
        markAsRecentlyPlayed(songId = 2L, playedAtMillis = 2_000L, recentlyPlayedMaxSize = 3)
        markAsRecentlyPlayed(songId = 3L, playedAtMillis = 3_000L, recentlyPlayedMaxSize = 3)
        markAsRecentlyPlayed(songId = 1L, playedAtMillis = 4_000L, recentlyPlayedMaxSize = 2)

        val results = dataSource.getRecentlyPlayedSongsFlow(limit = 10).first()

        assertEquals(listOf("Replayed", "Latest"), results.map { it.name })
    }

    @Test
    fun `recycleRecentlyPlayedCache removes expired songs`() = runTest {
        dataSource.cacheSongs(
            listOf(
                song(id = 1L, name = "Expired"),
                song(id = 2L, name = "Fresh"),
            ),
            accessedAtMillis = 1_000L,
            songCacheMaxSize = 10,
        )
        markAsRecentlyPlayed(songId = 1L, playedAtMillis = 1_000L, recentlyPlayedMaxSize = 10)
        markAsRecentlyPlayed(songId = 2L, playedAtMillis = 3_000L, recentlyPlayedMaxSize = 10)

        recycleRecentlyPlayedCache(
            recentlyPlayedExpiresBeforeMillis = 2_000L,
            recentlyPlayedMaxSize = 10,
        )

        val results = dataSource.getRecentlyPlayedSongsFlow(limit = 10).first()

        assertEquals(listOf("Fresh"), results.map { it.name })
    }

    @Test
    fun `recycleRecentlyPlayedCache prunes overflow with LRU order`() = runTest {
        dataSource.cacheSongs(
            listOf(
                song(id = 1L, name = "Oldest"),
                song(id = 2L, name = "Middle"),
                song(id = 3L, name = "Latest"),
            ),
            accessedAtMillis = 1_000L,
            songCacheMaxSize = 10,
        )
        markAsRecentlyPlayed(songId = 1L, playedAtMillis = 1_000L, recentlyPlayedMaxSize = 10)
        markAsRecentlyPlayed(songId = 2L, playedAtMillis = 2_000L, recentlyPlayedMaxSize = 10)
        markAsRecentlyPlayed(songId = 3L, playedAtMillis = 3_000L, recentlyPlayedMaxSize = 10)

        recycleRecentlyPlayedCache(
            recentlyPlayedExpiresBeforeMillis = 0L,
            recentlyPlayedMaxSize = 2,
        )

        val results = dataSource.getRecentlyPlayedSongsFlow(limit = 10).first()

        assertEquals(listOf("Latest", "Middle"), results.map { it.name })
    }

    @Test
    fun `cacheSongs prunes oldest shared song cache entries`() = runTest {
        dataSource.cacheSongs(
            songs = listOf(song(id = 1L, name = "Old")),
            accessedAtMillis = 1_000L,
            songCacheMaxSize = 10,
        )
        dataSource.cacheSongs(
            songs = listOf(song(id = 2L, name = "Fresh")),
            accessedAtMillis = 2_000L,
            songCacheMaxSize = 1,
        )

        val oldSong = dataSource.getSongFlow(songId = 1L).first()
        val freshSong = dataSource.getSongFlow(songId = 2L).first()

        assertNull(oldSong)
        assertEquals("Fresh", freshSong?.name)
    }

    @Test
    fun `replaceSearchResults prunes oldest search queries`() = runTest {
        dataSource.replaceSearchResults(
            query = "older",
            songs = listOf(song(id = 1L, name = "Older")),
            nextOffset = 1,
            reachedEnd = true,
            updatedAtMillis = 1_000L,
            songCacheMaxSize = 10,
            searchCacheMaxQueries = 10,
        )
        dataSource.replaceSearchResults(
            query = "newer",
            songs = listOf(song(id = 2L, name = "Newer")),
            nextOffset = 1,
            reachedEnd = true,
            updatedAtMillis = 2_000L,
            songCacheMaxSize = 10,
            searchCacheMaxQueries = 1,
        )

        assertNull(dataSource.getSearchMetadata("older"))
        assertEquals(
            CachedSearchMetadata(nextOffset = 1, reachedEnd = true),
            dataSource.getSearchMetadata("newer"),
        )
        assertEquals(emptyList<Song>(), dataSource.getSearchResultsFlow("older").first())
    }

    private suspend fun markAsRecentlyPlayed(
        songId: Long,
        playedAtMillis: Long,
        recentlyPlayedMaxSize: Int,
    ) {
        dataSource.markAsRecentlyPlayed(
            songId = songId,
            playedAtMillis = playedAtMillis,
            recentlyPlayedMaxSize = recentlyPlayedMaxSize,
            songCacheMaxSize = 10,
        )
    }

    private suspend fun recycleRecentlyPlayedCache(
        recentlyPlayedExpiresBeforeMillis: Long,
        recentlyPlayedMaxSize: Int,
    ) {
        dataSource.recycleRecentlyPlayedCache(
            recentlyPlayedExpiresBeforeMillis = recentlyPlayedExpiresBeforeMillis,
            recentlyPlayedMaxSize = recentlyPlayedMaxSize,
        )
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
