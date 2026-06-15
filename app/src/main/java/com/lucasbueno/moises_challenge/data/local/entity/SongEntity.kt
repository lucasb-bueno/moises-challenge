package com.lucasbueno.moises_challenge.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lucasbueno.moises_challenge.domain.model.Song

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["lastAccessedAtMillis"]),
    ],
)
data class SongEntity(
    @PrimaryKey
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
    val lastAccessedAtMillis: Long,
)

fun SongEntity.toDomain(): Song {
    return Song(
        id = id,
        name = name,
        artistName = artistName,
        albumId = albumId,
        albumName = albumName,
        artworkUrl = artworkUrl,
        previewUrl = previewUrl,
        durationMillis = durationMillis,
        genre = genre,
        releaseDate = releaseDate,
    )
}

fun Song.toEntity(lastAccessedAtMillis: Long): SongEntity {
    return SongEntity(
        id = id,
        name = name,
        artistName = artistName,
        albumId = albumId,
        albumName = albumName,
        artworkUrl = artworkUrl,
        previewUrl = previewUrl,
        durationMillis = durationMillis,
        genre = genre,
        releaseDate = releaseDate,
        lastAccessedAtMillis = lastAccessedAtMillis,
    )
}
