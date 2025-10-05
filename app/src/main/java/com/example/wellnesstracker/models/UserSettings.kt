package com.example.wellnesstracker.models

data class UserSettings(
    val userName: String = "",
    val age: Int = 25,
    val weight: Float = 70f, // Weight in kg
    val height: Float = 170f, // Height in cm
    val activityLevel: String = "Moderate", // Low, Moderate, High
    val dailyWaterGoal: Int = 2000, // Daily water goal in ml
    val reminderInterval: Int = 60, // Reminder interval in minutes
    val isHydrationReminderEnabled: Boolean = true,
    val reminderStartTime: String = "08:00",
    val reminderEndTime: String = "22:00",
    val stepGoal: Int = 10000,
    val theme: String = "Light" // Light, Dark
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userName" to userName,
            "age" to age,
            "weight" to weight,
            "height" to height,
            "activityLevel" to activityLevel,
            "dailyWaterGoal" to dailyWaterGoal,
            "reminderInterval" to reminderInterval,
            "isHydrationReminderEnabled" to isHydrationReminderEnabled,
            "reminderStartTime" to reminderStartTime,
            "reminderEndTime" to reminderEndTime,
            "stepGoal" to stepGoal,
            "theme" to theme
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): UserSettings {
            return UserSettings(
                userName = map["userName"] as? String ?: "",
                age = (map["age"] as? Number)?.toInt() ?: 25,
                weight = (map["weight"] as? Number)?.toFloat() ?: 70f,
                height = (map["height"] as? Number)?.toFloat() ?: 170f,
                activityLevel = map["activityLevel"] as? String ?: "Moderate",
                dailyWaterGoal = (map["dailyWaterGoal"] as? Number)?.toInt() ?: 2000,
                reminderInterval = (map["reminderInterval"] as? Number)?.toInt() ?: 60,
                isHydrationReminderEnabled = map["isHydrationReminderEnabled"] as? Boolean ?: true,
                reminderStartTime = map["reminderStartTime"] as? String ?: "08:00",
                reminderEndTime = map["reminderEndTime"] as? String ?: "22:00",
                stepGoal = (map["stepGoal"] as? Number)?.toInt() ?: 10000,
                theme = map["theme"] as? String ?: "Light"
            )
        }
    }
}
