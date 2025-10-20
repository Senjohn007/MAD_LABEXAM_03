package com.example.wellnesstracker.models

import java.util.*

data class HabitProgress(
    val habitId: String,
    val date: String,
    val isCompleted: Boolean,
    val completionTime: Long
) {
    companion object {
        fun fromMap(map: Map<String, Any>): HabitProgress {
            return HabitProgress(
                habitId = map["habitId"] as? String ?: "",
                date = map["date"] as? String ?: "",
                isCompleted = map["isCompleted"] as? Boolean ?: false,
                completionTime = (map["completionTime"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}
