package com.example.hyperlocal_forum.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Topic::class, Comment::class], version = 1)
@TypeConverters(GeoCoordinatesConverter::class)
abstract class ForumDatabase : RoomDatabase() {
    abstract fun forumDao(): ForumDao
    
    companion object {
        @Volatile
        private var INSTANCE: ForumDatabase? = null

        fun getDatabase(context: Context): ForumDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ForumDatabase::class.java,
                    "forum_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}