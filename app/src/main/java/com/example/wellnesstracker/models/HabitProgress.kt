package com.example.wellnesstracker.models

import java.util.*

data class HabitProgress(
    val habitId: String,
    val date: String, // Format: yyyy-MM-dd
    val isCompleted: Boolean = false,
    val completionTime: Long? = null,
    val progress: Int = 0, // For habits that can be partially completed
    val notes: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "habitId" to habitId,
            "date" to date,
            "isCompleted" to isCompleted,
            "completionTime" to (completionTime ?: 0L),
            "progress" to progress,
            "notes" to notes
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): HabitProgress {
            return HabitProgress(
                habitId = map["habitId"] as? String ?: "",
                date = map["date"] as? String ?: "",
                isCompleted = map["isCompleted"] as? Boolean ?: false,
                completionTime = (map["completionTime"] as? Number)?.toLong(),
                progress = (map["progress"] as? Number)?.toInt() ?: 0,
                notes = map["notes"] as? String ?: ""
            )
        }
    }
}
