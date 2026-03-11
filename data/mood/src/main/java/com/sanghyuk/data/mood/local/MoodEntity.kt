package com.sanghyuk.data.mood.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntity(
    @PrimaryKey val date: String,
    val moodType: String,
)
