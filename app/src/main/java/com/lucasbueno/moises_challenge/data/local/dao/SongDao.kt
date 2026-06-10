package com.lucasbueno.moises_challenge.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lucasbueno.moises_challenge.data.local.entity.RecentlyPlayedEntity
import com.lucasbueno.moises_challenge.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("SELECT * FROM songs WHERE id = :songId")
    fun observeSong(songId: Long): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE albumId = :albumId ORDER BY id")
    fun observeSongsByAlbum(albumId: Long): Flow<List<SongEntity>>

    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN recently_played_songs ON recently_played_songs.songId = songs.id
        ORDER BY recently_played_songs.playedAtMillis DESC
        LIMIT :limit
        """,
    )
    fun observeRecentlyPlayedSongs(limit: Int): Flow<List<SongEntity>>

    @Upsert
    suspend fun upsertRecentlyPlayed(recentlyPlayed: RecentlyPlayedEntity)
}
