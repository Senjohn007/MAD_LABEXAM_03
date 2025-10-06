package com.example.wellnesstracker.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.wellnesstracker.receivers.WaterReminderReceiver
import java.util.*

class ReminderManager(private val context: Context) {

    companion object {
        private const val TAG = "ReminderManager"
        private const val PREFS_NAME = "ReminderPrefs"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_REMINDER_INTERVAL = "reminder_interval"
        private const val KEY_START_HOUR = "start_hour"
        private const val KEY_END_HOUR = "end_hour"
        private const val KEY_START_MINUTE = "start_minute"
        private const val KEY_END_MINUTE = "end_minute"
        private const val KEY_ACTIVE_DAYS = "active_days"
        private const val REQUEST_CODE = 1001
    }

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun isRemindersEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMINDERS_ENABLED, false)
    }

    fun getReminderInterval(): Int {
        return sharedPreferences.getInt(KEY_REMINDER_INTERVAL, 2) // Default 2 hours
    }

    fun getStartHour(): Int {
        return sharedPreferences.getInt(KEY_START_HOUR, 8) // Default 8 AM
    }

    fun getEndHour(): Int {
        return sharedPreferences.getInt(KEY_END_HOUR, 22) // Default 10 PM
    }

    fun getStartMinute(): Int {
        return sharedPreferences.getInt(KEY_START_MINUTE, 0) // Default 0 minutes
    }

    fun getEndMinute(): Int {
        return sharedPreferences.getInt(KEY_END_MINUTE, 0) // Default 0 minutes
    }

    fun getActiveDays(): Set<Int> {
        val defaultDays = setOf(1, 2, 3, 4, 5, 6, 7) // All days by default
        val savedDays = sharedPreferences.getStringSet(KEY_ACTIVE_DAYS,
            defaultDays.map { it.toString() }.toSet())
        return savedDays?.map { it.toInt() }?.toSet() ?: defaultDays
    }

    fun saveReminderSettings(
        enabled: Boolean,
        intervalHours: Int,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        activeDays: Set<Int>
    ) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_REMINDERS_ENABLED, enabled)
            putInt(KEY_REMINDER_INTERVAL, intervalHours)
            putInt(KEY_START_HOUR, startHour)
            putInt(KEY_START_MINUTE, startMinute)
            putInt(KEY_END_HOUR, endHour)
            putInt(KEY_END_MINUTE, endMinute)
            putStringSet(KEY_ACTIVE_DAYS, activeDays.map { it.toString() }.toSet())
            apply()
        }

        if (enabled) {
            scheduleReminders(intervalHours)
        } else {
            cancelReminders()
        }
    }

    fun scheduleReminders(intervalHours: Int) {
        try {
            val intent = Intent(context, WaterReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val intervalMillis = intervalHours * 60 * 60 * 1000L // Convert to milliseconds
            val firstTriggerTime = System.currentTimeMillis() + intervalMillis

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    firstTriggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    firstTriggerTime,
                    intervalMillis,
                    pendingIntent
                )
            }

            Log.d(TAG, "Water reminders scheduled every $intervalHours hours")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminders", e)
        }
    }

    fun scheduleSnoozeReminder(delayMinutes: Int) {
        try {
            val intent = Intent(context, WaterReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                999, // Different request code for snooze
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

            Log.d(TAG, "Snooze reminder scheduled for $delayMinutes minutes")

        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling snooze reminder", e)
        }
    }

    fun cancelReminders() {
        try {
            val intent = Intent(context, WaterReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Water reminders cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling reminders", e)
        }
    }

    fun rescheduleRemindersIfEnabled() {
        if (isRemindersEnabled()) {
            val interval = getReminderInterval()
            scheduleReminders(interval)
            Log.d(TAG, "Reminders rescheduled after boot/update")
        }
    }

    fun getReminderStatusText(): String {
        return if (isRemindersEnabled()) {
            val interval = getReminderInterval()
            "Every $interval hour${if (interval > 1) "s" else ""}"
        } else {
            "Disabled"
        }
    }

    fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        return String.format("%d:%02d %s", displayHour, minute, amPm)
    }
}
