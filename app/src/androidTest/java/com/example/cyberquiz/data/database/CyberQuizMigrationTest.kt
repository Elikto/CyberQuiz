package com.example.cyberquiz.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CyberQuizMigrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation,
        CyberQuizDatabase::class.java
    )

    @Test
    fun migrate1To5_preservesLegacyDataAndValidatesSchema() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """
                INSERT INTO questions (
                    id, category, difficulty, question,
                    answerA, answerB, answerC, answerD,
                    correctIndex, explanation, source, seen
                ) VALUES (
                    1, 'Réseau', 'MEDIUM', 'Question historique ?',
                    'A', 'B', 'C', 'D',
                    2, 'Explication historique', 'seed', 1
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO progress (
                    id, xp, level, answered, correct,
                    streak, bestStreak, totalResponseMs
                ) VALUES (1, 120, 2, 10, 7, 3, 5, 42000)
                """.trimIndent()
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            TEST_DB,
            5,
            true,
            *CyberQuizDatabase.ALL_MIGRATIONS
        )

        migrated.query(
            """
            SELECT quizType, category, difficulty, question, correctIndex,
                   explanation, source, seen
            FROM questions
            WHERE id = 1
            """.trimIndent()
        ).use { cursor ->
            assertTrue("La question historique doit être conservée", cursor.moveToFirst())
            assertEquals("CYBERSECURITY", cursor.getString(0))
            assertEquals("Réseau", cursor.getString(1))
            assertEquals("MEDIUM", cursor.getString(2))
            assertEquals("Question historique ?", cursor.getString(3))
            assertEquals(2, cursor.getInt(4))
            assertEquals("Explication historique", cursor.getString(5))
            assertEquals("seed", cursor.getString(6))
            assertEquals(1, cursor.getInt(7))
        }

        migrated.query(
            """
            SELECT quizType, xp, level, answered, correct,
                   streak, bestStreak, totalResponseMs
            FROM progress
            WHERE id = 1
            """.trimIndent()
        ).use { cursor ->
            assertTrue("La progression historique doit être conservée", cursor.moveToFirst())
            assertEquals("CYBERSECURITY", cursor.getString(0))
            assertEquals(120, cursor.getInt(1))
            assertEquals(2, cursor.getInt(2))
            assertEquals(10, cursor.getInt(3))
            assertEquals(7, cursor.getInt(4))
            assertEquals(3, cursor.getInt(5))
            assertEquals(5, cursor.getInt(6))
            assertEquals(42000L, cursor.getLong(7))
        }

        migrated.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name IN ('review_items', 'category_progress', 'concept_progress')"
        ).use { cursor ->
            assertEquals(3, cursor.count)
        }

        migrated.close()
    }

    private companion object {
        const val TEST_DB = "cyberquiz-migration-test"
    }
}
