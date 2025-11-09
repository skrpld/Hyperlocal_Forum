package com.example.hyperlocal_forum.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hyperlocal_forum.data.models.local.LocalComment
import com.example.hyperlocal_forum.data.models.local.LocalTopic
import com.example.hyperlocal_forum.data.models.local.LocalUser

@Database(
    entities = [LocalTopic::class, LocalComment::class, LocalUser::class],
    version = 3,
    exportSchema = false
)
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
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}