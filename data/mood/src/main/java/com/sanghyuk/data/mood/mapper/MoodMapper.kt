package com.sanghyuk.data.mood.mapper

import com.sanghyuk.data.mood.local.MoodEntity
import com.sanghyuk.domain.mood.model.MoodEntry
import com.sanghyuk.domain.mood.model.MoodType
import java.time.LocalDate

fun MoodEntity.toDomain(): MoodEntry = MoodEntry(
    date = LocalDate.parse(date),
    moodType = MoodType.valueOf(moodType),
)

fun MoodEntry.toEntity(): MoodEntity = MoodEntity(
    date = date.toString(),
    moodType = moodType.name,
)
