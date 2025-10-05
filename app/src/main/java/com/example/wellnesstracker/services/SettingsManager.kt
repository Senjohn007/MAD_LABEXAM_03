package com.example.wellnesstracker.services

import android.content.Context

data class UserSettings(
    val userName: String = "User",
    val dailyWaterGoal: Int = 2500,
    val stepGoal: Int = 10000,
    val isHydrationReminderEnabled: Boolean = true,
    val reminderInterval: Int = 2 // hours
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userName" to userName,
            "dailyWaterGoal" to dailyWaterGoal,
            "stepGoal" to stepGoal,
            "isHydrationReminderEnabled" to isHydrationReminderEnabled,
            "reminderInterval" to reminderInterval
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): UserSettings {
            return UserSettings(
                userName = map["userName"] as? String ?: "User",
                dailyWaterGoal = (map["dailyWaterGoal"] as? Number)?.toInt() ?: 2500,
                stepGoal = (map["stepGoal"] as? Number)?.toInt() ?: 10000,
                isHydrationReminderEnabled = map["isHydrationReminderEnabled"] as? Boolean ?: true,
                reminderInterval = (map["reminderInterval"] as? Number)?.toInt() ?: 2
            )
        }
    }
}

class SettingsManager(context: Context) : DataManager(context) {
    override val preferencesName = "user_settings_prefs"

    companion object {
        private const val USER_SETTINGS_KEY = "user_settings"
        private const val FIRST_TIME_USER_KEY = "first_time_user"
        private const val DAILY_WATER_GOAL_KEY = "daily_water_goal"
        private const val STEP_GOAL_KEY = "step_goal"
        private const val REMINDER_INTERVAL_KEY = "reminder_interval"
        private const val REMINDER_ENABLED_KEY = "reminder_enabled"
        private const val USER_NAME_KEY = "user_name"
    }

    fun getUserSettings(): UserSettings {
        return try {
            val settingsMap = getList<Map<String, Any>>(USER_SETTINGS_KEY).firstOrNull()
            if (settingsMap != null) {
                UserSettings.fromMap(settingsMap)
            } else {
                // Fallback to individual preferences for backward compatibility
                UserSettings(
                    userName = getString(USER_NAME_KEY, "User"),
                    dailyWaterGoal = getInt(DAILY_WATER_GOAL_KEY, 2500),
                    stepGoal = getInt(STEP_GOAL_KEY, 10000),
                    isHydrationReminderEnabled = getBoolean(REMINDER_ENABLED_KEY, true),
                    reminderInterval = getInt(REMINDER_INTERVAL_KEY, 2)
                )
            }
        } catch (e: Exception) {
            // Return default settings if any error occurs
            UserSettings()
        }
    }

    fun saveUserSettings(settings: UserSettings) {
        try {
            // Save as a list for future extensibility
            saveList(USER_SETTINGS_KEY, listOf(settings.toMap()))

            // Also save individual values for backward compatibility
            saveString(USER_NAME_KEY, settings.userName)
            saveInt(DAILY_WATER_GOAL_KEY, settings.dailyWaterGoal)
            saveInt(STEP_GOAL_KEY, settings.stepGoal)
            saveBoolean(REMINDER_ENABLED_KEY, settings.isHydrationReminderEnabled)
            saveInt(REMINDER_INTERVAL_KEY, settings.reminderInterval)

            // Mark that user is no longer first time user
            saveBoolean(FIRST_TIME_USER_KEY, false)
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }

    fun isFirstTimeUser(): Boolean {
        return getBoolean(FIRST_TIME_USER_KEY, true)
    }

    fun updateDailyWaterGoal(goal: Int) {
        val currentSettings = getUserSettings()
        val updatedSettings = currentSettings.copy(dailyWaterGoal = goal)
        saveUserSettings(updatedSettings)
    }

    fun updateStepGoal(goal: Int) {
        val currentSettings = getUserSettings()
        val updatedSettings = currentSettings.copy(stepGoal = goal)
        saveUserSettings(updatedSettings)
    }

    fun updateUserName(name: String) {
        val currentSettings = getUserSettings()
        val updatedSettings = currentSettings.copy(userName = name)
        saveUserSettings(updatedSettings)
    }

    fun updateReminderInterval(intervalHours: Int) {
        val currentSettings = getUserSettings()
        val updatedSettings = currentSettings.copy(reminderInterval = intervalHours)
        saveUserSettings(updatedSettings)
    }

    fun updateReminderEnabled(enabled: Boolean) {
        val currentSettings = getUserSettings()
        val updatedSettings = currentSettings.copy(isHydrationReminderEnabled = enabled)
        saveUserSettings(updatedSettings)
    }
}
