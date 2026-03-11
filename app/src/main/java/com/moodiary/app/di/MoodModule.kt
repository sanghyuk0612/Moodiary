package com.moodiary.app.di

import android.content.Context
import androidx.room.Room
import com.sanghyuk.data.mood.local.MoodDao
import com.sanghyuk.data.mood.local.MoodDatabase
import com.sanghyuk.data.mood.repository.RoomMoodRepository
import com.sanghyuk.domain.mood.repository.MoodRepository
import com.sanghyuk.domain.mood.usecase.DeleteAllMoodsUseCase
import com.sanghyuk.domain.mood.usecase.DeleteMoodUseCase
import com.sanghyuk.domain.mood.usecase.GetMoodEntriesUseCase
import com.sanghyuk.domain.mood.usecase.GetTodayMoodUseCase
import com.sanghyuk.domain.mood.usecase.SaveMoodUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MoodModule {
    @Provides
    @Singleton
    fun provideMoodDatabase(
        @ApplicationContext context: Context,
    ): MoodDatabase = Room.databaseBuilder(
        context,
        MoodDatabase::class.java,
        "moodiary.db",
    ).addCallback(MoodDatabaseSeedCallback()).build()

    @Provides
    @Singleton
    fun provideMoodDao(database: MoodDatabase): MoodDao = database.moodDao()

    @Provides
    @Singleton
    fun provideMoodRepository(moodDao: MoodDao): MoodRepository = RoomMoodRepository(moodDao)

    @Provides
    @Singleton
    fun provideGetTodayMoodUseCase(repository: MoodRepository): GetTodayMoodUseCase =
        GetTodayMoodUseCase(repository)

    @Provides
    @Singleton
    fun provideGetMoodEntriesUseCase(repository: MoodRepository): GetMoodEntriesUseCase =
        GetMoodEntriesUseCase(repository)

    @Provides
    @Singleton
    fun provideSaveMoodUseCase(repository: MoodRepository): SaveMoodUseCase =
        SaveMoodUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteMoodUseCase(repository: MoodRepository): DeleteMoodUseCase =
        DeleteMoodUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteAllMoodsUseCase(repository: MoodRepository): DeleteAllMoodsUseCase =
        DeleteAllMoodsUseCase(repository)
}

private class MoodDatabaseSeedCallback : androidx.room.RoomDatabase.Callback() {
    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        super.onCreate(db)
        // FIXME: 테스트 데이터
    }
}