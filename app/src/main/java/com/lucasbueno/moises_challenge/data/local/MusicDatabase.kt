package com.lucasbueno.moises_challenge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lucasbueno.moises_challenge.data.local.dao.SearchDao
import com.lucasbueno.moises_challenge.data.local.dao.SongDao
import com.lucasbueno.moises_challenge.data.local.entity.RecentlyPlayedEntity
import com.lucasbueno.moises_challenge.data.local.entity.SearchQueryEntity
import com.lucasbueno.moises_challenge.data.local.entity.SearchResultEntity
import com.lucasbueno.moises_challenge.data.local.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        SearchQueryEntity::class,
        SearchResultEntity::class,
        RecentlyPlayedEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    abstract fun searchDao(): SearchDao

    companion object {
        const val DATABASE_NAME = "music.db"
    }
}
