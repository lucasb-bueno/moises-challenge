package com.lucasbueno.moises_challenge.domain.repository

import com.lucasbueno.moises_challenge.domain.model.Album
import com.lucasbueno.moises_challenge.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun observeSearchResults(query: String): Flow<List<Song>>

    fun observeRecentlyPlayedSongs(limit: Int): Flow<List<Song>>

    fun observeSong(songId: Long): Flow<Song?>

    fun observeAlbum(albumId: Long): Flow<Album?>

    suspend fun refreshSearch(query: String, limit: Int): Result<Unit>

    suspend fun loadNextSearchPage(query: String, limit: Int): Result<Unit>

    suspend fun markAsRecentlyPlayed(songId: Long): Result<Unit>

    suspend fun refreshAlbum(albumId: Long): Result<Unit>
}
