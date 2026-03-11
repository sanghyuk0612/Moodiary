package com.sanghyuk.domain.mood.repository

import com.sanghyuk.domain.mood.model.MoodEntry
import java.time.LocalDate

interface MoodRepository {
    suspend fun getTodayMood(): MoodEntry?
    suspend fun getMoodEntries(): List<MoodEntry>
    suspend fun saveMood(entry: MoodEntry)
    suspend fun deleteMood(date: LocalDate)
    suspend fun deleteAllMoods()
}