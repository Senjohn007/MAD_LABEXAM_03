package com.example.wellnesstracker.models

import java.util.*

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val category: String = "General",
    val targetDaily: Int = 1,
    val isActive: Boolean = true,
    val createdDate: Long = System.currentTimeMillis(),
    val iconResourceId: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "category" to category,
            "targetDaily" to targetDaily,
            "isActive" to isActive,
            "createdDate" to createdDate,
            "iconResourceId" to iconResourceId
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Habit {
            return Habit(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                category = map["category"] as? String ?: "General",
                targetDaily = (map["targetDaily"] as? Number)?.toInt() ?: 1,
                isActive = map["isActive"] as? Boolean ?: true,
                createdDate = (map["createdDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                iconResourceId = (map["iconResourceId"] as? Number)?.toInt() ?: 0
            )
        }
    }
}
