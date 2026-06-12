package com.lucasbueno.moises_challenge.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SongsRoute

@Serializable
data class SongDetailsRoute(
    val songId: Long,
)

@Serializable
data class AlbumRoute(
    val albumId: Long,
)
