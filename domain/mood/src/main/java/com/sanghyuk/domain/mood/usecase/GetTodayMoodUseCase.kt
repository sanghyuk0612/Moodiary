package com.sanghyuk.domain.mood.usecase

import com.sanghyuk.domain.mood.model.MoodEntry
import com.sanghyuk.domain.mood.repository.MoodRepository

class GetTodayMoodUseCase(
    private val repository: MoodRepository,
) {
    suspend operator fun invoke(): MoodEntry? = repository.getTodayMood()
}
