package com.lucasbueno.moises_challenge.presentation.mock

import com.lucasbueno.moises_challenge.domain.model.Album
import com.lucasbueno.moises_challenge.domain.model.Song

object PreviewMusicData {
    val songs = listOf(
        Song(
            id = 1L,
            name = "Purple Rain",
            artistName = "Prince",
            albumId = 1L,
            albumName = "Purple Rain",
            artworkUrl = "https://picsum.photos/seed/purple-rain/300/300",
            previewUrl = null,
            durationMillis = 268_000L,
            genre = "Pop",
            releaseDate = null,
        ),
        Song(
            id = 2L,
            name = "Power Of Equality",
            artistName = "Red Hot Chili Peppers",
            albumId = 2L,
            albumName = "Blood Sugar Sex Magik",
            artworkUrl = "https://picsum.photos/seed/power-of-equality/300/300",
            previewUrl = null,
            durationMillis = 243_000L,
            genre = "Rock",
            releaseDate = null,
        ),
        Song(
            id = 3L,
            name = "Something",
            artistName = "The Beatles",
            albumId = 3L,
            albumName = "Abbey Road",
            artworkUrl = "https://picsum.photos/seed/something-beatles/300/300",
            previewUrl = null,
            durationMillis = 183_000L,
            genre = "Rock",
            releaseDate = null,
        ),
        Song(
            id = 4L,
            name = "Like A Virgin",
            artistName = "Madonna",
            albumId = 4L,
            albumName = "Like A Virgin",
            artworkUrl = "https://picsum.photos/seed/like-a-virgin/300/300",
            previewUrl = null,
            durationMillis = 218_000L,
            genre = "Pop",
            releaseDate = null,
        ),
        Song(
            id = 5L,
            name = "Get Lucky",
            artistName = "Daft Punk feat. Pharrell Williams",
            albumId = 5L,
            albumName = "Random Access Memories",
            artworkUrl = "https://picsum.photos/seed/get-lucky-daft-punk/300/300",
            previewUrl = null,
            durationMillis = 369_000L,
            genre = "Dance",
            releaseDate = null,
        ),
    )

    val albumSongs = listOf(
        songs[4].copy(id = 51L, name = "Around the World", artistName = "Daft Punk"),
        songs[4].copy(id = 52L, name = "Aerodynamic", artistName = "Daft Punk"),
        songs[4].copy(id = 53L, name = "Harder, Better, Faster, Stronger", artistName = "Daft Punk"),
        songs[4].copy(id = 54L, name = "Get Lucky"),
        songs[4].copy(id = 55L, name = "Digital Love", artistName = "Daft Punk"),
        songs[4].copy(id = 56L, name = "One More Time", artistName = "Daft Punk"),
    )

    val album = Album(
        id = 5L,
        name = "Album Title",
        artistName = "Daft Punk",
        artworkUrl = songs[4].artworkUrl,
        songs = albumSongs,
    )

    fun songById(songId: Long): Song = songs.firstOrNull { it.id == songId } ?: songs[4]
}
