package com.example.wellnesstracker.models

import java.util.*

data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: String, // Format: yyyy-MM-dd
    val time: String, // Format: HH:mm
    val moodEmoji: String,
    val moodLevel: Int, // 1-5 scale
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "date" to date,
            "time" to time,
            "moodEmoji" to moodEmoji,
            "moodLevel" to moodLevel,
            "notes" to notes,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): MoodEntry {
            return MoodEntry(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                date = map["date"] as? String ?: "",
                time = map["time"] as? String ?: "",
                moodEmoji = map["moodEmoji"] as? String ?: "😐",
                moodLevel = (map["moodLevel"] as? Number)?.toInt() ?: 3,
                notes = map["notes"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
