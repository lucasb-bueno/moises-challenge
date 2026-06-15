package com.lucasbueno.moises_challenge.domain.repository

import com.lucasbueno.moises_challenge.domain.model.Album
import com.lucasbueno.moises_challenge.domain.model.SearchPagination
import com.lucasbueno.moises_challenge.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getSearchResultsFlow(query: String): Flow<List<Song>>

    fun getRecentlyPlayedSongsFlow(limit: Int): Flow<List<Song>>

    fun getSongFlow(songId: Long): Flow<Song?>

    fun getAlbumFlow(albumId: Long): Flow<Album?>

    suspend fun refreshSearch(query: String, limit: Int): Result<SearchPagination>

    suspend fun loadNextSearchPage(query: String, limit: Int): Result<SearchPagination>

    suspend fun markAsRecentlyPlayed(songId: Long): Result<Unit>

    suspend fun refreshAlbum(albumId: Long): Result<Unit>
}
