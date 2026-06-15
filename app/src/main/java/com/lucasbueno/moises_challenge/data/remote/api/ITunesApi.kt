package com.lucasbueno.moises_challenge.data.remote.api

import com.lucasbueno.moises_challenge.data.remote.dto.ITunesSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("media") media: String,
        @Query("entity") entity: String,
        @Query("limit") limit: Int,
    ): ITunesSearchResponseDto

    @GET("lookup")
    suspend fun lookupAlbumSongs(
        @Query("id") albumId: Long,
        @Query("entity") entity: String,
    ): ITunesSearchResponseDto
}
