package com.example.cyberquiz.data.database

import androidx.room.Embedded
import androidx.room.Relation

data class ReviewItemWithQuestion(
    @Embedded
    val review: ReviewItemEntity,
    @Relation(
        parentColumn = "questionId",
        entityColumn = "id"
    )
    val question: QuestionEntity?
)
