package com.example.wellnesstracker.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.wellnesstracker.R
import com.example.wellnesstracker.StepsActivity
import com.example.wellnesstracker.utils.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class StepCounterService : Service(), SensorEventListener {

    private val TAG = "StepCounterService"
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "step_counter_channel"

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private lateinit var sharedPrefs: SharedPreferences

    // Binder for bound service
    private val binder = StepCounterBinder()

    // Step counting variables
    private var initialStepCount = 0L
    private var currentDaySteps = 0
    private var lastSavedDate = ""
    private var sensorStepsSinceReboot = 0L

    // Callback for UI updates
    var onStepCountChanged: ((Int) -> Unit)? = null

    // Notification permission status
    private var canShowNotifications = true

    inner class StepCounterBinder : Binder() {
        fun getService(): StepCounterService = this@StepCounterService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        sharedPrefs = SharedPreferences(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Check notification permission
        checkNotificationPermission()

        createNotificationChannel()
        initializeStepData()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        // Start foreground service with notification if permission is granted
        if (canShowNotifications) {
            try {
                startForeground(NOTIFICATION_ID, createNotification())
            } catch (e: SecurityException) {
                Log.e(TAG, "Cannot show notification due to missing permission", e)
                canShowNotifications = false
            }
        } else {
            Log.w(TAG, "Starting service without notification due to missing permission")
        }

        if (stepCounterSensor != null) {
            val registered = sensorManager.registerListener(
                this,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            Log.d(TAG, "Sensor registered: $registered")
        } else {
            Log.e(TAG, "Step counter sensor not available")
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Service bound")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Service unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        sensorManager.unregisterListener(this)
        saveStepData()
    }

    // Check if we have notification permission
    private fun checkNotificationPermission() {
        canShowNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+) - Check POST_NOTIFICATIONS permission
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Below Android 13 - No permission needed
            true
        }

        Log.d(TAG, "Notification permission granted: $canShowNotifications")
    }

    // SensorEventListener implementation
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            sensorStepsSinceReboot = event.values[0].toLong()
            Log.d(TAG, "Sensor steps since reboot: $sensorStepsSinceReboot")

            updateDailyStepCount()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }

    private fun initializeStepData() {
        val today = getCurrentDate()
        lastSavedDate = sharedPrefs.getLastSavedDate()

        if (lastSavedDate != today) {
            // New day - reset daily steps
            Log.d(TAG, "New day detected. Resetting daily steps.")
            resetDailySteps(today)
        } else {
            // Same day - load existing data
            currentDaySteps = sharedPrefs.getTodaySteps()
            initialStepCount = sharedPrefs.getInitialStepCount()
        }

        Log.d(TAG, "Initialized - Date: $today, Daily steps: $currentDaySteps, Initial: $initialStepCount")
    }

    private fun updateDailyStepCount() {
        val today = getCurrentDate()

        // Check if it's a new day
        if (lastSavedDate != today) {
            resetDailySteps(today)
        }

        if (initialStepCount == 0L && sensorStepsSinceReboot > 0) {
            // First time getting sensor data today
            initialStepCount = sensorStepsSinceReboot
            sharedPrefs.setInitialStepCount(initialStepCount)
        }

        if (initialStepCount > 0 && sensorStepsSinceReboot >= initialStepCount) {
            val newDailySteps = (sensorStepsSinceReboot - initialStepCount).toInt()

            if (newDailySteps != currentDaySteps) {
                currentDaySteps = newDailySteps
                saveStepData()
                updateNotification()

                // Notify UI if bound
                onStepCountChanged?.invoke(currentDaySteps)

                Log.d(TAG, "Daily steps updated: $currentDaySteps")
            }
        }
    }

    private fun resetDailySteps(newDate: String) {
        // Save yesterday's data to weekly history if needed
        if (lastSavedDate.isNotEmpty() && currentDaySteps > 0) {
            saveToWeeklyHistory(lastSavedDate, currentDaySteps)
        }

        // Reset for new day
        currentDaySteps = 0
        initialStepCount = sensorStepsSinceReboot
        lastSavedDate = newDate

        // Save new day data
        sharedPrefs.setLastSavedDate(newDate)
        sharedPrefs.setTodaySteps(0)
        sharedPrefs.setInitialStepCount(initialStepCount)

        Log.d(TAG, "Daily steps reset for date: $newDate")
    }

    private fun saveStepData() {
        sharedPrefs.setTodaySteps(currentDaySteps)
        sharedPrefs.setLastSavedDate(getCurrentDate())
        sharedPrefs.setInitialStepCount(initialStepCount)
    }

    private fun saveToWeeklyHistory(date: String, steps: Int) {
        try {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            calendar.time = dateFormat.parse(date) ?: return

            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sunday, 6=Saturday
            sharedPrefs.setWeeklySteps(dayOfWeek, steps)

            Log.d(TAG, "Saved to weekly history - Day: $dayOfWeek, Steps: $steps")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to weekly history", e)
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Counter",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Counting your steps"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, StepsActivity::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlags)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Counter Active")
            .setContentText("Today: ${formatStepCount(currentDaySteps)} steps")
            .setSmallIcon(R.drawable.ic_steps)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification() {
        if (!canShowNotifications) {
            Log.d(TAG, "Cannot update notification - permission not granted")
            return
        }

        try {
            val notification = createNotification()
            val notificationManager = NotificationManagerCompat.from(this)

            // Double-check permission before posting
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            } else {
                Log.w(TAG, "Notification permission revoked, cannot update notification")
                canShowNotifications = false
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when updating notification", e)
            canShowNotifications = false
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification", e)
        }
    }

    private fun formatStepCount(steps: Int): String {
        return when {
            steps >= 1000 -> String.format("%,d", steps)
            else -> steps.toString()
        }
    }

    // Public methods for bound service
    fun getCurrentSteps(): Int = currentDaySteps

    fun addManualSteps(steps: Int) {
        currentDaySteps += steps
        saveStepData()
        updateNotification()
        onStepCountChanged?.invoke(currentDaySteps)
        Log.d(TAG, "Manual steps added: $steps, Total: $currentDaySteps")
    }

    // Method to refresh notification permission status
    fun refreshNotificationPermission() {
        checkNotificationPermission()
        if (canShowNotifications) {
            updateNotification()
        }
    }
}
