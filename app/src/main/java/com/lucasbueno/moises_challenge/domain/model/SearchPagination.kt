package com.lucasbueno.moises_challenge.domain.model

data class SearchPagination(
    val nextOffset: Int,
    val reachedEnd: Boolean,
)
