package com.sanghyuk.data.mood.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries WHERE date = :date LIMIT 1")
    suspend fun getMoodByDate(date: String): MoodEntity?

    @Query("SELECT * FROM mood_entries ORDER BY date ASC")
    suspend fun getMoodEntries(): List<MoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMood(entity: MoodEntity)
}
