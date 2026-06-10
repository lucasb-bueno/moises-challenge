package com.lucasbueno.moises_challenge.data.remote.dto

import com.squareup.moshi.Json

data class ITunesSearchResponseDto(
    @param:Json(name = "resultCount")
    val resultCount: Int,
    @param:Json(name = "results")
    val results: List<ITunesSongDto>,
)
