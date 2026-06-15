package com.lucasbueno.moises_challenge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.lucasbueno.moises_challenge.data.local.entity.SearchQueryEntity
import com.lucasbueno.moises_challenge.data.local.entity.SearchResultEntity
import com.lucasbueno.moises_challenge.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {
    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN search_results ON search_results.songId = songs.id
        WHERE search_results.query = :query
        ORDER BY search_results.position ASC
        """,
    )
    fun observeSearchResults(query: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM search_queries WHERE query = :query")
    suspend fun getSearchQuery(query: String): SearchQueryEntity?

    @Query("SELECT COUNT(*) FROM search_results WHERE query = :query")
    suspend fun getSearchResultCount(query: String): Int

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM search_results WHERE query = :query")
    suspend fun getNextSearchResultPosition(query: String): Int

    @Upsert
    suspend fun upsertSearchQuery(searchQuery: SearchQueryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(searchResults: List<SearchResultEntity>)

    @Query("DELETE FROM search_results WHERE query = :query")
    suspend fun deleteSearchResults(query: String)

    @Query(
        """
        DELETE FROM search_queries
        WHERE query NOT IN (
            SELECT query FROM search_queries
            ORDER BY updatedAtMillis DESC, query DESC
            LIMIT :maxSize
        )
        """,
    )
    suspend fun pruneSearchQueriesToSize(maxSize: Int)

    @Transaction
    suspend fun replaceSearchResults(
        searchQuery: SearchQueryEntity,
        searchResults: List<SearchResultEntity>,
    ) {
        upsertSearchQuery(searchQuery)
        deleteSearchResults(searchQuery.query)
        insertSearchResults(searchResults)
    }

    @Transaction
    suspend fun appendSearchResults(
        searchQuery: SearchQueryEntity,
        searchResults: List<SearchResultEntity>,
    ) {
        upsertSearchQuery(searchQuery)
        insertSearchResults(searchResults)
    }
}
