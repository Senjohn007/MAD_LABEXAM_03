package com.example.wellnesstracker.models

import java.util.*

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val category: String = "General", // Added missing category property
    val isActive: Boolean = true
) {
    companion object {
        fun fromMap(map: Map<String, Any>): Habit {
            return Habit(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                category = map["category"] as? String ?: "General", // Added category mapping
                isActive = map["isActive"] as? Boolean ?: true
            )
        }
    }
}
