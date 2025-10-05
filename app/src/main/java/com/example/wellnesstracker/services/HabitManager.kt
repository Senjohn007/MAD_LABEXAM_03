package com.example.wellnesstracker.services

import android.content.Context
import com.example.wellnesstracker.models.Habit
import com.example.wellnesstracker.models.HabitProgress
import java.text.SimpleDateFormat
import java.util.*

class HabitManager(context: Context) : DataManager(context) {
    override val preferencesName = "habits_prefs"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        private const val HABITS_KEY = "habits"
        private const val HABIT_PROGRESS_KEY = "habit_progress"
    }

    // Habit CRUD operations
    fun saveHabit(habit: Habit) {
        val habits = getAllHabits().toMutableList()
        val existingIndex = habits.indexOfFirst { it.id == habit.id }

        if (existingIndex >= 0) {
            habits[existingIndex] = habit
        } else {
            habits.add(habit)
        }

        saveList(HABITS_KEY, habits)
    }

    fun getAllHabits(): List<Habit> {
        return getList<Map<String, Any>>(HABITS_KEY).map { Habit.fromMap(it) }
    }

    fun getActiveHabits(): List<Habit> {
        return getAllHabits().filter { it.isActive }
    }

    fun getHabitById(id: String): Habit? {
        return getAllHabits().find { it.id == id }
    }

    fun deleteHabit(habitId: String) {
        val habits = getAllHabits().filter { it.id != habitId }
        saveList(HABITS_KEY, habits)

        // Also delete all progress for this habit
        val progress = getAllHabitProgress().filter { it.habitId != habitId }
        saveList(HABIT_PROGRESS_KEY, progress)
    }

    // Habit Progress operations
    fun saveHabitProgress(progress: HabitProgress) {
        val allProgress = getAllHabitProgress().toMutableList()
        val existingIndex = allProgress.indexOfFirst {
            it.habitId == progress.habitId && it.date == progress.date
        }

        if (existingIndex >= 0) {
            allProgress[existingIndex] = progress
        } else {
            allProgress.add(progress)
        }

        saveList(HABIT_PROGRESS_KEY, allProgress)
    }

    fun getAllHabitProgress(): List<HabitProgress> {
        return getList<Map<String, Any>>(HABIT_PROGRESS_KEY).map { HabitProgress.fromMap(it) }
    }

    fun getHabitProgressForDate(date: String): List<HabitProgress> {
        return getAllHabitProgress().filter { it.date == date }
    }

    fun getTodayProgress(): List<HabitProgress> {
        val today = dateFormat.format(Date())
        return getHabitProgressForDate(today)
    }

    fun markHabitComplete(habitId: String, date: String = dateFormat.format(Date())) {
        val existingProgress = getAllHabitProgress().find {
            it.habitId == habitId && it.date == date
        }
        val progress = existingProgress?.copy(
            isCompleted = true,
            completionTime = System.currentTimeMillis()
        ) ?: HabitProgress(
            habitId = habitId,
            date = date,
            isCompleted = true,
            completionTime = System.currentTimeMillis()
        )

        saveHabitProgress(progress)
    }

    fun getTodayCompletionPercentage(): Float {
        val activeHabits = getActiveHabits()
        if (activeHabits.isEmpty()) return 0f

        val todayProgress = getTodayProgress()
        val completedCount = todayProgress.count { it.isCompleted }

        return (completedCount.toFloat() / activeHabits.size) * 100
    }
}
