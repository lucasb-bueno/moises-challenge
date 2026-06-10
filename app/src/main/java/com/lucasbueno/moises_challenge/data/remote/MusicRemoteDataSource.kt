package com.lucasbueno.moises_challenge.data.remote

import com.lucasbueno.moises_challenge.domain.model.Song

interface MusicRemoteDataSource {
    suspend fun searchSongs(
        query: String,
        offset: Int,
        limit: Int,
    ): List<Song>

    suspend fun lookupAlbumSongs(albumId: Long): List<Song>
}
