package com.example.wellnesstracker.models

data class StepData(
    val date: String, // Format: yyyy-MM-dd
    val stepCount: Int,
    val distance: Float = 0f, // Distance in km
    val calories: Int = 0,
    val activeTime: Int = 0, // Active time in minutes
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "date" to date,
            "stepCount" to stepCount,
            "distance" to distance,
            "calories" to calories,
            "activeTime" to activeTime,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): StepData {
            return StepData(
                date = map["date"] as? String ?: "",
                stepCount = (map["stepCount"] as? Number)?.toInt() ?: 0,
                distance = (map["distance"] as? Number)?.toFloat() ?: 0f,
                calories = (map["calories"] as? Number)?.toInt() ?: 0,
                activeTime = (map["activeTime"] as? Number)?.toInt() ?: 0,
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
