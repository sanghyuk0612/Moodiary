package com.sanghyuk.domain.mood.usecase

import com.sanghyuk.domain.mood.repository.MoodRepository

class DeleteAllMoodsUseCase(
    private val repository: MoodRepository,
) {
    suspend operator fun invoke() = repository.deleteAllMoods()
}