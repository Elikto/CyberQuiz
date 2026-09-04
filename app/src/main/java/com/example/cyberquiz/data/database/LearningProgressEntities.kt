package com.example.cyberquiz.data.database

import androidx.room.Entity

@Entity(
    tableName = "category_progress",
    primaryKeys = ["quizType", "category"]
)
data class CategoryProgressEntity(
    val quizType: String,
    val category: String,
    val answered: Int = 0,
    val correct: Int = 0,
    val lastAnsweredAt: Long = 0L
)

@Entity(
    tableName = "concept_progress",
    primaryKeys = ["quizType", "concept"]
)
data class ConceptProgressEntity(
    val quizType: String,
    val concept: String,
    val category: String,
    val attempts: Int = 0,
    val correct: Int = 0,
    val reviewMastered: Boolean = false,
    val lastResultCorrect: Boolean = false,
    val lastAnsweredAt: Long = 0L
)
