package com.example.cyberquiz.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM questions WHERE quizType = :quizType AND seen = 0 ORDER BY id LIMIT 1")
    suspend fun nextUnseen(quizType: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE quizType = :quizType AND category = :category AND seen = 0 ORDER BY id LIMIT 1")
    suspend fun nextUnseenInCategory(quizType: String, category: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun questionById(id: Long): QuestionEntity?

    @Query("UPDATE questions SET seen = 1 WHERE id = :id")
    suspend fun markSeen(id: Long)

    @Query("SELECT COUNT(*) FROM questions WHERE quizType = :quizType")
    suspend fun questionCount(quizType: String): Int

    @Query("SELECT COUNT(*) FROM questions WHERE quizType = :quizType AND source = :source")
    suspend fun questionCountBySource(quizType: String, source: String): Int

    @Query("UPDATE questions SET seen = 0 WHERE quizType = :quizType")
    suspend fun resetSeen(quizType: String)

    @Query("UPDATE questions SET seen = 0 WHERE quizType = :quizType AND category = :category")
    suspend fun resetSeenInCategory(quizType: String, category: String)

    @Insert
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("SELECT * FROM progress WHERE quizType = :quizType LIMIT 1")
    fun progress(quizType: String): Flow<ProgressEntity?>

    @Query("SELECT * FROM progress WHERE quizType = :quizType LIMIT 1")
    suspend fun progressSnapshot(quizType: String): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProgress(progress: ProgressEntity)

    @Query("UPDATE progress SET xp=:xp, level=:level, answered=:answered, correct=:correct, streak=:streak, bestStreak=:bestStreak, totalResponseMs=:totalResponseMs WHERE quizType=:quizType")
    suspend fun updateProgress(
        quizType: String,
        xp: Int,
        level: Int,
        answered: Int,
        correct: Int,
        streak: Int,
        bestStreak: Int,
        totalResponseMs: Long
    )

    @Query("SELECT * FROM review_items WHERE quizType = :quizType ORDER BY mastered ASC, wrongCount DESC, lastWrongAt DESC")
    fun reviewItems(quizType: String): Flow<List<ReviewItemEntity>>

    @Query("SELECT * FROM review_items WHERE quizType = :quizType AND concept = :concept LIMIT 1")
    suspend fun reviewItemSnapshot(quizType: String, concept: String): ReviewItemEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReviewItem(item: ReviewItemEntity)

    @Query(
        "UPDATE review_items SET category=:category, difficulty=:difficulty, questionId=:questionId, question=:question, correctAnswer=:correctAnswer, wrongCount=wrongCount+1, mastered=0, lastWrongAt=:lastWrongAt WHERE quizType=:quizType AND concept=:concept"
    )
    suspend fun recordReviewWrong(
        quizType: String,
        concept: String,
        category: String,
        difficulty: String,
        questionId: Long,
        question: String,
        correctAnswer: String,
        lastWrongAt: Long
    )

    @Query(
        "UPDATE review_items SET correctAfterWrongCount=correctAfterWrongCount+1, mastered=1 WHERE quizType=:quizType AND concept=:concept"
    )
    suspend fun recordReviewCorrect(quizType: String, concept: String)
}
