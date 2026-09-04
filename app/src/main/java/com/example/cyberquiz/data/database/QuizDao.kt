package com.example.cyberquiz.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM questions WHERE quizType = :quizType AND seen = 0 ORDER BY id LIMIT 1")
    suspend fun nextUnseen(quizType: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE quizType = :quizType AND category = :category AND seen = 0 ORDER BY id LIMIT 1")
    suspend fun nextUnseenInCategory(quizType: String, category: String): QuestionEntity?

    @Query("UPDATE questions SET seen = 1 WHERE id = :id")
    suspend fun markSeen(id: Long)

    @Query("SELECT COUNT(*) FROM questions WHERE quizType = :quizType")
    suspend fun questionCount(quizType: String): Int

    @Query("UPDATE questions SET seen = 0 WHERE quizType = :quizType")
    suspend fun resetSeen(quizType: String)

    @Query("UPDATE questions SET seen = 0 WHERE quizType = :quizType AND category = :category")
    suspend fun resetSeenInCategory(quizType: String, category: String)

    @Insert
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("SELECT * FROM progress WHERE id = 1")
    fun progress(): Flow<ProgressEntity?>

    @Query("SELECT * FROM progress WHERE id = 1 LIMIT 1")
    suspend fun progressSnapshot(): ProgressEntity?

    @Insert
    suspend fun insertProgress(progress: ProgressEntity)

    @Query("UPDATE progress SET xp=:xp, level=:level, answered=:answered, correct=:correct, streak=:streak, bestStreak=:bestStreak, totalResponseMs=:totalResponseMs WHERE id=1")
    suspend fun updateProgress(
        xp: Int,
        level: Int,
        answered: Int,
        correct: Int,
        streak: Int,
        bestStreak: Int,
        totalResponseMs: Long
    )
}
