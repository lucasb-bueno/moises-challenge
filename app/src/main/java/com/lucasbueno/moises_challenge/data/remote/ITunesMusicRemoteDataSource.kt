package com.lucasbueno.moises_challenge.data.remote

import com.lucasbueno.moises_challenge.data.remote.api.ITunesApi
import com.lucasbueno.moises_challenge.data.remote.dto.isSong
import com.lucasbueno.moises_challenge.data.remote.dto.toSongOrNull
import com.lucasbueno.moises_challenge.domain.model.Song
import javax.inject.Inject

class ITunesMusicRemoteDataSource @Inject constructor(
    private val api: ITunesApi,
) : MusicRemoteDataSource {
    override suspend fun searchSongs(
        query: String,
        offset: Int,
        limit: Int,
    ): List<Song> {
        if (offset >= MAX_SEARCH_LIMIT) return emptyList()

        val requestLimit = (offset + limit).coerceAtMost(MAX_SEARCH_LIMIT)

        return api.searchSongs(
            term = query,
            media = MEDIA_MUSIC,
            entity = ENTITY_SONG,
            limit = requestLimit,
        ).results.mapNotNull { songDto ->
            songDto.takeIf { it.isSong() }?.toSongOrNull()
        }.drop(offset)
    }

    override suspend fun lookupAlbumSongs(albumId: Long): List<Song> {
        return api.lookupAlbumSongs(
            albumId = albumId,
            entity = ENTITY_SONG,
        ).results.mapNotNull { songDto ->
            songDto.takeIf { it.isSong() }?.toSongOrNull()
        }
    }

    private companion object {
        const val MEDIA_MUSIC = "music"
        const val ENTITY_SONG = "song"
        const val MAX_SEARCH_LIMIT = 200
    }
}
