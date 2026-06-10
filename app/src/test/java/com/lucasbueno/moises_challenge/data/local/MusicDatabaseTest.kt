package com.lucasbueno.moises_challenge.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lucasbueno.moises_challenge.data.local.entity.RecentlyPlayedEntity
import com.lucasbueno.moises_challenge.data.local.entity.SearchQueryEntity
import com.lucasbueno.moises_challenge.data.local.entity.SearchResultEntity
import com.lucasbueno.moises_challenge.data.local.entity.SongEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MusicDatabaseTest {
    private lateinit var database: MusicDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MusicDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `observeSearchResults emits songs in stored search position order`() = runTest {
        database.songDao().upsertSongs(
            listOf(
                songEntity(id = 1L, name = "First"),
                songEntity(id = 2L, name = "Second"),
            ),
        )
        database.searchDao().replaceSearchResults(
            searchQuery = searchQuery(query = "daft punk", nextOffset = 2),
            searchResults = listOf(
                SearchResultEntity(query = "daft punk", songId = 2L, position = 0),
                SearchResultEntity(query = "daft punk", songId = 1L, position = 1),
            ),
        )

        val results = database.searchDao().observeSearchResults("daft punk").first()

        assertEquals(listOf("Second", "First"), results.map { it.name })
    }

    @Test
    fun `appendSearchResults keeps existing results and updates pagination metadata`() = runTest {
        database.songDao().upsertSongs(
            listOf(
                songEntity(id = 1L, name = "First"),
                songEntity(id = 2L, name = "Second"),
            ),
        )
        database.searchDao().replaceSearchResults(
            searchQuery = searchQuery(query = "radiohead", nextOffset = 1),
            searchResults = listOf(
                SearchResultEntity(query = "radiohead", songId = 1L, position = 0),
            ),
        )
        database.searchDao().appendSearchResults(
            searchQuery = searchQuery(query = "radiohead", nextOffset = 2, reachedEnd = true),
            searchResults = listOf(
                SearchResultEntity(query = "radiohead", songId = 2L, position = 1),
            ),
        )

        val query = database.searchDao().getSearchQuery("radiohead")
        val count = database.searchDao().getSearchResultCount("radiohead")
        val results = database.searchDao().observeSearchResults("radiohead").first()

        assertEquals(2, query?.nextOffset)
        assertEquals(true, query?.reachedEnd)
        assertEquals(2, count)
        assertEquals(listOf(1L, 2L), results.map { it.id })
    }

    @Test
    fun `observeRecentlyPlayedSongs emits songs ordered by latest play time`() = runTest {
        database.songDao().upsertSongs(
            listOf(
                songEntity(id = 1L, name = "Older"),
                songEntity(id = 2L, name = "Latest"),
                songEntity(id = 3L, name = "Too old"),
            ),
        )
        database.songDao().upsertRecentlyPlayed(
            RecentlyPlayedEntity(songId = 1L, playedAtMillis = 2_000L),
        )
        database.songDao().upsertRecentlyPlayed(
            RecentlyPlayedEntity(songId = 2L, playedAtMillis = 3_000L),
        )
        database.songDao().upsertRecentlyPlayed(
            RecentlyPlayedEntity(songId = 3L, playedAtMillis = 1_000L),
        )

        val results = database.songDao().observeRecentlyPlayedSongs(limit = 2).first()

        assertEquals(listOf("Latest", "Older"), results.map { it.name })
    }

    @Test
    fun `observeSongsByAlbum emits cached songs for album`() = runTest {
        database.songDao().upsertSongs(
            listOf(
                songEntity(id = 1L, name = "Album song", albumId = 10L),
                songEntity(id = 2L, name = "Other album song", albumId = 20L),
            ),
        )

        val results = database.songDao().observeSongsByAlbum(albumId = 10L).first()

        assertEquals(listOf("Album song"), results.map { it.name })
    }

    private fun searchQuery(
        query: String,
        nextOffset: Int,
        reachedEnd: Boolean = false,
    ): SearchQueryEntity {
        return SearchQueryEntity(
            query = query,
            nextOffset = nextOffset,
            reachedEnd = reachedEnd,
            updatedAtMillis = 1_000L,
        )
    }

    private fun songEntity(
        id: Long,
        name: String,
        albumId: Long? = null,
    ): SongEntity {
        return SongEntity(
            id = id,
            name = name,
            artistName = "Artist",
            albumId = albumId,
            albumName = albumId?.let { "Album $it" },
            artworkUrl = null,
            previewUrl = null,
            durationMillis = null,
            genre = null,
            releaseDate = null,
        )
    }
}
