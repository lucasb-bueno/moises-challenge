package com.lucasbueno.moises_challenge.data.repository

import com.lucasbueno.moises_challenge.data.local.MusicLocalDataSource
import com.lucasbueno.moises_challenge.data.remote.MusicRemoteDataSource
import com.lucasbueno.moises_challenge.domain.model.Album
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

    override suspend fun refreshSearch(query: String, limit: Int): Result<Unit> {
        return runCatching {
            val normalizedQuery = query.normalized()
            val songs = remoteDataSource.searchSongs(
                query = normalizedQuery,
                offset = FIRST_PAGE_OFFSET,
                limit = limit,
            )

            localDataSource.replaceSearchResults(
                query = normalizedQuery,
                songs = songs,
                nextOffset = songs.size,
                reachedEnd = songs.reachedEnd(limit),
                updatedAtMillis = System.currentTimeMillis(),
            )
        }
    }

    override suspend fun loadNextSearchPage(query: String, limit: Int): Result<Unit> {
        return runCatching {
            val normalizedQuery = query.normalized()
            val metadata = localDataSource.getSearchMetadata(normalizedQuery)

            if (metadata?.reachedEnd == true) return@runCatching

            val offset = metadata?.nextOffset ?: FIRST_PAGE_OFFSET
            val songs = remoteDataSource.searchSongs(
                query = normalizedQuery,
                offset = offset,
                limit = limit,
            )

            localDataSource.appendSearchResults(
                query = normalizedQuery,
                songs = songs,
                nextOffset = offset + songs.size,
                reachedEnd = songs.reachedEnd(limit),
                updatedAtMillis = System.currentTimeMillis(),
            )
        }
    }

    override suspend fun markAsRecentlyPlayed(songId: Long): Result<Unit> {
        return runCatching {
            localDataSource.markAsRecentlyPlayed(
                songId = songId,
                playedAtMillis = System.currentTimeMillis(),
            )
        }
    }

    override suspend fun refreshAlbum(albumId: Long): Result<Unit> {
        return runCatching {
            val songs = remoteDataSource.lookupAlbumSongs(albumId)
            localDataSource.cacheSongs(songs)
        }
    }

    private fun String.normalized(): String = trim()

    private fun List<Song>.reachedEnd(limit: Int): Boolean = size < limit

    private companion object {
        const val FIRST_PAGE_OFFSET = 0
    }
}
