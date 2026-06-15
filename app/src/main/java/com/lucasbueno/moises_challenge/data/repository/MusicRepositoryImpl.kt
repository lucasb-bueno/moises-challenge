package com.lucasbueno.moises_challenge.data.repository

import com.lucasbueno.moises_challenge.data.local.MusicLocalDataSource
import com.lucasbueno.moises_challenge.data.remote.MusicRemoteDataSource
import com.lucasbueno.moises_challenge.domain.model.Album
import com.lucasbueno.moises_challenge.domain.model.RecentlyPlayedCachePolicy
import com.lucasbueno.moises_challenge.domain.model.SearchPagination
import com.lucasbueno.moises_challenge.domain.model.Song
import com.lucasbueno.moises_challenge.domain.repository.MusicRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class MusicRepositoryImpl @Inject constructor(
    private val localDataSource: MusicLocalDataSource,
    private val remoteDataSource: MusicRemoteDataSource,
) : MusicRepository {
    override fun getSearchResultsFlow(query: String): Flow<List<Song>> {
        return localDataSource.getSearchResultsFlow(query.normalized())
    }

    override fun getRecentlyPlayedSongsFlow(limit: Int): Flow<List<Song>> {
        return localDataSource.getRecentlyPlayedSongsFlow(limit)
    }

    override fun getSongFlow(songId: Long): Flow<Song?> {
        return localDataSource.getSongFlow(songId)
    }

    override fun getAlbumFlow(albumId: Long): Flow<Album?> {
        return localDataSource.getAlbumFlow(albumId)
    }

    override suspend fun refreshSearch(query: String, limit: Int): Result<SearchPagination> {
        return runCatching {
            val normalizedQuery = query.normalized()
            val songs = remoteDataSource.searchSongs(
                query = normalizedQuery,
                offset = FIRST_PAGE_OFFSET,
                limit = limit,
            )

            val nextOffset = songs.size
            val reachedEnd = songs.reachedEnd(limit)

            localDataSource.replaceSearchResults(
                query = normalizedQuery,
                songs = songs,
                nextOffset = nextOffset,
                reachedEnd = reachedEnd,
                updatedAtMillis = currentTimeMillis(),
                songCacheMaxSize = RecentlyPlayedCachePolicy.SONG_CACHE_MAX_SIZE,
                searchCacheMaxQueries = RecentlyPlayedCachePolicy.SEARCH_CACHE_MAX_QUERIES,
            )

            SearchPagination(
                nextOffset = nextOffset,
                reachedEnd = reachedEnd,
            )
        }
    }

    override suspend fun loadNextSearchPage(query: String, limit: Int): Result<SearchPagination> {
        return runCatching {
            val normalizedQuery = query.normalized()
            val metadata = localDataSource.getSearchMetadata(normalizedQuery)

            if (metadata?.reachedEnd == true) {
                return@runCatching SearchPagination(
                    nextOffset = metadata.nextOffset,
                    reachedEnd = true,
                )
            }

            val offset = metadata?.nextOffset ?: FIRST_PAGE_OFFSET
            val songs = remoteDataSource.searchSongs(
                query = normalizedQuery,
                offset = offset,
                limit = limit,
            )

            val nextOffset = offset + songs.size
            val reachedEnd = songs.reachedEnd(limit)

            localDataSource.appendSearchResults(
                query = normalizedQuery,
                songs = songs,
                nextOffset = nextOffset,
                reachedEnd = reachedEnd,
                updatedAtMillis = currentTimeMillis(),
                songCacheMaxSize = RecentlyPlayedCachePolicy.SONG_CACHE_MAX_SIZE,
                searchCacheMaxQueries = RecentlyPlayedCachePolicy.SEARCH_CACHE_MAX_QUERIES,
            )

            SearchPagination(
                nextOffset = nextOffset,
                reachedEnd = reachedEnd,
            )
        }
    }

    override suspend fun markAsRecentlyPlayed(songId: Long): Result<Unit> {
        return runCatching {
            localDataSource.markAsRecentlyPlayed(
                songId = songId,
                playedAtMillis = currentTimeMillis(),
                recentlyPlayedMaxSize = RecentlyPlayedCachePolicy.MAX_SIZE,
                songCacheMaxSize = RecentlyPlayedCachePolicy.SONG_CACHE_MAX_SIZE,
            )
        }
    }

    override suspend fun recycleRecentlyPlayedCache(
        recentlyPlayedMaxAgeMillis: Long,
        recentlyPlayedMaxSize: Int,
    ): Result<Unit> {
        return runCatching {
            localDataSource.recycleRecentlyPlayedCache(
                recentlyPlayedExpiresBeforeMillis = currentTimeMillis() - recentlyPlayedMaxAgeMillis,
                recentlyPlayedMaxSize = recentlyPlayedMaxSize,
            )
        }
    }

    override suspend fun refreshAlbum(albumId: Long): Result<Unit> {
        return runCatching {
            val songs = remoteDataSource.lookupAlbumSongs(albumId)
            localDataSource.cacheSongs(
                songs = songs,
                accessedAtMillis = currentTimeMillis(),
                songCacheMaxSize = RecentlyPlayedCachePolicy.SONG_CACHE_MAX_SIZE,
            )
        }
    }

    private fun String.normalized(): String = trim()

    private fun List<Song>.reachedEnd(limit: Int): Boolean = size < limit

    private fun currentTimeMillis(): Long = System.currentTimeMillis()

    private companion object {
        const val FIRST_PAGE_OFFSET = 0
    }
}
