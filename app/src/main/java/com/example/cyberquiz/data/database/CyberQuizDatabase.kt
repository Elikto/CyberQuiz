package com.example.cyberquiz.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [QuestionEntity::class, ProgressEntity::class],
    version = 3,
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

        fun get(context: Context): CyberQuizDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context,
                CyberQuizDatabase::class.java,
                "cyberquiz.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also { INSTANCE = it }
        }
    }
}
