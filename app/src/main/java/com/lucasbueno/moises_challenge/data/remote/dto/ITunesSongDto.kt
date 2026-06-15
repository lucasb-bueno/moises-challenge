package com.lucasbueno.moises_challenge.data.remote.dto

import com.lucasbueno.moises_challenge.domain.model.Song
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ITunesSongDto(
    @SerialName("wrapperType")
    val wrapperType: String? = null,
    @SerialName("kind")
    val kind: String? = null,
    @SerialName("artistId")
    val artistId: Long? = null,
    @SerialName("collectionId")
    val collectionId: Long? = null,
    @SerialName("trackId")
    val trackId: Long? = null,
    @SerialName("artistName")
    val artistName: String? = null,
    @SerialName("collectionName")
    val collectionName: String? = null,
    @SerialName("trackName")
    val trackName: String? = null,
    @SerialName("artworkUrl100")
    val artworkUrl100: String? = null,
    @SerialName("previewUrl")
    val previewUrl: String? = null,
    @SerialName("trackTimeMillis")
    val trackTimeMillis: Long? = null,
    @SerialName("primaryGenreName")
    val primaryGenreName: String? = null,
    @SerialName("releaseDate")
    val releaseDate: String? = null,
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
