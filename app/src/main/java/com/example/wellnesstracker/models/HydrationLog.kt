package com.example.wellnesstracker.models

import java.util.*

data class HydrationLog(
    val id: String = UUID.randomUUID().toString(),
    val date: String, // Format: yyyy-MM-dd
    val amount: Int, // Amount in ml
    val time: String, // Format: HH:mm
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "date" to date,
            "amount" to amount,
            "time" to time,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): HydrationLog {
            return HydrationLog(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                date = map["date"] as? String ?: "",
                amount = (map["amount"] as? Number)?.toInt() ?: 0,
                time = map["time"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
