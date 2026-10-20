package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun getBookmarksByProfileFlow(profileId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE profileId = :profileId ORDER BY createdAt DESC")
    suspend fun getBookmarksByProfile(profileId: String): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE profileId = :profileId AND url = :url LIMIT 1")
    suspend fun getBookmarkByUrl(profileId: String, url: String): BookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE profileId = :profileId AND (title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%')")
    suspend fun searchBookmarks(profileId: String, query: String): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE profileId = :profileId AND url = :url")
    suspend fun deleteBookmarkByUrl(profileId: String, url: String)

    @Query("DELETE FROM bookmarks WHERE profileId = :profileId")
    suspend fun deleteAllBookmarksForProfile(profileId: String)
}
