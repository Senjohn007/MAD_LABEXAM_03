package com.example.wellnesstracker

import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var ivStepChart: ImageView
    private lateinit var btnSetGoal: Button

    // Data variables
    private var currentSteps = 8542
    private var dailyGoal = 10000
    private val stepHistory = mutableListOf<StepEntry>()
    private lateinit var stepHistoryAdapter: StepHistoryAdapter

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

            Log.d(TAG, "StepsActivity onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in StepsActivity onCreate", e)
            showToast("Error initializing step counter: ${e.message}")
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
            ivStepChart = findViewById(R.id.iv_step_chart)
            btnSetGoal = findViewById(R.id.btn_set_goal)

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
                showToast("Set daily goal - Coming Soon!")
            }

            // Chart click
            ivStepChart.setOnClickListener {
                showToast("Detailed chart view - Coming Soon!")
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

            // Add to current steps
            currentSteps += manualSteps

            // Add to history
            val currentTime = System.currentTimeMillis()
            stepHistory.add(0, StepEntry(manualSteps, "Manual Entry", currentTime))

            // Update UI
            updateUI()

            // Clear input
            etManualSteps.text.clear()

            showToast("Added $manualSteps steps!")
            Log.d(TAG, "Added $manualSteps steps manually. Current total: $currentSteps")

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
            // Add some sample step entries for demonstration
            val calendar = Calendar.getInstance()

            // Today - automatic tracking
            stepHistory.add(StepEntry(3542, "Auto Detected", calendar.timeInMillis))

            // Earlier today - manual entry
            calendar.add(Calendar.HOUR_OF_DAY, -2)
            stepHistory.add(StepEntry(2000, "Manual Entry", calendar.timeInMillis))

            // Yesterday
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            stepHistory.add(StepEntry(9876, "Daily Total", calendar.timeInMillis))

            // Day before yesterday
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            stepHistory.add(StepEntry(7543, "Daily Total", calendar.timeInMillis))

        } catch (e: Exception) {
            Log.e(TAG, "Error loading sample data", e)
        }
    }

    private fun updateUI() {
        try {
            // Update step count display
            tvStepCount.text = currentSteps.toString()
            tvStepGoal.text = "of $dailyGoal steps"

            // Calculate and update percentage
            val percentage = ((currentSteps.toDouble() / dailyGoal.toDouble()) * 100).toInt()
            tvStepPercentage.text = "$percentage% of daily goal"

            // Update progress bar
            progressSteps.progress = percentage.coerceAtMost(100)

            // Calculate and update activity summary
            val distance = (currentSteps * 0.000762).toString().take(3) // Rough calculation: 1 step ≈ 0.762 meters
            tvDistance.text = "$distance km"

            val calories = (currentSteps * 0.04).toInt() // Rough calculation: 1 step ≈ 0.04 calories
            tvCalories.text = "$calories cal"

            val activeMinutes = (currentSteps / 100) // Rough calculation: 100 steps per minute
            val hours = activeMinutes / 60
            val minutes = activeMinutes % 60
            tvActiveTime.text = "${hours}h ${minutes}m"

            // Update RecyclerView
            stepHistoryAdapter.notifyDataSetChanged()

            Log.d(TAG, "UI updated - Steps: $currentSteps, Goal: $dailyGoal, Percentage: $percentage%")

        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI", e)
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
                else -> holder.ivStepIcon.setImageResource(R.drawable.ic_steps)
            }
        }

        override fun getItemCount(): Int = stepList.size
    }
}
