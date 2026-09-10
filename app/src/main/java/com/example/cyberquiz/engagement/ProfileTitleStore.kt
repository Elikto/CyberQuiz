package com.example.cyberquiz.engagement

import android.content.Context
import com.example.cyberquiz.model.ProfileTitleDefinition
import com.example.cyberquiz.model.effectiveProfileTitle
import com.example.cyberquiz.model.profileTitleDefinitions

internal object ProfileTitleStore {
    private const val PREFS = "cyberquiz_profile_titles"
    private const val KEY_SELECTED_TITLE = "selected_title"

    fun selectedTitleId(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_TITLE, null)

    fun selectTitle(
        context: Context,
        titleId: String,
        unlockedAchievementIds: Set<String>
    ): Boolean {
        val definition = profileTitleDefinitions.firstOrNull { it.id == titleId } ?: return false
        if (definition.achievementId !in unlockedAchievementIds) return false
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_TITLE, definition.id)
            .apply()
        return true
    }

    fun effectiveTitle(
        context: Context,
        unlockedAchievementIds: Set<String>
    ): ProfileTitleDefinition? = effectiveProfileTitle(
        selectedTitleId = selectedTitleId(context),
        unlockedAchievementIds = unlockedAchievementIds
    )
}