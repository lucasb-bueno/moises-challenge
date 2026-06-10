package com.lucasbueno.moises_challenge.data.local.entity

import com.lucasbueno.moises_challenge.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class SongEntityTest {
    @Test
    fun `toEntity maps domain song to cached song entity`() {
        val song = Song(
            id = 1L,
            name = "Song name",
            artistName = "Artist name",
            albumId = 2L,
            albumName = "Album name",
            artworkUrl = "https://example.com/artwork.jpg",
            previewUrl = "https://example.com/preview.m4a",
            durationMillis = 30_000L,
            genre = "Pop",
            releaseDate = "2026-01-01T00:00:00Z",
        )

        val entity = song.toEntity()

        assertEquals(
            SongEntity(
                id = 1L,
                name = "Song name",
                artistName = "Artist name",
                albumId = 2L,
                albumName = "Album name",
                artworkUrl = "https://example.com/artwork.jpg",
                previewUrl = "https://example.com/preview.m4a",
                durationMillis = 30_000L,
                genre = "Pop",
                releaseDate = "2026-01-01T00:00:00Z",
            ),
            entity,
        )
    }

    @Test
    fun `toDomain maps cached song entity to domain song`() {
        val entity = SongEntity(
            id = 1L,
            name = "Song name",
            artistName = "Artist name",
            albumId = 2L,
            albumName = "Album name",
            artworkUrl = "https://example.com/artwork.jpg",
            previewUrl = "https://example.com/preview.m4a",
            durationMillis = 30_000L,
            genre = "Pop",
            releaseDate = "2026-01-01T00:00:00Z",
        )

        val song = entity.toDomain()

        assertEquals(
            Song(
                id = 1L,
                name = "Song name",
                artistName = "Artist name",
                albumId = 2L,
                albumName = "Album name",
                artworkUrl = "https://example.com/artwork.jpg",
                previewUrl = "https://example.com/preview.m4a",
                durationMillis = 30_000L,
                genre = "Pop",
                releaseDate = "2026-01-01T00:00:00Z",
            ),
            song,
        )
    }
}
