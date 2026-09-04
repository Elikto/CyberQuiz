package com.example.cyberquiz.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [QuestionEntity::class, ProgressEntity::class], version = 1, exportSchema = true)
abstract class CyberQuizDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao

    companion object {
        @Volatile private var INSTANCE: CyberQuizDatabase? = null
        fun get(context: Context): CyberQuizDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context, CyberQuizDatabase::class.java, "cyberquiz.db").build().also { INSTANCE = it }
        }
    }
}
