package com.example.hyperlocal_forum.di

import android.content.Context
import com.example.hyperlocal_forum.data.ForumDatabase
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.data.ForumRepository
import com.example.hyperlocal_forum.utils.AuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideForumDatabase(@ApplicationContext context: Context): ForumDatabase {
        return ForumDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideForumDao(database: ForumDatabase): ForumDao {
        return database.forumDao()
    }

    @Provides
    @Singleton
    fun provideForumRepository(forumDao: ForumDao): ForumRepository {
        return ForumRepository(forumDao)
    }

    @Provides
    @Singleton
    fun provideAuthManager(): AuthManager {
        return AuthManager()
    }
}
