package com.example.wellnesstracker.models

import java.util.*

data class HabitCompletion(
    val habitId: String,
    val date: String, // "yyyy-MM-dd" format
    val isCompleted: Boolean,
    val completedAt: Long = System.currentTimeMillis() // Changed from completionTime to completedAt
) {
    companion object {
        fun fromMap(map: Map<String, Any>): HabitCompletion {
            return HabitCompletion(
                habitId = map["habitId"] as? String ?: "",
                date = map["date"] as? String ?: "",
                isCompleted = map["isCompleted"] as? Boolean ?: false,
                completedAt = (map["completedAt"] as? Number)?.toLong() ?: 0L // Match property name
            )
        }
    }
}
