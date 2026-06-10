package com.lucasbueno.moises_challenge.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "search_results",
    primaryKeys = ["query", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = SearchQueryEntity::class,
            parentColumns = ["query"],
            childColumns = ["query"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["query", "position"]),
        Index(value = ["songId"]),
    ],
)
data class SearchResultEntity(
    val query: String,
    val songId: Long,
    val position: Int,
)
