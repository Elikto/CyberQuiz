package com.example.cyberquiz.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(defaultValue = "'CYBERSECURITY'")
    val quizType: String = "CYBERSECURITY",
    val category: String,
    val difficulty: String,
    val question: String,
    val answerA: String,
    val answerB: String,
    val answerC: String,
    val answerD: String,
    val correctIndex: Int,
    val explanation: String,
    val source: String = "local",
    val seen: Boolean = false
)
