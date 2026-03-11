package com.sanghyuk.domain.mood.repository

import com.sanghyuk.domain.mood.model.MoodEntry

interface MoodRepository {
    suspend fun getTodayMood(): MoodEntry?
    suspend fun getMoodEntries(): List<MoodEntry>
    suspend fun saveMood(entry: MoodEntry)
}
