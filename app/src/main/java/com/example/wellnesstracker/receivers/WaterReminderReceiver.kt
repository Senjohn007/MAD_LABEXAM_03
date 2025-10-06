package com.example.wellnesstracker.receivers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.wellnesstracker.HydrationActivity
import com.example.wellnesstracker.R

class WaterReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WaterReminderReceiver"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "WATER_REMINDER_CHANNEL"
        const val CHANNEL_NAME = "Water Reminders"
        const val CHANNEL_DESC = "Reminders to drink water and stay hydrated"

        // Action for notification buttons
        const val ACTION_DRINK_WATER = "com.example.wellnesstracker.DRINK_WATER"
        const val ACTION_SNOOZE = "com.example.wellnesstracker.SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "Water reminder received")

            when (intent.action) {
                ACTION_DRINK_WATER -> {
                    Log.d(TAG, "User clicked 'I Drank Water' action")
                    handleDrinkWaterAction(context)
                }
                ACTION_SNOOZE -> {
                    Log.d(TAG, "User clicked 'Snooze' action")
                    handleSnoozeAction(context)
                }
                else -> {
                    // Regular reminder notification
                    createNotificationChannel(context)
                    showWaterReminderNotification(context)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in WaterReminderReceiver", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create audio attributes for notification sound
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Notification channel created successfully")
        }
    }

    private fun showWaterReminderNotification(context: Context) {
        try {
            // Intent to open HydrationActivity when notification is tapped
            val openAppIntent = Intent(context, HydrationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Action button: "I Drank Water"
            val drinkWaterIntent = Intent(context, WaterReminderReceiver::class.java).apply {
                action = ACTION_DRINK_WATER
            }
            val drinkWaterPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                drinkWaterIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Action button: "Snooze 30 min"
            val snoozeIntent = Intent(context, WaterReminderReceiver::class.java).apply {
                action = ACTION_SNOOZE
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                2,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Get motivational messages
            val motivationalMessages = getMotivationalMessages()
            val randomMessage = motivationalMessages.random()

            // Build the notification
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("💧 Time to Hydrate!")
                .setContentText(randomMessage)
                .setStyle(NotificationCompat.BigTextStyle().bigText(randomMessage))
                .setSmallIcon(R.drawable.ic_water_drop)
                .setLargeIcon(null as Bitmap?)
                .setContentIntent(openAppPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(Color.CYAN)
                // Add action buttons
                .addAction(
                    R.drawable.ic_water_drop,
                    "I Drank Water",
                    drinkWaterPendingIntent
                )
                .addAction(
                    R.drawable.ic_reminder,
                    "Snooze 30min",
                    snoozePendingIntent
                )
                .build()

            // Show the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)

            // Vibrate device for extra attention
            vibrateDevice(context)

            Log.d(TAG, "Water reminder notification shown successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error showing water reminder notification", e)
        }
    }

    private fun vibrateDevice(context: Context) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Create vibration pattern: wait 0ms, vibrate 1000ms, wait 500ms, vibrate 1000ms
                    val vibrationEffect = VibrationEffect.createWaveform(
                        longArrayOf(0, 1000, 500, 1000),
                        -1 // Don't repeat
                    )
                    vibrator.vibrate(vibrationEffect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 1000, 500, 1000), -1)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating device", e)
        }
    }

    private fun handleDrinkWaterAction(context: Context) {
        try {
            // Cancel the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)

            // Show success notification
            showSuccessNotification(context, "Great! Keep staying hydrated! 💧")

            Log.d(TAG, "Drink water action handled")

        } catch (e: Exception) {
            Log.e(TAG, "Error handling drink water action", e)
        }
    }

    private fun handleSnoozeAction(context: Context) {
        try {
            // Cancel the current notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)

            // Schedule a new reminder in 30 minutes
            scheduleSnoozeReminder(context, 30)

            // Show confirmation
            showSuccessNotification(context, "Reminder snoozed for 30 minutes ⏰")

            Log.d(TAG, "Snooze action handled - reminder in 30 minutes")

        } catch (e: Exception) {
            Log.e(TAG, "Error handling snooze action", e)
        }
    }

    private fun scheduleSnoozeReminder(context: Context, delayMinutes: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WaterReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                999, // Different request code for snooze
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
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

    private fun showSuccessNotification(context: Context, message: String) {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Water Tracker")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_water_drop)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setTimeoutAfter(5000) // Auto dismiss after 5 seconds
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID + 1, notification)

        } catch (e: Exception) {
            Log.e(TAG, "Error showing success notification", e)
        }
    }

    private fun getMotivationalMessages(): List<String> {
        return listOf(
            "Stay hydrated and feel great! Time for some water! 💧",
            "Your body is calling for hydration! Don't keep it waiting! 🚰",
            "A glass of water a day keeps dehydration away! 💙",
            "Hydration station time! Your body will thank you! ✨",
            "Water break! Your health is worth it! 🌊",
            "Time to refuel with some H2O! Stay healthy! 💪",
            "Don't forget to drink water - your body needs it! 🥤",
            "Hydrate now and feel the energy boost! 🔋",
            "Water time! Keep that glow and energy flowing! ✨",
            "Your reminder to drink water and stay awesome! 🌟",
            "Sip by sip, stay healthy and fit! Time for water! 💧",
            "Hydration alert! Time to give your body some love! ❤️"
        )
    }
}
