package com.example.cyberquiz.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(defaultValue = "'CYBERSECURITY'")
    val quizType: String = "CYBERSECURITY",
    val xp: Int = 0,
    val level: Int = 1,
    val answered: Int = 0,
    val correct: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val totalResponseMs: Long = 0
)
