package com.example.mysocialproject.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {
//    @Singleton
//    @Provides
//    fun provideAppDataManager(appDataManager: AppDataManager): AppDataHelper = appDataManager
//
//    //pref
//
//    @Singleton
//    @Provides
//    fun providePreference(mPref: AppPrefManager): PrefHelper = mPref
//


    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    //Dao
//
//    @Provides
//    @Singleton
//    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
//        return Room.databaseBuilder(
//            context,
//            AppDatabase::class.java,
//             Constant.TABLE_NAME
//        )
//            .addMigrations(Constant.MIGRATION_2_3)
//            .allowMainThreadQueries()
//            .build()
//    }

//    @Provides
//    fun provideMessageDao(database: AppDatabase): MessageDao {
//        return database.messageDao()
//    }

//    @Provides
//    fun provideUserDao(database: AppDatabase):UserDao{
//        return database.userDao()
//    }

    //Firebase
//
//    @Provides
//    @Singleton
//    fun provideFirebaseAuth(): FirebaseAuth {
//        return FirebaseAuth.getInstance()
//    }
//
//    @Provides
//    @Singleton
//    fun provideFirebaseFirestore(): FirebaseFirestore {
//        return FirebaseFirestore.getInstance()
//    }
//
//    @Provides
//    @Singleton
//    fun provideFirebaseStorage(): FirebaseStorage {
//        return FirebaseStorage.getInstance()
//    }
//
//    @Suppress("DEPRECATION")
//    @Provides
//    @Singleton
//    fun provideFirebaseDynamicLinks(): FirebaseDynamicLinks {
//        return FirebaseDynamicLinks.getInstance()
//    }
//
//
//    //Repository
//    @Provides
//    @Singleton
//    fun provideUserRepository(
//        auth: FirebaseAuth,
//        fireStore: FirebaseFirestore,
//        storage: FirebaseStorage
//    ): UserRepository {
//        return UserRepositoryImpl(auth, fireStore, storage)
//    }
//
//    @Provides
//    @Singleton
//    fun providePostRepository(
//        postPagingSource: PostPagingSource,
//        auth: FirebaseAuth,
//        fireStore: FirebaseFirestore,
//        storage: FirebaseStorage
//    ): PostRepository {
//        return PostRepositoryImpl(postPagingSource, auth, fireStore, storage)
//    }
//
//
//    @Provides
//    @Singleton
//    fun provideMessageRepository(
//        auth: FirebaseAuth,
//        fireStore: FirebaseFirestore,
//        storage: FirebaseStorage
//    ): MessageRepository {
//        return MessageRepositoryImpl(auth, fireStore, storage)
//    }
//
//
//    @Provides
//    @Singleton
//    fun provideFriendRepository(
//        auth: FirebaseAuth,
//        fireStore: FirebaseFirestore,
//        storage: FirebaseStorage,
//        pendingDynamicLinkData: FirebaseDynamicLinks
//    ): FriendRepository {
//        return FriendRepositoryImpl(auth, fireStore, storage, pendingDynamicLinkData)
//    }


}