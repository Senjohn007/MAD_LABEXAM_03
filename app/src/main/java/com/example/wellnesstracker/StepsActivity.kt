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
import android.view.MenuItem
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class StepsActivity : AppCompatActivity() {

    private val TAG = "StepsActivity"

    // Companion object for constants
    private companion object {
        const val ACTIVITY_RECOGNITION_REQUEST_CODE = 100
        const val DEFAULT_DAILY_GOAL = 10000
        const val MAX_MANUAL_STEPS = 50000
        const val MAX_GOAL_STEPS = 100000
        const val STEP_TO_KM_RATIO = 0.000762
        const val STEP_TO_CALORIE_RATIO = 0.04
        const val STEPS_PER_MINUTE = 100
    }

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
    private lateinit var tvStepHistoryTitle: TextView
    private lateinit var tvWeeklyAverage: TextView
    private lateinit var tvWeeklyBest: TextView
    private lateinit var btnSetGoal: Button
    private lateinit var tvDailyGoalCount: TextView
    private lateinit var tvGoalStatus: TextView
    private lateinit var bottomNavigation: BottomNavigationView

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

    // Data variables
    private var currentSteps = 0
    private var dailyGoal = DEFAULT_DAILY_GOAL
    private val stepHistory = mutableListOf<StepEntry>()
    private val weeklySteps = IntArray(7) // Sun=0, Mon=1, ..., Sat=6

    // Adapter
    private lateinit var stepHistoryAdapter: StepHistoryAdapter

    // Service-related variables
    private var stepCounterService: StepCounterService? = null
    private var isServiceBound = false

    // Coroutines
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // SharedPreferences manager
    private lateinit var sharedPrefs: SharedPreferences

    // Service connection
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val binder = service as StepCounterService.StepCounterBinder
                stepCounterService = binder.getService()
                isServiceBound = true

                // Set up callback for real-time updates
                stepCounterService?.onStepCountChanged = { steps ->
                    runOnUiThread {
                        updateSteps(steps)
                    }
                }

                // Get current steps from service
                val serviceSteps = stepCounterService?.getCurrentSteps() ?: 0
                if (serviceSteps > currentSteps) {
                    updateSteps(serviceSteps)
                }

                Log.d(TAG, "Service connected. Current steps: $currentSteps")
            } catch (e: Exception) {
                Log.e(TAG, "Error in service connection", e)
                showToast("Error connecting to step counter service")
            }
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
            setContentView(R.layout.steps_page)

            initializeComponents()
            initializeViews()
            setupToolbar()
            setupBottomNavigation()
            setupClickListeners()
            setupRecyclerView()

            // Load data asynchronously
            loadDataAsync()

            // Start step counting service
            checkAndRequestPermissions()

            Log.d(TAG, "StepsActivity onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in StepsActivity onCreate", e)
            showToast("Error initializing step counter: ${e.message}")
        }
    }

    private fun initializeComponents() {
        sharedPrefs = SharedPreferences(this)
    }

    private fun initializeViews() {
        try {
            // Main UI components
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
            tvStepHistoryTitle = findViewById(R.id.tv_step_history_title)
            tvWeeklyAverage = findViewById(R.id.tv_weekly_average)
            tvWeeklyBest = findViewById(R.id.tv_weekly_best)
            btnSetGoal = findViewById(R.id.btn_set_goal)
            tvDailyGoalCount = findViewById(R.id.tv_daily_goal_count)
            tvGoalStatus = findViewById(R.id.tv_goal_status)
            bottomNavigation = findViewById(R.id.bottom_navigation)

            // Weekly Chart Components
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

            // Initially disable add button
            btnAddSteps.isEnabled = false

            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            throw e
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
                title = "Step Counter"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    private fun setupBottomNavigation() {
        try {
            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        navigateToActivity(MainActivity::class.java)
                        true
                    }
                    R.id.nav_steps -> {
                        // Already in steps activity
                        true
                    }
                    R.id.nav_hydration -> {
                        navigateToActivity(HydrationActivity::class.java)
                        true
                    }
                    R.id.nav_mood -> {
                        navigateToActivity(MoodActivity::class.java)
                        true
                    }
                    R.id.nav_habits -> {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("navigate_to", "habits")
                        startActivity(intent)
                        finish()
                        true
                    }
                    else -> false
                }
            }

            // Set steps as selected
            bottomNavigation.selectedItemId = R.id.nav_steps

            Log.d(TAG, "Bottom navigation setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up bottom navigation", e)
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        try {
            val intent = Intent(this, activityClass)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to activity: ${activityClass.simpleName}", e)
            showToast("Navigation error")
        }
    }

    private fun setupClickListeners() {
        try {
            btnAddSteps.setOnClickListener {
                if (btnAddSteps.isEnabled) {
                    addManualSteps()
                }
            }

            btnSetGoal.setOnClickListener {
                showSetGoalDialog()
            }

            etManualSteps.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    updateAddButtonState()
                }
            })

            Log.d(TAG, "Click listeners setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            // Step history RecyclerView
            stepHistoryAdapter = StepHistoryAdapter(stepHistory)
            rvStepHistory.apply {
                layoutManager = LinearLayoutManager(this@StepsActivity)
                adapter = stepHistoryAdapter
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
            }

            Log.d(TAG, "RecyclerView setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun loadDataAsync() {
        activityScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    loadSavedData()
                }

                // Update UI on main thread
                updateUI()

            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
                showToast("Error loading step data")
            }
        }
    }

    private suspend fun loadSavedData() {
        try {
            // Load daily goal
            dailyGoal = sharedPrefs.getDailyGoal().takeIf { it > 0 } ?: DEFAULT_DAILY_GOAL

            // Load current steps if service not connected
            if (!isServiceBound) {
                currentSteps = sharedPrefs.getTodaySteps()
            }

            // Load weekly data
            val weeklyData = sharedPrefs.getAllWeeklySteps()
            for (i in weeklyData.indices) {
                if (i < weeklySteps.size) {
                    weeklySteps[i] = weeklyData[i]
                }
            }

            // Update current day
            val calendar = Calendar.getInstance()
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            if (currentDayOfWeek in 0 until weeklySteps.size) {
                weeklySteps[currentDayOfWeek] = currentSteps
            }

            // Load step history (sample data if empty)
            if (stepHistory.isEmpty()) {
                loadSampleStepHistory()
            }

            Log.d(TAG, "Data loaded - Steps: $currentSteps, Goal: $dailyGoal")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved data", e)
        }
    }

    private fun loadSampleStepHistory() {
        val calendar = Calendar.getInstance()

        // Today's total
        stepHistory.add(StepEntry(currentSteps, "Today's Total", calendar.timeInMillis))

        // Sample entries
        calendar.add(Calendar.HOUR_OF_DAY, -2)
        stepHistory.add(StepEntry(1500, "Manual Entry", calendar.timeInMillis))

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        stepHistory.add(StepEntry(8765, "Daily Total", calendar.timeInMillis))

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        stepHistory.add(StepEntry(9234, "Daily Total", calendar.timeInMillis))
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {

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

    private fun startStepCountingService() {
        try {
            val serviceIntent = Intent(this, StepCounterService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "Step counting service started and bound")

        } catch (e: Exception) {
            Log.e(TAG, "Error starting step counting service", e)
            showToast("Error starting step counter")
        }
    }

    private fun showSetGoalDialog() {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_goal, null)
            val etGoal = dialogView.findViewById<EditText>(R.id.et_goal)

            etGoal.setText(dailyGoal.toString())
            etGoal.selectAll()

            AlertDialog.Builder(this)
                .setTitle("Set Daily Step Goal")
                .setMessage("Enter your daily step goal (1-${MAX_GOAL_STEPS} steps)")
                .setView(dialogView)
                .setPositiveButton("Set Goal") { _, _ ->
                    val goalText = etGoal.text.toString().trim()
                    val newGoal = goalText.toIntOrNull()

                    when {
                        goalText.isEmpty() -> showToast("Please enter a goal")
                        newGoal == null || newGoal <= 0 -> showToast("Please enter a valid goal")
                        newGoal > MAX_GOAL_STEPS -> showToast("Goal too large (max $MAX_GOAL_STEPS)")
                        else -> {
                            dailyGoal = newGoal
                            activityScope.launch {
                                withContext(Dispatchers.IO) {
                                    sharedPrefs.setDailyGoal(dailyGoal)
                                }
                                updateUI()
                            }
                            showToast("Daily goal set to ${formatNumber(dailyGoal)} steps!")
                            Log.d(TAG, "Daily goal updated to: $dailyGoal")
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()

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
            when {
                manualSteps == null || manualSteps <= 0 -> {
                    showToast("Please enter a valid number of steps")
                    etManualSteps.error = "Invalid number"
                    return
                }
                manualSteps > MAX_MANUAL_STEPS -> {
                    showToast("Steps count too high (max $MAX_MANUAL_STEPS)")
                    etManualSteps.error = "Too high"
                    return
                }
            }

            // Add steps to service or locally
            if (isServiceBound && stepCounterService != null) {
                stepCounterService?.addManualSteps(manualSteps)
            } else {
                updateSteps(currentSteps + manualSteps)
            }

            // Add to history
            val currentTime = System.currentTimeMillis()
            stepHistory.add(0, StepEntry(manualSteps, "Manual Entry", currentTime))

            // Save data
            activityScope.launch {
                withContext(Dispatchers.IO) {
                    sharedPrefs.setTodaySteps(currentSteps)
                }
            }

            // Update UI
            updateUI()

            // Clear input
            etManualSteps.text.clear()
            etManualSteps.error = null

            showToast("Added ${formatNumber(manualSteps)} steps!")
            Log.d(TAG, "Added $manualSteps steps manually")

        } catch (e: Exception) {
            Log.e(TAG, "Error adding manual steps", e)
            showToast("Error adding steps")
        }
    }

    private fun updateSteps(newSteps: Int) {
        if (newSteps != currentSteps) {
            currentSteps = newSteps

            // Update current day in weekly data
            val calendar = Calendar.getInstance()
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            if (currentDayOfWeek in 0 until weeklySteps.size) {
                weeklySteps[currentDayOfWeek] = currentSteps
            }

            updateUI()
        }
    }

    private fun updateAddButtonState() {
        try {
            val text = etManualSteps.text.toString().trim()
            val steps = text.toIntOrNull()
            btnAddSteps.isEnabled = steps != null && steps > 0 && steps <= MAX_MANUAL_STEPS
        } catch (e: Exception) {
            Log.e(TAG, "Error updating add button state", e)
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
            } else 0

            tvStepPercentage.text = "$percentage% of daily goal"
            progressSteps.progress = percentage.coerceAtMost(100)

            // Update daily goal card
            tvDailyGoalCount.text = formatNumber(dailyGoal)
            updateGoalStatus()

            // Update activity summary
            updateActivitySummary()

            // Update weekly chart
            updateWeeklyChart()

            // Update history
            stepHistoryAdapter.notifyDataSetChanged()

            // Update visibility
            tvStepHistoryTitle.visibility = if (stepHistory.isNotEmpty()) View.VISIBLE else View.GONE

            Log.d(TAG, "UI updated - Steps: $currentSteps, Percentage: $percentage%")

        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI", e)
        }
    }

    private fun updateGoalStatus() {
        try {
            val remaining = dailyGoal - currentSteps
            if (remaining <= 0) {
                tvGoalStatus.text = "🎉 Goal achieved! Great job!"
                tvGoalStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            } else {
                tvGoalStatus.text = "${formatNumber(remaining)} steps to go!"
                tvGoalStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating goal status", e)
        }
    }

    private fun updateActivitySummary() {
        try {
            // Distance calculation
            val distanceKm = currentSteps * STEP_TO_KM_RATIO
            tvDistance.text = String.format("%.1f km", distanceKm)

            // Calories calculation
            val calories = (currentSteps * STEP_TO_CALORIE_RATIO).toInt()
            tvCalories.text = "$calories cal"

            // Active time calculation
            val activeMinutes = currentSteps / STEPS_PER_MINUTE
            val hours = activeMinutes / 60
            val minutes = activeMinutes % 60
            tvActiveTime.text = "${hours}h ${minutes}m"

        } catch (e: Exception) {
            Log.e(TAG, "Error updating activity summary", e)
        }
    }

    private fun updateWeeklyChart() {
        try {
            val stepCounts = arrayOf(tvSunCount, tvMonCount, tvTueCount, tvWedCount, tvThuCount, tvFriCount, tvSatCount)
            val progressBars = arrayOf(progressSun, progressMon, progressTue, progressWed, progressThu, progressFri, progressSat)

            var totalSteps = 0
            var maxSteps = 0

            // Get current day of week (0 = Sunday, 6 = Saturday)
            val calendar = Calendar.getInstance()
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

            for (i in weeklySteps.indices) {
                val steps = weeklySteps[i]
                stepCounts[i].text = when {
                    steps >= 1000 -> String.format("%.1fK", steps / 1000.0)
                    else -> steps.toString()
                }

                // Calculate percentage based on daily goal
                val percentage = if (dailyGoal > 0) {
                    ((steps.toDouble() / dailyGoal.toDouble()) * 100).toInt()
                } else 0
                progressBars[i].progress = percentage.coerceAtMost(100)

                // Highlight current day
                val textColor = if (i == currentDayOfWeek) {
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)
                } else {
                    ContextCompat.getColor(this, android.R.color.holo_blue_dark)
                }
                stepCounts[i].setTextColor(textColor)

                totalSteps += steps
                if (steps > maxSteps) maxSteps = steps
            }

            // Update weekly summary
            val nonZeroSteps = weeklySteps.filter { it > 0 }
            val averageSteps = if (nonZeroSteps.isNotEmpty()) nonZeroSteps.average().toInt() else 0

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

    // Activity lifecycle methods
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        // Save current data
        activityScope.launch {
            withContext(Dispatchers.IO) {
                sharedPrefs.setTodaySteps(currentSteps)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unbind service
        if (isServiceBound) {
            try {
                unbindService(serviceConnection)
                isServiceBound = false
                Log.d(TAG, "Service unbound successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service", e)
            }
        }

        // Cancel coroutines
        activityScope.cancel()
        Log.d(TAG, "StepsActivity destroyed")
    }

    // Data class for step entries
    data class StepEntry(
        val steps: Int,
        val type: String,
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

    // Step History Adapter
    private class StepHistoryAdapter(
        private val stepList: MutableList<StepEntry>
    ) : RecyclerView.Adapter<StepHistoryAdapter.StepViewHolder>() {

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
            try {
                val stepEntry = stepList[position]

                holder.tvStepNumber.text = "${stepEntry.steps} steps"
                holder.tvStepType.text = stepEntry.type
                holder.tvStepTime.text = stepEntry.getFormattedTime()

                // Set appropriate icons
                val iconRes = when (stepEntry.type) {
                    "Auto Detected" -> R.drawable.ic_auto_steps
                    "Manual Entry" -> R.drawable.ic_manual_steps
                    "Daily Total" -> R.drawable.ic_daily_steps
                    "Today's Total" -> R.drawable.ic_steps
                    else -> R.drawable.ic_steps
                }

                try {
                    holder.ivStepIcon.setImageResource(iconRes)
                } catch (e: Exception) {
                    // Fallback to default icon if drawable doesn't exist
                    holder.ivStepIcon.setImageResource(android.R.drawable.ic_menu_compass)
                }

            } catch (e: Exception) {
                Log.e("StepHistoryAdapter", "Error binding view holder", e)
            }
        }

        override fun getItemCount(): Int = stepList.size
    }
}
