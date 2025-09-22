package com.example.wellnesstracker

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Habit management
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        sharedPreferences.edit().putString("habits", json).apply()
    }

    fun getHabits(): List<Habit> {
        val json = sharedPreferences.getString("habits", null)
        return if (json != null) {
            val type = object : TypeToken<List<Habit>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    // Mood management
    fun saveMoodEntries(moods: List<MoodEntry>) {
        val json = gson.toJson(moods)
        sharedPreferences.edit().putString("moods", json).apply()
    }

    fun getMoodEntries(): List<MoodEntry> {
        val json = sharedPreferences.getString("moods", null)
        return if (json != null) {
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    // Hydration settings
    fun saveHydrationSettings(settings: HydrationSettings) {
        val json = gson.toJson(settings)
        sharedPreferences.edit().putString("hydration_settings", json).apply()
    }

    fun getHydrationSettings(): HydrationSettings {
        val json = sharedPreferences.getString("hydration_settings", null)
        return if (json != null) {
            gson.fromJson(json, HydrationSettings::class.java)
        } else {
            HydrationSettings()
        }
    }

    // Water intake tracking
    fun getTodayWaterIntake(): Int {
        val today = getCurrentDateString()
        return sharedPreferences.getInt("water_$today", 0)
    }

    fun addWaterIntake(glasses: Int = 1) {
        val today = getCurrentDateString()
        val current = getTodayWaterIntake()
        sharedPreferences.edit().putInt("water_$today", current + glasses).apply()
    }

    // Utility functions
    private fun getCurrentDateString(): String {
        val calendar = java.util.Calendar.getInstance()
        return "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH) + 1}-${calendar.get(java.util.Calendar.DAY_OF_MONTH)}"
    }
}
