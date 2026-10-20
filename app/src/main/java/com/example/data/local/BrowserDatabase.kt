package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BookmarkEntity
import com.example.data.model.DownloadItemEntity
import com.example.data.model.HistoryEntity
import com.example.data.model.ProfileEntity
import com.example.data.model.ShortcutEntity
import com.example.data.model.SitePermissionEntity
import com.example.data.model.TabEntity

@Database(
    entities = [
        ProfileEntity::class,
        TabEntity::class,
        BookmarkEntity::class,
        HistoryEntity::class,
        DownloadItemEntity::class,
        ShortcutEntity::class,
        SitePermissionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun tabDao(): TabDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun downloadDao(): DownloadDao
    abstract fun shortcutDao(): ShortcutDao
    abstract fun sitePermissionDao(): SitePermissionDao

    companion object {
        @Volatile
        private var INSTANCE: BrowserDatabase? = null

        fun getInstance(context: Context): BrowserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrowserDatabase::class.java,
                    "prism_browser.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
