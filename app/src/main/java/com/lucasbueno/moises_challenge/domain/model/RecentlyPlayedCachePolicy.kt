package com.lucasbueno.moises_challenge.domain.model

object RecentlyPlayedCachePolicy {
    const val MAX_SIZE = 7
    const val MAX_AGE_MILLIS = 5 * 60 * 1_000L
    const val RECYCLE_INTERVAL_MILLIS = MAX_AGE_MILLIS
    const val SONG_CACHE_MAX_SIZE = 200
    const val SEARCH_CACHE_MAX_QUERIES = 20
}
