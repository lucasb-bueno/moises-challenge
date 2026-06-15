package com.lucasbueno.moises_challenge.data.local

import com.lucasbueno.moises_challenge.domain.model.Album
import com.lucasbueno.moises_challenge.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicLocalDataSource {
    fun getSearchResultsFlow(query: String): Flow<List<Song>>

    fun getRecentlyPlayedSongsFlow(limit: Int): Flow<List<Song>>

    fun getSongFlow(songId: Long): Flow<Song?>

    fun getAlbumFlow(albumId: Long): Flow<Album?>

    suspend fun getSearchMetadata(query: String): CachedSearchMetadata?

    suspend fun replaceSearchResults(
        query: String,
        songs: List<Song>,
        nextOffset: Int,
        reachedEnd: Boolean,
        updatedAtMillis: Long,
        songCacheMaxSize: Int,
        searchCacheMaxQueries: Int,
    )

    suspend fun appendSearchResults(
        query: String,
        songs: List<Song>,
        nextOffset: Int,
        reachedEnd: Boolean,
        updatedAtMillis: Long,
        songCacheMaxSize: Int,
        searchCacheMaxQueries: Int,
    )

    suspend fun cacheSongs(songs: List<Song>, accessedAtMillis: Long, songCacheMaxSize: Int)

    suspend fun markAsRecentlyPlayed(
        songId: Long,
        playedAtMillis: Long,
        recentlyPlayedMaxSize: Int,
        songCacheMaxSize: Int,
    )

    suspend fun recycleRecentlyPlayedCache(
        recentlyPlayedExpiresBeforeMillis: Long,
        recentlyPlayedMaxSize: Int,
    )
}
