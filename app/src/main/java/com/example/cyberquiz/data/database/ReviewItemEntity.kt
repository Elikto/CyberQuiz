package com.example.cyberquiz.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_items",
    indices = [Index(value = ["quizType", "concept"], unique = true)]
)
data class ReviewItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quizType: String,
    val concept: String,
    val category: String,
    val difficulty: String,
    val questionId: Long,
    val question: String,
    val correctAnswer: String,
    val wrongCount: Int = 1,
    val correctAfterWrongCount: Int = 0,
    val mastered: Boolean = false,
    val lastWrongAt: Long = 0L
)
