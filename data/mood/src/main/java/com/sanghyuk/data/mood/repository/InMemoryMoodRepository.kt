package com.sanghyuk.data.mood.repository

import com.sanghyuk.domain.mood.model.MoodEntry
import com.sanghyuk.domain.mood.repository.MoodRepository

class InMemoryMoodRepository : MoodRepository {
    private var cachedEntry: MoodEntry? = null

    override suspend fun getTodayMood(): MoodEntry? = cachedEntry

    override suspend fun saveMood(entry: MoodEntry) {
        cachedEntry = entry
    }
}
