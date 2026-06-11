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
    )

    suspend fun appendSearchResults(
        query: String,
        songs: List<Song>,
        nextOffset: Int,
        reachedEnd: Boolean,
        updatedAtMillis: Long,
    )

    suspend fun cacheSongs(songs: List<Song>)

    suspend fun markAsRecentlyPlayed(songId: Long, playedAtMillis: Long)
}
