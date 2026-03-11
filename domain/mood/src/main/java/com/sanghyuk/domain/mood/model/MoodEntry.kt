package com.sanghyuk.domain.mood.model

import java.time.LocalDate

data class MoodEntry(
    val date: LocalDate,
    val moodType: MoodType,
)
