package com.lucasbueno.moises_challenge.data.local

import androidx.room.withTransaction
import com.lucasbueno.moises_challenge.data.local.entity.RecentlyPlayedEntity
import com.lucasbueno.moises_challenge.data.local.entity.SearchQueryEntity
import com.lucasbueno.moises_challenge.data.local.entity.SearchResultEntity
import com.lucasbueno.moises_challenge.data.local.entity.toDomain
import com.lucasbueno.moises_challenge.data.local.entity.toEntity
import com.lucasbueno.moises_challenge.domain.model.Album
import com.lucasbueno.moises_challenge.domain.model.Song
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMusicLocalDataSource @Inject constructor(
    private val database: MusicDatabase,
) : MusicLocalDataSource {
    private val songDao = database.songDao()
    private val searchDao = database.searchDao()

    override fun getSearchResultsFlow(query: String): Flow<List<Song>> {
        return searchDao.observeSearchResults(query)
            .map { songs -> songs.map { it.toDomain() } }
    }

    override fun getRecentlyPlayedSongsFlow(limit: Int): Flow<List<Song>> {
        return songDao.observeRecentlyPlayedSongs(limit)
            .map { songs -> songs.map { it.toDomain() } }
    }

    override fun getSongFlow(songId: Long): Flow<Song?> {
        return songDao.observeSong(songId)
            .map { song -> song?.toDomain() }
    }

    override fun getAlbumFlow(albumId: Long): Flow<Album?> {
        return songDao.observeSongsByAlbum(albumId)
            .map { songs -> songs.map { it.toDomain() }.toAlbumOrNull(albumId) }
    }

    override suspend fun getSearchMetadata(query: String): CachedSearchMetadata? {
        return searchDao.getSearchQuery(query)?.let { searchQuery ->
            CachedSearchMetadata(
                nextOffset = searchQuery.nextOffset,
                reachedEnd = searchQuery.reachedEnd,
            )
        }
    }

    override suspend fun replaceSearchResults(
        query: String,
        songs: List<Song>,
        nextOffset: Int,
        reachedEnd: Boolean,
        updatedAtMillis: Long,
        songCacheMaxSize: Int,
        searchCacheMaxQueries: Int,
    ) {
        database.withTransaction {
            songDao.upsertSongs(songs.map { it.toEntity(lastAccessedAtMillis = updatedAtMillis) })
            searchDao.replaceSearchResults(
                searchQuery = searchQuery(
                    query = query,
                    nextOffset = nextOffset,
                    reachedEnd = reachedEnd,
                    updatedAtMillis = updatedAtMillis,
                ),
                searchResults = songs.toSearchResults(query = query, startPosition = 0),
            )
            recycleBoundedCaches(
                songCacheMaxSize = songCacheMaxSize,
                searchCacheMaxQueries = searchCacheMaxQueries,
            )
        }
    }

    override suspend fun appendSearchResults(
        query: String,
        songs: List<Song>,
        nextOffset: Int,
        reachedEnd: Boolean,
        updatedAtMillis: Long,
        songCacheMaxSize: Int,
        searchCacheMaxQueries: Int,
    ) {
        database.withTransaction {
            val startPosition = searchDao.getNextSearchResultPosition(query)

            songDao.upsertSongs(songs.map { it.toEntity(lastAccessedAtMillis = updatedAtMillis) })
            searchDao.appendSearchResults(
                searchQuery = searchQuery(
                    query = query,
                    nextOffset = nextOffset,
                    reachedEnd = reachedEnd,
                    updatedAtMillis = updatedAtMillis,
                ),
                searchResults = songs.toSearchResults(
                    query = query,
                    startPosition = startPosition,
                ),
            )
            recycleBoundedCaches(
                songCacheMaxSize = songCacheMaxSize,
                searchCacheMaxQueries = searchCacheMaxQueries,
            )
        }
    }

    override suspend fun cacheSongs(
        songs: List<Song>,
        accessedAtMillis: Long,
        songCacheMaxSize: Int,
    ) {
        database.withTransaction {
            songDao.upsertSongs(songs.map { it.toEntity(lastAccessedAtMillis = accessedAtMillis) })
            songDao.pruneSongsToSize(songCacheMaxSize.coerceAtLeast(MIN_CACHE_SIZE))
        }
    }

    override suspend fun markAsRecentlyPlayed(
        songId: Long,
        playedAtMillis: Long,
        recentlyPlayedMaxSize: Int,
        songCacheMaxSize: Int,
    ) {
        database.withTransaction {
            songDao.updateSongLastAccessedAt(
                songId = songId,
                lastAccessedAtMillis = playedAtMillis,
            )
            songDao.upsertRecentlyPlayed(
                RecentlyPlayedEntity(
                    songId = songId,
                    playedAtMillis = playedAtMillis,
                ),
            )
            songDao.pruneRecentlyPlayedToSize(recentlyPlayedMaxSize.coerceAtLeast(MIN_CACHE_SIZE))
            songDao.pruneSongsToSize(songCacheMaxSize.coerceAtLeast(MIN_CACHE_SIZE))
        }
    }

    override suspend fun recycleRecentlyPlayedCache(
        recentlyPlayedExpiresBeforeMillis: Long,
        recentlyPlayedMaxSize: Int,
    ) {
        database.withTransaction {
            songDao.deleteRecentlyPlayedOlderThan(recentlyPlayedExpiresBeforeMillis)
            songDao.pruneRecentlyPlayedToSize(recentlyPlayedMaxSize.coerceAtLeast(MIN_CACHE_SIZE))
        }
    }

    private suspend fun recycleBoundedCaches(songCacheMaxSize: Int, searchCacheMaxQueries: Int) {
        searchDao.pruneSearchQueriesToSize(searchCacheMaxQueries.coerceAtLeast(MIN_CACHE_SIZE))
        songDao.pruneSongsToSize(songCacheMaxSize.coerceAtLeast(MIN_CACHE_SIZE))
    }

    private fun searchQuery(
        query: String,
        nextOffset: Int,
        reachedEnd: Boolean,
        updatedAtMillis: Long,
    ): SearchQueryEntity {
        return SearchQueryEntity(
            query = query,
            nextOffset = nextOffset,
            reachedEnd = reachedEnd,
            updatedAtMillis = updatedAtMillis,
        )
    }

    private fun List<Song>.toSearchResults(
        query: String,
        startPosition: Int,
    ): List<SearchResultEntity> {
        return mapIndexed { index, song ->
            SearchResultEntity(
                query = query,
                songId = song.id,
                position = startPosition + index,
            )
        }
    }

    private fun List<Song>.toAlbumOrNull(albumId: Long): Album? {
        val songs = filter { it.albumId == albumId }
        val firstSong = songs.firstOrNull() ?: return null

        return Album(
            id = albumId,
            name = firstSong.albumName.orEmpty(),
            artistName = firstSong.artistName,
            artworkUrl = firstSong.artworkUrl,
            songs = songs,
        )
    }

    private companion object {
        const val MIN_CACHE_SIZE = 0
    }
}
