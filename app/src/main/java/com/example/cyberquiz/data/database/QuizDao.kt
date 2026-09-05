package com.example.cyberquiz.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM questions WHERE quizType = :quizType AND seen = 0 ORDER BY id LIMIT 1")
    suspend fun nextUnseen(quizType: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE quizType = :quizType AND category = :category AND seen = 0 ORDER BY id LIMIT 1")
    suspend fun nextUnseenInCategory(quizType: String, category: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE quizType = :quizType ORDER BY id")
    suspend fun questionsSnapshot(quizType: String): List<QuestionEntity>

    @Query(
        "SELECT q.* FROM questions q INNER JOIN review_items r ON r.questionId = q.id WHERE r.quizType = :quizType AND r.mastered = 0 ORDER BY r.wrongCount DESC, r.lastWrongAt DESC"
    )
    suspend fun activeReviewQuestionsSnapshot(quizType: String): List<QuestionEntity>

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

    @Query(
        "UPDATE questions SET question=:newQuestion, answerA=:answerA, answerB=:answerB, answerC=:answerC, answerD=:answerD, explanation=:explanation WHERE quizType=:quizType AND question=:oldQuestion"
    )
    suspend fun rewriteQuestion(
        quizType: String,
        oldQuestion: String,
        newQuestion: String,
        answerA: String,
        answerB: String,
        answerC: String,
        answerD: String,
        explanation: String
    )

    @Query(
        "UPDATE review_items SET question=:newQuestion, concept=:newCorrectAnswer, correctAnswer=:newCorrectAnswer WHERE quizType=:quizType AND question=:oldQuestion"
    )
    suspend fun rewriteReviewItem(
        quizType: String,
        oldQuestion: String,
        newQuestion: String,
        newCorrectAnswer: String
    )

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

    @Transaction
    @Query("SELECT * FROM review_items WHERE quizType = :quizType ORDER BY mastered ASC, wrongCount DESC, lastWrongAt DESC")
    fun reviewItemsWithQuestions(quizType: String): Flow<List<ReviewItemWithQuestion>>

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

    @Query("SELECT * FROM category_progress WHERE quizType = :quizType ORDER BY answered DESC, category ASC")
    fun categoryProgress(quizType: String): Flow<List<CategoryProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategoryProgress(item: CategoryProgressEntity)

    @Query(
        "UPDATE category_progress SET answered=answered+1, correct=correct+:correctIncrement, lastAnsweredAt=:timestamp WHERE quizType=:quizType AND category=:category"
    )
    suspend fun incrementCategoryProgress(
        quizType: String,
        category: String,
        correctIncrement: Int,
        timestamp: Long
    )

    @Query("SELECT * FROM concept_progress WHERE quizType = :quizType ORDER BY lastAnsweredAt DESC")
    fun conceptProgress(quizType: String): Flow<List<ConceptProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConceptProgress(item: ConceptProgressEntity)

    @Query(
        "UPDATE concept_progress SET category=:category, attempts=attempts+1, correct=correct+:correctIncrement, reviewMastered=:reviewMastered, lastResultCorrect=:lastResultCorrect, lastAnsweredAt=:timestamp WHERE quizType=:quizType AND concept=:concept"
    )
    suspend fun incrementConceptProgress(
        quizType: String,
        concept: String,
        category: String,
        correctIncrement: Int,
        reviewMastered: Boolean,
        lastResultCorrect: Boolean,
        timestamp: Long
    )

    @Query(
        "UPDATE concept_progress SET reviewMastered=1, lastResultCorrect=1, lastAnsweredAt=:timestamp WHERE quizType=:quizType AND concept=:concept"
    )
    suspend fun markConceptMasteredByReview(
        quizType: String,
        concept: String,
        timestamp: Long
    )

    @Query(
        "UPDATE concept_progress SET reviewMastered=0, lastResultCorrect=0, lastAnsweredAt=:timestamp WHERE quizType=:quizType AND concept=:concept"
    )
    suspend fun markConceptNeedsReview(
        quizType: String,
        concept: String,
        timestamp: Long
    )
}
