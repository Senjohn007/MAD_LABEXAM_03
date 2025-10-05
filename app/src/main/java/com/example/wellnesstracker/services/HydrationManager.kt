package com.example.wellnesstracker.services

import android.content.Context
import com.example.wellnesstracker.models.HydrationLog
import java.text.SimpleDateFormat
import java.util.*

class HydrationManager(context: Context) : DataManager(context) {
    override val preferencesName = "hydration_prefs"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    companion object {
        private const val HYDRATION_LOG_KEY = "hydration_log"
    }

    fun addHydrationEntry(amount: Int, date: String? = null, time: String? = null) {
        val now = Date()
        val entry = HydrationLog(
            date = date ?: dateFormat.format(now),
            amount = amount,
            time = time ?: timeFormat.format(now)
        )

        val logs = getAllHydrationLogs().toMutableList()
        logs.add(entry)
        logs.sortByDescending { it.timestamp }

        saveList(HYDRATION_LOG_KEY, logs)
    }

    fun getAllHydrationLogs(): List<HydrationLog> {
        return getList<Map<String, Any>>(HYDRATION_LOG_KEY).map { HydrationLog.fromMap(it) }
    }

    fun getHydrationLogsForDate(date: String): List<HydrationLog> {
        return getAllHydrationLogs().filter { it.date == date }
    }

    fun getTodayHydrationLogs(): List<HydrationLog> {
        val today = dateFormat.format(Date())
        return getHydrationLogsForDate(today)
    }

    fun getTodayWaterIntake(): Int {
        return getTodayHydrationLogs().sumOf { it.amount }
    }

    fun deleteHydrationEntry(entryId: String) {
        val logs = getAllHydrationLogs().filter { it.id != entryId }
        saveList(HYDRATION_LOG_KEY, logs)
    }

    fun getWeeklyHydrationData(): List<Pair<String, Int>> {
        val calendar = Calendar.getInstance()
        val weekData = mutableListOf<Pair<String, Int>>()

        for (i in 0 until 7) {
            val date = dateFormat.format(calendar.time)
            val intake = getHydrationLogsForDate(date).sumOf { it.amount }
            weekData.add(0, Pair(date, intake)) // Add at beginning for chronological order
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return weekData
    }

    fun getTodayHydrationPercentage(dailyGoal: Int): Float {
        val todayIntake = getTodayWaterIntake()
        return if (dailyGoal > 0) {
            (todayIntake.toFloat() / dailyGoal) * 100
        } else 0f
    }
}
