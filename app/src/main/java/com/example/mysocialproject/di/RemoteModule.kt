package com.example.mysocialproject.di

import android.content.Context
import androidx.room.Room
import com.example.mysocialproject.extension.Constant
import com.example.mysocialproject.networking.room.AppDatabase
import com.example.mysocialproject.networking.room.MessageDao
import com.example.mysocialproject.networking.room.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
             Constant.TABLE_NAME
        )
            .addMigrations(Constant.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase):UserDao{
        return database.userDao()
    }
}