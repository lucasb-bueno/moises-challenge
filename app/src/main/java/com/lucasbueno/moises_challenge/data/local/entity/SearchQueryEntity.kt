package com.lucasbueno.moises_challenge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_queries")
data class SearchQueryEntity(
    @PrimaryKey
    val query: String,
    val nextOffset: Int,
    val reachedEnd: Boolean,
    val updatedAtMillis: Long,
)
