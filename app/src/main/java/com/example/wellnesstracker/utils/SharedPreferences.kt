package com.example.wellnesstracker.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 *  Simple, thread-safe SharedPreferences wrapper.
 *
 *  1.  Call  Prefs.init(applicationContext)  once in  Application.onCreate().
 *  2.  Use the put / get helpers anywhere in the app.
 */
object Prefs {

    private const val FILE = "wellness_prefs"
    private lateinit var prefs: android.content.SharedPreferences
    private val gson = Gson()

    /** MUST be called once from your Application class */
    fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    }

    /** Check if Prefs has been initialized */
    fun isInitialized(): Boolean {
        return ::prefs.isInitialized
    }

    /* ------------------------------------------------------------------ *
     *  Primitive helpers                                                 *
     * ------------------------------------------------------------------ */

    fun putInt(key: String, value: Int)            = prefs.edit().putInt(key, value).apply()
    fun getInt(key: String, def: Int = 0)          = prefs.getInt(key, def)

    fun putBool(key: String, value: Boolean)       = prefs.edit().putBoolean(key, value).apply()
    fun getBool(key: String, def: Boolean = false) = prefs.getBoolean(key, def)

    fun putFloat(key: String, value: Float)        = prefs.edit().putFloat(key, value).apply()
    fun getFloat(key: String, def: Float = 0f)     = prefs.getFloat(key, def)

    fun putLong(key: String, value: Long)          = prefs.edit().putLong(key, value).apply()
    fun getLong(key: String, def: Long = 0L)       = prefs.getLong(key, def)

    fun putString(key: String, value: String)      = prefs.edit().putString(key, value).apply()
    fun getString(key: String, def: String = "")   = prefs.getString(key, def) ?: def

    /* ------------------------------------------------------------------ *
     *  Generic object helpers – JSON via Gson                            *
     * ------------------------------------------------------------------ */

    /** Save any serialisable object ( data class, list, map … ) */
    fun <T> putObj(key: String, obj: T) {
        prefs.edit().putString(key, gson.toJson(obj)).apply()
    }

    /**
     * Retrieve an object.
     * Usage:
     *     val type = object : TypeToken<UserSettings>(){}.type
     *     val settings = Prefs.getObj("settings", UserSettings(), type)
     */
    fun <T> getObj(key: String, def: T, type: Type): T {
        val json = prefs.getString(key, null) ?: return def
        return runCatching { gson.fromJson<T>(json, type) }.getOrElse { def }
    }

    /** Remove a key */
    fun remove(key: String) = prefs.edit().remove(key).apply()

    /** Clear *everything* (use carefully!) */
    fun clearAll() = prefs.edit().clear().apply()

    /* ------------------------------------------------------------------ *
     *  Step Counter specific helpers                                      *
     * ------------------------------------------------------------------ */

    // Step counting constants
    private const val PREF_TODAY_STEPS = "today_steps"
    private const val PREF_LAST_SAVED_DATE = "last_saved_date"
    private const val PREF_INITIAL_STEP_COUNT = "initial_step_count"
    private const val PREF_DAILY_GOAL = "daily_goal"
    private const val PREF_WEEKLY_STEPS_PREFIX = "weekly_steps_"

    // Step counting methods
    fun getTodaySteps(): Int = getInt(PREF_TODAY_STEPS, 0)
    fun setTodaySteps(steps: Int) = putInt(PREF_TODAY_STEPS, steps)

    fun getLastSavedDate(): String = getString(PREF_LAST_SAVED_DATE, "")
    fun setLastSavedDate(date: String) = putString(PREF_LAST_SAVED_DATE, date)

    fun getInitialStepCount(): Long = getLong(PREF_INITIAL_STEP_COUNT, 0L)
    fun setInitialStepCount(count: Long) = putLong(PREF_INITIAL_STEP_COUNT, count)

    fun getDailyGoal(): Int = getInt(PREF_DAILY_GOAL, 10000)
    fun setDailyGoal(goal: Int) = putInt(PREF_DAILY_GOAL, goal)

    // Weekly steps (0=Sunday, 1=Monday, ..., 6=Saturday)
    fun getWeeklySteps(dayOfWeek: Int): Int =
        getInt("$PREF_WEEKLY_STEPS_PREFIX$dayOfWeek", 0)

    fun setWeeklySteps(dayOfWeek: Int, steps: Int) =
        putInt("$PREF_WEEKLY_STEPS_PREFIX$dayOfWeek", steps)

    fun getAllWeeklySteps(): IntArray {
        val weeklySteps = IntArray(7)
        for (i in 0..6) {
            weeklySteps[i] = getWeeklySteps(i)
        }
        return weeklySteps
    }

    // Convenience method to reset weekly steps
    fun clearWeeklySteps() {
        for (i in 0..6) {
            setWeeklySteps(i, 0)
        }
    }

    // Check if it's a new week and reset if needed
    fun resetWeeklyStepsIfNeeded(currentWeekOfYear: Int): Boolean {
        val lastWeek = getInt("last_week_of_year", -1)
        if (lastWeek != currentWeekOfYear) {
            clearWeeklySteps()
            putInt("last_week_of_year", currentWeekOfYear)
            return true
        }
        return false
    }

    // Get step statistics
    fun getWeeklyAverage(): Int {
        val weeklySteps = getAllWeeklySteps()
        val total = weeklySteps.sum()
        return if (total > 0) total / 7 else 0
    }

    fun getWeeklyBest(): Int {
        val weeklySteps = getAllWeeklySteps()
        return weeklySteps.maxOrNull() ?: 0
    }

    fun getWeeklyTotal(): Int {
        val weeklySteps = getAllWeeklySteps()
        return weeklySteps.sum()
    }

    // Check if daily goal was achieved
    fun isDailyGoalAchieved(): Boolean {
        return getTodaySteps() >= getDailyGoal()
    }

    fun getDailyGoalProgress(): Float {
        val goal = getDailyGoal()
        return if (goal > 0) {
            (getTodaySteps().toFloat() / goal.toFloat()).coerceAtMost(1.0f)
        } else {
            0f
        }
    }

    // Utility method to get remaining steps for daily goal
    fun getRemainingStepsForGoal(): Int {
        val remaining = getDailyGoal() - getTodaySteps()
        return if (remaining > 0) remaining else 0
    }
}

