package com.example.wellnesstracker.services

import android.content.Context
import com.example.wellnesstracker.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodManager(context: Context) : DataManager(context) {
    override val preferencesName = "mood_prefs"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    companion object {
        private const val MOOD_ENTRIES_KEY = "mood_entries"
    }

    fun saveMoodEntry(moodEntry: MoodEntry) {
        val moods = getAllMoodEntries().toMutableList()
        val existingIndex = moods.indexOfFirst { it.id == moodEntry.id }

        if (existingIndex >= 0) {
            moods[existingIndex] = moodEntry
        } else {
            moods.add(moodEntry)
        }

        // Sort by timestamp (most recent first)
        moods.sortByDescending { it.timestamp }
        saveList(MOOD_ENTRIES_KEY, moods)
    }

    fun getAllMoodEntries(): List<MoodEntry> {
        return getList<Map<String, Any>>(MOOD_ENTRIES_KEY).map { MoodEntry.fromMap(it) }
    }

    fun getMoodEntriesForDate(date: String): List<MoodEntry> {
        return getAllMoodEntries().filter { it.date == date }
    }

    fun getTodayMoodEntries(): List<MoodEntry> {
        val today = dateFormat.format(Date())
        return getMoodEntriesForDate(today)
    }

    fun getWeeklyMoodEntries(): List<MoodEntry> {
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = dateFormat.format(calendar.time)

        return getAllMoodEntries().filter { entry ->
            entry.date >= startDate && entry.date <= endDate
        }
    }

    fun deleteMoodEntry(id: String) {
        val moods = getAllMoodEntries().filter { it.id != id }
        saveList(MOOD_ENTRIES_KEY, moods)
    }

    fun getMoodTrendData(days: Int = 7): List<Pair<String, Float>> {
        val calendar = Calendar.getInstance()
        val trendData = mutableListOf<Pair<String, Float>>()

        for (i in 0 until days) {
            val date = dateFormat.format(calendar.time)
            val entries = getMoodEntriesForDate(date)
            val avgMood = if (entries.isNotEmpty()) {
                entries.sumOf { it.moodLevel }.toFloat() / entries.size
            } else 0f

            trendData.add(0, Pair(date, avgMood)) // Add at beginning for chronological order
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return trendData
    }

    fun addQuickMood(moodEmoji: String, moodLevel: Int) {
        val now = Date()
        val moodEntry = MoodEntry(
            date = dateFormat.format(now),
            time = timeFormat.format(now),
            moodEmoji = moodEmoji,
            moodLevel = moodLevel,
            notes = "Quick mood entry"
        )

        saveMoodEntry(moodEntry)
    }
}
