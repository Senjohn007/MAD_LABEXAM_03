package com.example.wellnesstracker

import java.util.*

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var description: String,
    var color: String = "#6B73FF",
    val createdDate: Long = System.currentTimeMillis(),
    var isCompleted: Boolean = false,
    val completedDates: MutableSet<String> = mutableSetOf()
)

data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String,
    val label: String,
    val moodValue: Int, // 1-8 scale
    var note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class HydrationSettings(
    var intervalMinutes: Int = 120,
    var isEnabled: Boolean = true,
    var startTime: String = "08:00",
    var endTime: String = "22:00"
)
