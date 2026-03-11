package com.sanghyuk.domain.mood.usecase

import com.sanghyuk.domain.mood.repository.MoodRepository
import java.time.LocalDate

class DeleteMoodUseCase(
    private val repository: MoodRepository,
) {
    suspend operator fun invoke(date: LocalDate) = repository.deleteMood(date)
}