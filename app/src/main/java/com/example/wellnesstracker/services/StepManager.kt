package com.example.wellnesstracker.services

import android.content.Context
import com.example.wellnesstracker.models.StepData
import java.text.SimpleDateFormat
import java.util.*

class StepManager(context: Context) : DataManager(context) {
    override val preferencesName = "step_prefs"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        private const val STEP_DATA_KEY = "step_data"
        private const val DAILY_STEP_GOAL_KEY = "daily_step_goal"
        private const val STEP_LENGTH_CM_KEY = "step_length_cm"
    }

    fun saveStepData(stepData: StepData) {
        val allStepData = getAllStepData().toMutableList()
        val existingIndex = allStepData.indexOfFirst { it.date == stepData.date }

        if (existingIndex >= 0) {
            allStepData[existingIndex] = stepData
        } else {
            allStepData.add(stepData)
        }

        saveList(STEP_DATA_KEY, allStepData)
    }

    fun getAllStepData(): List<StepData> {
        return getList<Map<String, Any>>(STEP_DATA_KEY).map { StepData.fromMap(it) }
    }

    fun getStepDataForDate(date: String): StepData? {
        return getAllStepData().find { it.date == date }
    }

    fun getTodayStepData(): StepData? {
        val today = dateFormat.format(Date())
        return getStepDataForDate(today)
    }

    fun updateTodaySteps(steps: Int) {
        val today = dateFormat.format(Date())
        val existingData = getTodayStepData()
        val stepLength = getStepLength()

        val stepData = existingData?.copy(
            stepCount = steps,
            distance = (steps * stepLength) / 100000f, // Convert cm to km
            calories = calculateCalories(steps)
        ) ?: StepData(
            date = today,
            stepCount = steps,
            distance = (steps * stepLength) / 100000f,
            calories = calculateCalories(steps)
        )

        saveStepData(stepData)
    }

    fun getWeeklyStepData(): List<StepData> {
        val calendar = Calendar.getInstance()
        val weekData = mutableListOf<StepData>()

        for (i in 0 until 7) {
            val date = dateFormat.format(calendar.time)
            val stepData = getStepDataForDate(date) ?: StepData(
                date = date,
                stepCount = 0
            )
            weekData.add(0, stepData)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return weekData
    }

    fun setDailyStepGoal(goal: Int) {
        saveInt(DAILY_STEP_GOAL_KEY, goal)
    }

    fun getDailyStepGoal(): Int {
        return getInt(DAILY_STEP_GOAL_KEY, 10000)
    }

    fun setStepLength(lengthInCm: Float) {
        saveFloat(STEP_LENGTH_CM_KEY, lengthInCm)
    }

    fun getStepLength(): Float {
        return getFloat(STEP_LENGTH_CM_KEY, 75f) // Default average step length
    }

    private fun calculateCalories(steps: Int): Int {
        // Rough calculation: 1 step = 0.04 calories (average for 70kg person)
        return (steps * 0.04).toInt()
    }

    fun getTodayStepPercentage(): Float {
        val todaySteps = getTodayStepData()?.stepCount ?: 0
        val dailyGoal = getDailyStepGoal()

        return if (dailyGoal > 0) {
            (todaySteps.toFloat() / dailyGoal) * 100
        } else 0f
    }
}
