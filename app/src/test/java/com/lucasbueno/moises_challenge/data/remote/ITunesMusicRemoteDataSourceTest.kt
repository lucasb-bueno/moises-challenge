package com.lucasbueno.moises_challenge.data.remote

import com.lucasbueno.moises_challenge.data.remote.api.ITunesApi
import com.lucasbueno.moises_challenge.data.remote.dto.ITunesSearchResponseDto
import com.lucasbueno.moises_challenge.data.remote.dto.ITunesSongDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ITunesMusicRemoteDataSourceTest {
    private val api = mockk<ITunesApi>()
    private val dataSource = ITunesMusicRemoteDataSource(api)

    @Test
    fun `searchSongs emulates offset by requesting accumulated limit and dropping previous results`() =
        runTest {
            coEvery {
                api.searchSongs(
                    term = "queen",
                    media = "music",
                    entity = "song",
                    limit = 40,
                )
            } returns ITunesSearchResponseDto(
                results = List(40) { index -> songDto(id = index.toLong()) },
            )

            val songs = dataSource.searchSongs(
                query = "queen",
                offset = 20,
                limit = 20,
            )

            assertEquals((20L..39L).toList(), songs.map { it.id })
        }

    @Test
    fun `searchSongs caps accumulated request limit at iTunes maximum`() =
        runTest {
            coEvery {
                api.searchSongs(
                    term = "queen",
                    media = "music",
                    entity = "song",
                    limit = 200,
                )
            } returns ITunesSearchResponseDto(
                results = List(200) { index -> songDto(id = index.toLong()) },
            )

            val songs = dataSource.searchSongs(
                query = "queen",
                offset = 190,
                limit = 20,
            )

            assertEquals((190L..199L).toList(), songs.map { it.id })
        }

    @Test
    fun `searchSongs returns empty list when offset is at iTunes maximum`() =
        runTest {
            val songs = dataSource.searchSongs(
                query = "queen",
                offset = 200,
                limit = 20,
            )

            assertEquals(emptyList<Long>(), songs.map { it.id })
            coVerify(exactly = 0) {
                api.searchSongs(
                    term = "queen",
                    media = "music",
                    entity = "song",
                    limit = 200,
                )
            }
        }

    private fun songDto(id: Long): ITunesSongDto {
        return ITunesSongDto(
            wrapperType = "track",
            kind = "song",
            trackId = id,
            artistName = "Artist",
            trackName = "Song $id",
        )
    }
}
