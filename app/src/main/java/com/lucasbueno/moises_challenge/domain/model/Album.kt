package com.lucasbueno.moises_challenge.domain.model

data class Album(
    val id: Long,
    val name: String,
    val artistName: String,
    val artworkUrl: String?,
    val songs: List<Song>,
)
