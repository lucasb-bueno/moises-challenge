package com.lucasbueno.moises_challenge.domain.model

data class SongSearchPage(
    val songs: List<Song>,
    val nextOffset: Int?,
    val hasMore: Boolean,
)
