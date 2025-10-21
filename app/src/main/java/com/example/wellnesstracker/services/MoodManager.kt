package com.example.wellnesstracker.services

import android.content.Context
import android.util.Log
import com.example.wellnesstracker.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodManager(context: Context) : DataManager(context) {
    override val preferencesName = "mood_prefs"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    companion object {
        private const val MOOD_ENTRIES_KEY = "mood_entries"
        private const val TAG = "MoodManager"
    }

    fun saveMoodEntry(moodEntry: MoodEntry) {
        try {
            Log.d(TAG, "Saving mood entry: ${moodEntry.moodEmoji} - ${moodEntry.date} ${moodEntry.time}")

            val moods = getAllMoodEntries().toMutableList()
            val existingIndex = moods.indexOfFirst { it.id == moodEntry.id }

            if (existingIndex >= 0) {
                moods[existingIndex] = moodEntry
                Log.d(TAG, "Updated existing mood entry at index $existingIndex")
            } else {
                moods.add(moodEntry)
                Log.d(TAG, "Added new mood entry")
            }

            // FIXED: Don't sort here - let the UI handle sorting for display
            // Remove this line: moods.sortByDescending { it.timestamp }
            saveList(MOOD_ENTRIES_KEY, moods)

            Log.d(TAG, "Mood entry saved successfully. Total entries: ${moods.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving mood entry", e)
            throw e
        }
    }

    fun getAllMoodEntries(): List<MoodEntry> {
        return try {
            val entries = getList<Map<String, Any>>(MOOD_ENTRIES_KEY).map { MoodEntry.fromMap(it) }
            Log.d(TAG, "Retrieved ${entries.size} mood entries from storage")
            entries
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving mood entries", e)
            emptyList()
        }
    }

    fun getMoodEntriesForDate(date: String): List<MoodEntry> {
        return getAllMoodEntries().filter { it.date == date }
            .sortedWith { entry1, entry2 ->
                // Sort by time chronologically for the same date
                try {
                    val time1 = entry1.time.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
                    val time2 = entry2.time.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
                    time1.compareTo(time2)
                } catch (e: Exception) {
                    Log.e(TAG, "Error sorting by time", e)
                    entry1.timestamp.compareTo(entry2.timestamp)
                }
            }
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
        }.sortedWith { entry1, entry2 ->
            // First sort by date
            val dateComparison = entry1.date.compareTo(entry2.date)
            if (dateComparison != 0) return@sortedWith dateComparison

            // Then sort by time
            try {
                val time1 = entry1.time.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
                val time2 = entry2.time.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
                time1.compareTo(time2)
            } catch (e: Exception) {
                entry1.timestamp.compareTo(entry2.timestamp)
            }
        }
    }

    fun deleteMoodEntry(id: String) {
        try {
            Log.d(TAG, "Deleting mood entry with id: $id")
            val moods = getAllMoodEntries().filter { it.id != id }
            saveList(MOOD_ENTRIES_KEY, moods)
            Log.d(TAG, "Mood entry deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting mood entry", e)
            throw e
        }
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

    fun debugPrintAllEntries() {
        try {
            val entries = getAllMoodEntries()
            Log.d(TAG, "=== All Mood Entries (${entries.size}) ===")
            entries.forEachIndexed { index, entry ->
                Log.d(TAG, "$index: ${entry.moodEmoji} - ${entry.date} ${entry.time} - '${entry.notes}' (Level: ${entry.moodLevel}, ID: ${entry.id})")
            }
            Log.d(TAG, "=== End Debug ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error in debugPrintAllEntries", e)
        }
    }

    // Additional helper method for debugging storage
    fun getStorageStats(): String {
        return try {
            val entries = getAllMoodEntries()
            val totalEntries = entries.size
            val todayEntries = getTodayMoodEntries().size
            val weeklyEntries = getWeeklyMoodEntries().size

            "Storage Stats - Total: $totalEntries, Today: $todayEntries, This Week: $weeklyEntries"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting storage stats", e)
            "Error retrieving storage stats"
        }
    }

    // Helper method to clear all mood data (for testing purposes)
    fun clearAllMoodData() {
        try {
            Log.d(TAG, "Clearing all mood data")
            saveList(MOOD_ENTRIES_KEY, emptyList<MoodEntry>())
            Log.d(TAG, "All mood data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing mood data", e)
            throw e
        }
    }

    fun getDetailedMoodTrendData(days: Int = 7): List<Pair<String, Float>> {
        val calendar = Calendar.getInstance()
        val trendData = mutableListOf<Pair<String, Float>>()

        // Define 6-hour intervals: 0-6, 6-12, 12-18, 18-24
        val timeIntervals = listOf(
            Pair(6, 12),  // Morning (06:00-12:00)
            Pair(12, 18), // Afternoon (12:00-18:00)
            Pair(18, 24)  // Evening (18:00-24:00)
        )

        val intervalLabels = listOf("Morning", "Afternoon", "Evening")

        // Process days in reverse chronological order (oldest to newest for chart)
        for (day in (days - 1) downTo 0) {
            val tempCalendar = Calendar.getInstance()
            tempCalendar.add(Calendar.DAY_OF_YEAR, -day)
            val date = dateFormat.format(tempCalendar.time)
            val dayEntries = getMoodEntriesForDate(date)

            // Process each time interval in chronological order within the day
            timeIntervals.forEachIndexed { index, (startHour, endHour) ->
                val intervalEntries = dayEntries.filter { entry ->
                    val entryHour = getHourFromTime(entry.time)
                    entryHour >= startHour && entryHour < endHour
                }

                val avgMood = if (intervalEntries.isNotEmpty()) {
                    intervalEntries.sumOf { it.moodLevel }.toFloat() / intervalEntries.size
                } else {
                    0f // No mood data for this interval
                }

                // Only add non-zero data points to avoid empty intervals
                if (avgMood > 0f) {
                    // Create a label like "10-20 Morning" or "10-19 Evening"
                    val dateLabel = if (date.length >= 10) {
                        "${date.substring(5, 7)}-${date.substring(8, 10)}"
                    } else {
                        date
                    }
                    val intervalLabel = "$dateLabel ${intervalLabels[index]}"

                    trendData.add(Pair(intervalLabel, avgMood))

                    Log.d(TAG, "Added chart data: $intervalLabel -> $avgMood")
                }
            }
        }

        Log.d(TAG, "Generated ${trendData.size} chart data points in chronological order")
        return trendData
    }

    /**
     * Extract hour from time string (HH:mm format)
     */
    private fun getHourFromTime(timeString: String): Int {
        return try {
            val parts = timeString.split(":")
            parts[0].toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time: $timeString", e)
            0
        }
    }

    /**
     * Get mood entries for today grouped by 6-hour intervals
     */
    fun getTodayMoodsByInterval(): Map<String, List<MoodEntry>> {
        val today = dateFormat.format(Date())
        val todayEntries = getMoodEntriesForDate(today)

        val intervalMap = mutableMapOf<String, MutableList<MoodEntry>>()
        val intervalLabels = listOf("Night (00-06)", "Morning (06-12)", "Afternoon (12-18)", "Evening (18-24)")
        val timeRanges = listOf(0 to 6, 6 to 12, 12 to 18, 18 to 24)

        // Initialize all intervals
        intervalLabels.forEach { intervalMap[it] = mutableListOf() }

        todayEntries.forEach { entry ->
            val hour = getHourFromTime(entry.time)
            val intervalIndex = when (hour) {
                in 0..5 -> 0
                in 6..11 -> 1
                in 12..17 -> 2
                in 18..23 -> 3
                else -> 1 // Default to morning
            }

            intervalMap[intervalLabels[intervalIndex]]?.add(entry)
        }

        return intervalMap.mapValues { it.value.toList() }
    }

    /**
     * Get statistics for current day intervals
     */
    fun getTodayIntervalStats(): String {
        val intervalData = getTodayMoodsByInterval()
        val stats = intervalData.map { (interval, entries) ->
            "$interval: ${entries.size} entries"
        }.joinToString(", ")

        return "Today's Mood Distribution - $stats"
    }
}
