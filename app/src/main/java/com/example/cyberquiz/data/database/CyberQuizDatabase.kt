package com.example.cyberquiz.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        QuestionEntity::class,
        ProgressEntity::class,
        ReviewItemEntity::class,
        CategoryProgressEntity::class,
        ConceptProgressEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class CyberQuizDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao

    companion object {
        @Volatile
        private var INSTANCE: CyberQuizDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE questions ADD COLUMN quizType TEXT NOT NULL DEFAULT 'CYBERSECURITY'"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE progress ADD COLUMN quizType TEXT NOT NULL DEFAULT 'CYBERSECURITY'"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS review_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        quizType TEXT NOT NULL,
                        concept TEXT NOT NULL,
                        category TEXT NOT NULL,
                        difficulty TEXT NOT NULL,
                        questionId INTEGER NOT NULL,
                        question TEXT NOT NULL,
                        correctAnswer TEXT NOT NULL,
                        wrongCount INTEGER NOT NULL,
                        correctAfterWrongCount INTEGER NOT NULL,
                        mastered INTEGER NOT NULL,
                        lastWrongAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_review_items_quizType_concept ON review_items (quizType, concept)"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS category_progress (
                        quizType TEXT NOT NULL,
                        category TEXT NOT NULL,
                        answered INTEGER NOT NULL,
                        correct INTEGER NOT NULL,
                        lastAnsweredAt INTEGER NOT NULL,
                        PRIMARY KEY(quizType, category)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS concept_progress (
                        quizType TEXT NOT NULL,
                        concept TEXT NOT NULL,
                        category TEXT NOT NULL,
                        attempts INTEGER NOT NULL,
                        correct INTEGER NOT NULL,
                        reviewMastered INTEGER NOT NULL,
                        lastResultCorrect INTEGER NOT NULL,
                        lastAnsweredAt INTEGER NOT NULL,
                        PRIMARY KEY(quizType, concept)
                    )
                    """.trimIndent()
                )
            }
        }

        fun get(context: Context): CyberQuizDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context,
                CyberQuizDatabase::class.java,
                "cyberquiz.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                .also { INSTANCE = it }
        }
    }
}
