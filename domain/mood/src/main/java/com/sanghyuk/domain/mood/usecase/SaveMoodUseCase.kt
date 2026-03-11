package com.sanghyuk.domain.mood.usecase

import com.sanghyuk.domain.mood.model.MoodEntry
import com.sanghyuk.domain.mood.repository.MoodRepository

class SaveMoodUseCase(
    private val repository: MoodRepository,
) {
    suspend operator fun invoke(entry: MoodEntry) = repository.saveMood(entry)
}
