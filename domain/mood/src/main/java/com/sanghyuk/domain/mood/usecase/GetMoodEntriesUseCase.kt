package com.sanghyuk.domain.mood.usecase

import com.sanghyuk.domain.mood.model.MoodEntry
import com.sanghyuk.domain.mood.repository.MoodRepository

class GetMoodEntriesUseCase(
    private val repository: MoodRepository,
) {
    suspend operator fun invoke(): List<MoodEntry> = repository.getMoodEntries()
}
