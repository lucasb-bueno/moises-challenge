package com.lucasbueno.moises_challenge.data.remote.dto

import com.lucasbueno.moises_challenge.domain.model.Song
import com.squareup.moshi.Json

data class ITunesSongDto(
    @param:Json(name = "wrapperType")
    val wrapperType: String?,
    @param:Json(name = "kind")
    val kind: String?,
    @param:Json(name = "artistId")
    val artistId: Long?,
    @param:Json(name = "collectionId")
    val collectionId: Long?,
    @param:Json(name = "trackId")
    val trackId: Long?,
    @param:Json(name = "artistName")
    val artistName: String?,
    @param:Json(name = "collectionName")
    val collectionName: String?,
    @param:Json(name = "trackName")
    val trackName: String?,
    @param:Json(name = "artworkUrl100")
    val artworkUrl100: String?,
    @param:Json(name = "previewUrl")
    val previewUrl: String?,
    @param:Json(name = "trackTimeMillis")
    val trackTimeMillis: Long?,
    @param:Json(name = "primaryGenreName")
    val primaryGenreName: String?,
    @param:Json(name = "releaseDate")
    val releaseDate: String?,
)

fun ITunesSongDto.isSong(): Boolean = wrapperType == "track" && kind == "song" && trackId != null

fun ITunesSongDto.toSongOrNull(): Song? {
    val id = trackId ?: return null
    val name = trackName?.takeIf { it.isNotBlank() } ?: return null
    val artist = artistName?.takeIf { it.isNotBlank() } ?: return null

    return Song(
        id = id,
        name = name,
        artistName = artist,
        albumId = collectionId,
        albumName = collectionName,
        artworkUrl = artworkUrl100,
        previewUrl = previewUrl,
        durationMillis = trackTimeMillis,
        genre = primaryGenreName,
        releaseDate = releaseDate,
    )
}
