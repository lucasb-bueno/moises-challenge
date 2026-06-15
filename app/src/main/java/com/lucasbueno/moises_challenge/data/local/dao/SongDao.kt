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

    @Query("UPDATE songs SET lastAccessedAtMillis = :lastAccessedAtMillis WHERE id = :songId")
    suspend fun updateSongLastAccessedAt(songId: Long, lastAccessedAtMillis: Long)

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

    @Query("DELETE FROM recently_played_songs WHERE playedAtMillis < :expiresBeforeMillis")
    suspend fun deleteRecentlyPlayedOlderThan(expiresBeforeMillis: Long)

    @Query(
        """
        DELETE FROM recently_played_songs
        WHERE songId NOT IN (
            SELECT songId FROM recently_played_songs
            ORDER BY playedAtMillis DESC, songId DESC
            LIMIT :maxSize
        )
        """,
    )
    suspend fun pruneRecentlyPlayedToSize(maxSize: Int)

    @Query(
        """
        DELETE FROM songs
        WHERE id NOT IN (
            SELECT id FROM songs
            ORDER BY lastAccessedAtMillis DESC, id DESC
            LIMIT :maxSize
        )
        """,
    )
    suspend fun pruneSongsToSize(maxSize: Int)
}
