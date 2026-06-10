package com.lucasbueno.moises_challenge.data.remote.dto

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ITunesSongDtoTest {
    @Test
    fun `isSong returns true for song tracks with track id`() {
        val dto = songDto(
            wrapperType = "track",
            kind = "song",
            trackId = 1L,
        )

        assertTrue(dto.isSong())
    }

    @Test
    fun `isSong returns false for collection wrappers`() {
        val dto = songDto(
            wrapperType = "collection",
            kind = null,
            trackId = null,
        )

        assertFalse(dto.isSong())
    }

    @Test
    fun `isSong returns false for tracks without id`() {
        val dto = songDto(
            wrapperType = "track",
            kind = "song",
            trackId = null,
        )

        assertFalse(dto.isSong())
    }

    @Test
    fun `toSongOrNull maps valid song dto to domain model`() {
        val dto = songDto(
            wrapperType = "track",
            kind = "song",
            trackId = 1L,
            trackName = "Song",
            artistName = "Artist",
        )

        val song = dto.toSongOrNull()

        requireNotNull(song)
        assertTrue(song.id == 1L)
        assertTrue(song.name == "Song")
        assertTrue(song.artistName == "Artist")
    }

    @Test
    fun `toSongOrNull returns null when required title is missing`() {
        val dto = songDto(
            wrapperType = "track",
            kind = "song",
            trackId = 1L,
            trackName = null,
            artistName = "Artist",
        )

        assertTrue(dto.toSongOrNull() == null)
    }

    private fun songDto(
        wrapperType: String?,
        kind: String?,
        trackId: Long?,
        trackName: String? = null,
        artistName: String? = null,
    ): ITunesSongDto {
        return ITunesSongDto(
            wrapperType = wrapperType,
            kind = kind,
            artistId = null,
            collectionId = null,
            trackId = trackId,
            artistName = artistName,
            collectionName = null,
            trackName = trackName,
            artworkUrl100 = null,
            previewUrl = null,
            trackTimeMillis = null,
            primaryGenreName = null,
            releaseDate = null,
        )
    }
}
