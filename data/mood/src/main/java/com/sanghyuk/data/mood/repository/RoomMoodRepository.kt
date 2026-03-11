package com.sanghyuk.data.mood.repository

import com.sanghyuk.data.mood.local.MoodDao
import com.sanghyuk.data.mood.mapper.toDomain
import com.sanghyuk.data.mood.mapper.toEntity
import com.sanghyuk.domain.mood.model.MoodEntry
import com.sanghyuk.domain.mood.repository.MoodRepository
import java.time.LocalDate

class RoomMoodRepository(
    private val moodDao: MoodDao,
) : MoodRepository {
    override suspend fun getTodayMood(): MoodEntry? = moodDao.getMoodByDate(LocalDate.now().toString())?.toDomain()

    override suspend fun getMoodEntries(): List<MoodEntry> = moodDao.getMoodEntries().map { it.toDomain() }

    override suspend fun saveMood(entry: MoodEntry) {
        moodDao.upsertMood(entry.toEntity())
    }

    override suspend fun deleteMood(date: LocalDate) {
        moodDao.deleteMoodByDate(date.toString())
    }

    override suspend fun deleteAllMoods() {
        moodDao.deleteAllMoods()
    }
}