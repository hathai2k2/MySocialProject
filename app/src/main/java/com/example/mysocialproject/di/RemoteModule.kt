package com.example.mysocialproject.di

import android.content.Context
import androidx.room.Room
import com.example.mysocialproject.extension.Constant
import com.example.mysocialproject.networking.AppDataHelper
import com.example.mysocialproject.networking.AppDataManager
import com.example.mysocialproject.networking.pref.AppPrefManager
import com.example.mysocialproject.networking.pref.PrefHelper
import com.example.mysocialproject.networking.repository.FriendRepository
import com.example.mysocialproject.networking.repository.FriendRepositoryImpl
import com.example.mysocialproject.networking.repository.MessageRepository
import com.example.mysocialproject.networking.repository.MessageRepositoryImpl
import com.example.mysocialproject.networking.repository.PostPagingSource
import com.example.mysocialproject.networking.repository.PostRepository
import com.example.mysocialproject.networking.repository.PostRepositoryImpl
import com.example.mysocialproject.networking.repository.UserRepository
import com.example.mysocialproject.networking.repository.UserRepositoryImpl
import com.example.mysocialproject.networking.room.AppDatabase
import com.example.mysocialproject.networking.room.MessageDao
import com.example.mysocialproject.networking.room.UserDao
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {
    @Singleton
    @Provides
    fun provideAppDataManager(appDataManager: AppDataManager): AppDataHelper = appDataManager

    //pref

    @Singleton
    @Provides
    fun providePreference(mPref: AppPrefManager): PrefHelper = mPref

    //Dao

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

    //Firebase

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Suppress("DEPRECATION")
    @Provides
    @Singleton
    fun  provideFirebaseDynamicLinks(): FirebaseDynamicLinks {
        return FirebaseDynamicLinks.getInstance()
    }


    //Repository
    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao, auth: FirebaseAuth, fireStore: FirebaseFirestore, storage: FirebaseStorage): UserRepository {
        return UserRepositoryImpl(userDao, auth, fireStore, storage)
    }

    @Provides
    @Singleton
    fun providePostRepository(postPagingSource: PostPagingSource, auth: FirebaseAuth, fireStore: FirebaseFirestore, storage: FirebaseStorage): PostRepository {
        return PostRepositoryImpl(postPagingSource, auth, fireStore, storage)
    }



    @Provides
    @Singleton
    fun provideMessageRepository(messageDao: MessageDao, auth: FirebaseAuth, fireStore: FirebaseFirestore, storage: FirebaseStorage): MessageRepository {
        return MessageRepositoryImpl(messageDao, auth, fireStore, storage)
    }



    @Provides
    @Singleton
    fun provideFriendRepository( auth: FirebaseAuth, fireStore: FirebaseFirestore, storage: FirebaseStorage,pendingDynamicLinkData: FirebaseDynamicLinks): FriendRepository {
        return FriendRepositoryImpl( auth, fireStore, storage,pendingDynamicLinkData)
    }


}