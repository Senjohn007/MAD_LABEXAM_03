package com.example.wellnesstracker

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstracker.services.StepCounterService
import com.example.wellnesstracker.utils.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class StepsActivity : AppCompatActivity() {

    private val TAG = "StepsActivity"

    // UI Components
    private lateinit var toolbar: Toolbar
    private lateinit var tvStepCount: TextView
    private lateinit var tvStepGoal: TextView
    private lateinit var tvStepPercentage: TextView
    private lateinit var progressSteps: ProgressBar
    private lateinit var tvDistance: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvActiveTime: TextView
    private lateinit var etManualSteps: EditText
    private lateinit var btnAddSteps: Button
    private lateinit var rvStepHistory: RecyclerView
    private lateinit var btnSetGoal: Button

    // Daily Goal UI Components
    private lateinit var tvDailyGoalCount: TextView
    private lateinit var tvGoalStatus: TextView

    // Weekly Chart UI Components
    private lateinit var tvSunCount: TextView
    private lateinit var tvMonCount: TextView
    private lateinit var tvTueCount: TextView
    private lateinit var tvWedCount: TextView
    private lateinit var tvThuCount: TextView
    private lateinit var tvFriCount: TextView
    private lateinit var tvSatCount: TextView
    private lateinit var progressSun: ProgressBar
    private lateinit var progressMon: ProgressBar
    private lateinit var progressTue: ProgressBar
    private lateinit var progressWed: ProgressBar
    private lateinit var progressThu: ProgressBar
    private lateinit var progressFri: ProgressBar
    private lateinit var progressSat: ProgressBar
    private lateinit var tvWeeklyAverage: TextView
    private lateinit var tvWeeklyBest: TextView

    // Data variables
    private var currentSteps = 8542
    private var dailyGoal = 10000
    private val stepHistory = mutableListOf<StepEntry>()
    private lateinit var stepHistoryAdapter: StepHistoryAdapter

    // Weekly step data (Sun=0, Mon=1, ..., Sat=6)
    private val weeklySteps = intArrayOf(7543, 9876, 10523, 6234, 8765, 9234, 8542) // Current week

    // Service-related variables
    companion object {
        private const val ACTIVITY_RECOGNITION_REQUEST_CODE = 100
    }

    private var stepCounterService: StepCounterService? = null
    private var isServiceBound = false

    // Service connection
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StepCounterService.StepCounterBinder
            stepCounterService = binder.getService()
            isServiceBound = true

            // Set up callback for real-time updates
            stepCounterService?.onStepCountChanged = { steps ->
                runOnUiThread {
                    currentSteps = steps
                    updateUI()
                }
            }

            // Get current steps from service
            currentSteps = stepCounterService?.getCurrentSteps() ?: currentSteps
            updateUI()

            Log.d(TAG, "Service connected. Current steps: $currentSteps")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            stepCounterService = null
            isServiceBound = false
            Log.d(TAG, "Service disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "StepsActivity onCreate started")

            // Set the content view to use steps_page.xml
            setContentView(R.layout.steps_page)

            initializeViews()
            setupToolbar()
            setupClickListeners()
            setupRecyclerView()
            loadSampleData()
            updateUI()

            // Start automatic step counting
            checkAndRequestPermissions()

            Log.d(TAG, "StepsActivity onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in StepsActivity onCreate", e)
            showToast("Error initializing step counter: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            try {
                unbindService(serviceConnection)
                isServiceBound = false
                Log.d(TAG, "Service unbound successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service", e)
            }
        }
    }

    private fun initializeViews() {
        try {
            // Initialize all views
            toolbar = findViewById(R.id.toolbar)
            tvStepCount = findViewById(R.id.tv_step_count)
            tvStepGoal = findViewById(R.id.tv_step_goal)
            tvStepPercentage = findViewById(R.id.tv_step_percentage)
            progressSteps = findViewById(R.id.progress_steps)
            tvDistance = findViewById(R.id.tv_distance)
            tvCalories = findViewById(R.id.tv_calories)
            tvActiveTime = findViewById(R.id.tv_active_time)
            etManualSteps = findViewById(R.id.et_manual_steps)
            btnAddSteps = findViewById(R.id.btn_add_steps)
            rvStepHistory = findViewById(R.id.rv_step_history)
            btnSetGoal = findViewById(R.id.btn_set_goal)

            // Daily Goal UI Components
            tvDailyGoalCount = findViewById(R.id.tv_daily_goal_count)
            tvGoalStatus = findViewById(R.id.tv_goal_status)

            // Weekly Chart UI Components
            tvSunCount = findViewById(R.id.tv_sun_count)
            tvMonCount = findViewById(R.id.tv_mon_count)
            tvTueCount = findViewById(R.id.tv_tue_count)
            tvWedCount = findViewById(R.id.tv_wed_count)
            tvThuCount = findViewById(R.id.tv_thu_count)
            tvFriCount = findViewById(R.id.tv_fri_count)
            tvSatCount = findViewById(R.id.tv_sat_count)

            progressSun = findViewById(R.id.progress_sun)
            progressMon = findViewById(R.id.progress_mon)
            progressTue = findViewById(R.id.progress_tue)
            progressWed = findViewById(R.id.progress_wed)
            progressThu = findViewById(R.id.progress_thu)
            progressFri = findViewById(R.id.progress_fri)
            progressSat = findViewById(R.id.progress_sat)

            tvWeeklyAverage = findViewById(R.id.tv_weekly_average)
            tvWeeklyBest = findViewById(R.id.tv_weekly_best)

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            throw e
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)

            // Handle back button click
            toolbar.setNavigationOnClickListener {
                finish()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    private fun setupClickListeners() {
        try {
            // Add steps button
            btnAddSteps.setOnClickListener {
                addManualSteps()
            }

            // Set goal button
            btnSetGoal.setOnClickListener {
                showSetGoalDialog()
            }

            // Manual steps input listener
            etManualSteps.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    updateAddButtonState()
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            stepHistoryAdapter = StepHistoryAdapter(stepHistory)
            rvStepHistory.layoutManager = LinearLayoutManager(this)
            rvStepHistory.adapter = stepHistoryAdapter

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    // Permission handling methods
    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_REQUEST_CODE
            )
        } else {
            startStepCountingService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepCountingService()
                showToast("Step tracking enabled!")
            } else {
                showToast("Permission required for automatic step counting")
            }
        }
    }

    // Start the step counting service - FIXED for API compatibility
    private fun startStepCountingService() {
        try {
            val serviceIntent = Intent(this, StepCounterService::class.java)

            // Check Android version and use appropriate method
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ (API 26+) - Use startForegroundService
                startForegroundService(serviceIntent)
            } else {
                // Android 7.1 and below (API < 26) - Use startService
                startService(serviceIntent)
            }

            // Bind to service for communication
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "Step counting service started and bound")

        } catch (e: Exception) {
            Log.e(TAG, "Error starting step counting service", e)
            showToast("Error starting step counter: ${e.message}")
        }
    }

    private fun showSetGoalDialog() {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Set Daily Goal")
            builder.setMessage("Enter your daily step goal:")

            // Set up the input
            val input = EditText(this)
            input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            input.setText(dailyGoal.toString())
            input.selectAll()
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("OK") { dialog, _ ->
                val goalText = input.text.toString().trim()
                if (goalText.isNotEmpty()) {
                    val newGoal = goalText.toIntOrNull()
                    if (newGoal != null && newGoal > 0 && newGoal <= 100000) {
                        dailyGoal = newGoal

                        // Save to SharedPreferences
                        try {
                            val sharedPrefs = SharedPreferences(this)
                            sharedPrefs.setDailyGoal(dailyGoal)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error saving daily goal to SharedPreferences", e)
                        }

                        updateUI()
                        showToast("Daily goal set to $dailyGoal steps!")
                        Log.d(TAG, "Daily goal updated to: $dailyGoal")
                    } else {
                        showToast("Please enter a valid goal between 1 and 100,000")
                    }
                } else {
                    showToast("Please enter a goal")
                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing set goal dialog", e)
            showToast("Error opening goal settings")
        }
    }

    private fun addManualSteps() {
        try {
            val manualStepsText = etManualSteps.text.toString().trim()
            if (manualStepsText.isEmpty()) {
                showToast("Please enter number of steps")
                return
            }

            val manualSteps = manualStepsText.toIntOrNull()
            if (manualSteps == null || manualSteps <= 0) {
                showToast("Please enter a valid number of steps")
                return
            }

            if (manualSteps > 50000) {
                showToast("Steps count seems too high. Please enter a reasonable number.")
                return
            }

            // Add to service if bound, otherwise add locally
            if (isServiceBound && stepCounterService != null) {
                stepCounterService?.addManualSteps(manualSteps)
            } else {
                currentSteps += manualSteps
                // Update today's steps in weekly data
                val calendar = Calendar.getInstance()
                val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                if (currentDayOfWeek >= 0 && currentDayOfWeek < weeklySteps.size) {
                    weeklySteps[currentDayOfWeek] = currentSteps
                }

                // Save to SharedPreferences
                try {
                    val sharedPrefs = SharedPreferences(this)
                    sharedPrefs.setTodaySteps(currentSteps)
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving steps to SharedPreferences", e)
                }
            }

            // Add to history
            val currentTime = System.currentTimeMillis()
            stepHistory.add(0, StepEntry(manualSteps, "Manual Entry", currentTime))

            // Update UI
            updateUI()

            // Clear input
            etManualSteps.text.clear()

            showToast("Added $manualSteps steps!")
            Log.d(TAG, "Added $manualSteps steps manually")

        } catch (e: Exception) {
            Log.e(TAG, "Error adding manual steps", e)
            showToast("Error adding steps")
        }
    }

    private fun updateAddButtonState() {
        try {
            // Enable add button only if input is not empty
            btnAddSteps.isEnabled = !etManualSteps.text.toString().trim().isEmpty()

        } catch (e: Exception) {
            Log.e(TAG, "Error updating add button state", e)
        }
    }

    private fun loadSampleData() {
        try {
            // Try to load from SharedPreferences if available
            try {
                val sharedPrefs = SharedPreferences(this)

                // Load daily goal
                dailyGoal = sharedPrefs.getDailyGoal()

                // Load current steps if service is not connected yet
                if (!isServiceBound) {
                    val savedSteps = sharedPrefs.getTodaySteps()
                    if (savedSteps > 0) {
                        currentSteps = savedSteps
                    }
                }

                // Load weekly data
                val weeklyData = sharedPrefs.getAllWeeklySteps()
                for (i in weeklyData.indices) {
                    if (i < weeklySteps.size && weeklyData[i] > 0) {
                        weeklySteps[i] = weeklyData[i]
                    }
                }

                Log.d(TAG, "Loaded data from SharedPreferences - Steps: $currentSteps, Goal: $dailyGoal")
            } catch (e: Exception) {
                Log.w(TAG, "Could not load from SharedPreferences, using sample data", e)
            }

            // Add sample step entries if history is empty
            if (stepHistory.isEmpty()) {
                val calendar = Calendar.getInstance()

                // Today - current steps
                stepHistory.add(StepEntry(currentSteps, "Today's Total", calendar.timeInMillis))

                // Earlier entries for demo
                calendar.add(Calendar.HOUR_OF_DAY, -2)
                stepHistory.add(StepEntry(2000, "Manual Entry", calendar.timeInMillis))

                // Yesterday
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                stepHistory.add(StepEntry(9876, "Daily Total", calendar.timeInMillis))

                // Day before yesterday
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                stepHistory.add(StepEntry(7543, "Daily Total", calendar.timeInMillis))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading sample data", e)
        }
    }

    private fun updateUI() {
        try {
            // Update step count display
            tvStepCount.text = formatNumber(currentSteps)
            tvStepGoal.text = "of ${formatNumber(dailyGoal)} steps"

            // Calculate and update percentage
            val percentage = if (dailyGoal > 0) {
                ((currentSteps.toDouble() / dailyGoal.toDouble()) * 100).toInt()
            } else {
                0
            }
            tvStepPercentage.text = "$percentage% of daily goal"

            // Update progress bar
            progressSteps.progress = percentage.coerceAtMost(100)

            // Update daily goal card
            tvDailyGoalCount.text = formatNumber(dailyGoal)
            updateGoalStatus()

            // Calculate and update activity summary
            val distance = String.format("%.1f", currentSteps * 0.000762) // Rough calculation: 1 step ≈ 0.762 meters
            tvDistance.text = "$distance km"

            val calories = (currentSteps * 0.04).toInt() // Rough calculation: 1 step ≈ 0.04 calories
            tvCalories.text = "$calories cal"

            val activeMinutes = (currentSteps / 100) // Rough calculation: 100 steps per minute
            val hours = activeMinutes / 60
            val minutes = activeMinutes % 60
            tvActiveTime.text = "${hours}h ${minutes}m"

            // Update weekly chart
            updateWeeklyChart()

            // Update RecyclerView
            stepHistoryAdapter.notifyDataSetChanged()

            Log.d(TAG, "UI updated - Steps: $currentSteps, Goal: $dailyGoal, Percentage: $percentage%")

        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI", e)
        }
    }

    private fun updateGoalStatus() {
        try {
            val remaining = dailyGoal - currentSteps
            if (remaining <= 0) {
                tvGoalStatus.text = "🎉 Goal achieved! Great job!"
                tvGoalStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            } else {
                tvGoalStatus.text = "${formatNumber(remaining)} steps to go!"
                tvGoalStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating goal status", e)
        }
    }

    private fun updateWeeklyChart() {
        try {
            // Try to get fresh weekly data from SharedPreferences
            try {
                val sharedPrefs = SharedPreferences(this)
                val weeklyData = sharedPrefs.getAllWeeklySteps()

                // Update current day with live data
                val calendar = Calendar.getInstance()
                val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                if (currentDayOfWeek >= 0 && currentDayOfWeek < weeklySteps.size) {
                    weeklySteps[currentDayOfWeek] = currentSteps
                }

                // Use SharedPreferences data if available, otherwise use local weeklySteps
                for (i in weeklyData.indices) {
                    if (i < weeklySteps.size && weeklyData[i] > 0) {
                        weeklySteps[i] = weeklyData[i]
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not load weekly data from SharedPreferences", e)
                // Update current day with live data using local array
                val calendar = Calendar.getInstance()
                val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                if (currentDayOfWeek >= 0 && currentDayOfWeek < weeklySteps.size) {
                    weeklySteps[currentDayOfWeek] = currentSteps
                }
            }

            val stepCounts = arrayOf(tvSunCount, tvMonCount, tvTueCount, tvWedCount, tvThuCount, tvFriCount, tvSatCount)
            val progressBars = arrayOf(progressSun, progressMon, progressTue, progressWed, progressThu, progressFri, progressSat)

            var totalSteps = 0
            var maxSteps = 0

            // Get current day of week (0 = Sunday, 6 = Saturday)
            val calendar = Calendar.getInstance()
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // Convert to 0-6 format

            for (i in weeklySteps.indices) {
                val steps = weeklySteps[i]
                stepCounts[i].text = formatNumber(steps)

                // Calculate percentage based on daily goal
                val percentage = if (dailyGoal > 0) {
                    ((steps.toDouble() / dailyGoal.toDouble()) * 100).toInt()
                } else {
                    0
                }
                progressBars[i].progress = percentage.coerceAtMost(100)

                // Highlight current day
                if (i == currentDayOfWeek) {
                    stepCounts[i].setTextColor(resources.getColor(android.R.color.holo_green_dark))
                } else {
                    stepCounts[i].setTextColor(resources.getColor(android.R.color.holo_blue_dark))
                }

                totalSteps += steps
                if (steps > maxSteps) maxSteps = steps
            }

            // Update weekly summary
            val averageSteps = if (totalSteps > 0) totalSteps / weeklySteps.size else 0
            tvWeeklyAverage.text = "Average: ${formatNumber(averageSteps)} steps/day"
            tvWeeklyBest.text = "Best: ${formatNumber(maxSteps)} steps"

        } catch (e: Exception) {
            Log.e(TAG, "Error updating weekly chart", e)
        }
    }

    private fun formatNumber(number: Int): String {
        return when {
            number >= 1000 -> String.format("%,d", number)
            else -> number.toString()
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast", e)
        }
    }

    // Data class for step entries
    data class StepEntry(
        val steps: Int,
        val type: String, // "Auto Detected", "Manual Entry", "Daily Total"
        val timestamp: Long
    ) {
        fun getFormattedDate(): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        fun getFormattedTime(): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    // RecyclerView Adapter for step history
    private class StepHistoryAdapter(private val stepList: List<StepEntry>) :
        RecyclerView.Adapter<StepHistoryAdapter.StepViewHolder>() {

        class StepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvStepNumber: TextView = itemView.findViewById(R.id.tv_step_number)
            val tvStepType: TextView = itemView.findViewById(R.id.tv_step_type)
            val tvStepTime: TextView = itemView.findViewById(R.id.tv_step_time)
            val ivStepIcon: ImageView = itemView.findViewById(R.id.iv_step_icon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_step_history, parent, false)
            return StepViewHolder(view)
        }

        override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
            val stepEntry = stepList[position]

            holder.tvStepNumber.text = "${stepEntry.steps} steps"
            holder.tvStepType.text = stepEntry.type
            holder.tvStepTime.text = stepEntry.getFormattedTime()

            // Set different icons based on type
            when (stepEntry.type) {
                "Auto Detected" -> holder.ivStepIcon.setImageResource(R.drawable.ic_auto_steps)
                "Manual Entry" -> holder.ivStepIcon.setImageResource(R.drawable.ic_manual_steps)
                "Daily Total" -> holder.ivStepIcon.setImageResource(R.drawable.ic_daily_steps)
                "Today's Total" -> holder.ivStepIcon.setImageResource(R.drawable.ic_steps)
                else -> holder.ivStepIcon.setImageResource(R.drawable.ic_steps)
            }
        }

        override fun getItemCount(): Int = stepList.size
    }
}