// Backward compatibility class - renamed to avoid conflict with Android's SharedPreferences
class WellnessPreferences(context: Context) {

    init {
        // Ensure Prefs is initialized
        if (!Prefs.isInitialized()) {
            Prefs.init(context)
        }
    }

    // Delegate all step counting methods to Prefs object
    fun getTodaySteps(): Int = Prefs.getTodaySteps()
    fun setTodaySteps(steps: Int) = Prefs.setTodaySteps(steps)

    fun getLastSavedDate(): String = Prefs.getLastSavedDate()
    fun setLastSavedDate(date: String) = Prefs.setLastSavedDate(date)

    fun getInitialStepCount(): Long = Prefs.getInitialStepCount()
    fun setInitialStepCount(count: Long) = Prefs.setInitialStepCount(count)

    fun getDailyGoal(): Int = Prefs.getDailyGoal()
    fun setDailyGoal(goal: Int) = Prefs.setDailyGoal(goal)

    fun getWeeklySteps(dayOfWeek: Int): Int = Prefs.getWeeklySteps(dayOfWeek)
    fun setWeeklySteps(dayOfWeek: Int, steps: Int) = Prefs.setWeeklySteps(dayOfWeek, steps)

    fun getAllWeeklySteps(): IntArray = Prefs.getAllWeeklySteps()

    // Additional convenience methods
    fun clearWeeklySteps() = Prefs.clearWeeklySteps()
    fun getWeeklyAverage(): Int = Prefs.getWeeklyAverage()
    fun getWeeklyBest(): Int = Prefs.getWeeklyBest()
    fun isDailyGoalAchieved(): Boolean = Prefs.isDailyGoalAchieved()
    fun getRemainingStepsForGoal(): Int = Prefs.getRemainingStepsForGoal()
}

// For backward compatibility, create a typealias
typealias SharedPreferences = WellnessPreferences