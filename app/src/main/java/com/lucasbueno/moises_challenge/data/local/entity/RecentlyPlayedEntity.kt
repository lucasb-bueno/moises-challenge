package com.lucasbueno.moises_challenge.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recently_played_songs",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["playedAtMillis"]),
    ],
)
data class RecentlyPlayedEntity(
    @PrimaryKey
    val songId: Long,
    val playedAtMillis: Long,
)
