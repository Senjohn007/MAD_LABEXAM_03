package com.example.wellnesstracker.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.wellnesstracker.utils.ReminderManager

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_PACKAGE_REPLACED -> {
                    Log.d(TAG, "Device booted or app updated, rescheduling water reminders")

                    // Reschedule water reminders if they were enabled
                    val reminderManager = ReminderManager(context)
                    reminderManager.rescheduleRemindersIfEnabled()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in BootReceiver", e)
        }
    }
}
