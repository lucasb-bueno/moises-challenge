package com.lucasbueno.moises_challenge.domain.model

data class Song(
    val id: Long,
    val name: String,
    val artistName: String,
    val albumId: Long?,
    val albumName: String?,
    val artworkUrl: String?,
    val previewUrl: String?,
    val durationMillis: Long?,
    val genre: String?,
    val releaseDate: String?,
)
