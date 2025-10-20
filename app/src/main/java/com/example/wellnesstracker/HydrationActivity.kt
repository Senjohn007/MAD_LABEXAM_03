package com.example.wellnesstracker

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstracker.utils.ReminderManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class HydrationActivity : AppCompatActivity() {

    private val TAG = "HydrationActivity"

    // SharedPreferences constants - optimized naming
    private companion object {
        const val PREFS_NAME = "HydrationPrefs"
        const val DAILY_GOAL_KEY = "daily_goal"
        const val CURRENT_INTAKE_KEY = "current_intake"
        const val LAST_DATE_KEY = "last_date"
        const val INTAKE_HISTORY_KEY = "intake_history"
        const val DEFAULT_GOAL = 2500
        const val MAX_INTAKE_AMOUNT = 5000
        const val MAX_GOAL_AMOUNT = 10000
    }

    // UI Components
    private lateinit var toolbar: Toolbar
    private lateinit var progressCircular: ProgressBar
    private lateinit var tvCurrentIntake: TextView
    private lateinit var tvDailyGoal: TextView
    private lateinit var tvPercentage: TextView
    private lateinit var btnAdd250: Button
    private lateinit var btnAdd500: Button
    private lateinit var btnAdd750: Button
    private lateinit var etCustomAmount: EditText
    private lateinit var btnAddCustom: Button
    private lateinit var rvIntakeHistory: RecyclerView
    private lateinit var cardReminderSettings: CardView
    private lateinit var tvReminderStatus: TextView
    private lateinit var btnSetGoal: Button
    private lateinit var bottomNavigation: BottomNavigationView

    // Data variables
    private var currentIntake = 0
    private var dailyGoal = DEFAULT_GOAL
    private val intakeHistory = mutableListOf<IntakeEntry>()
    private lateinit var intakeAdapter: IntakeHistoryAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var reminderManager: ReminderManager

    // Coroutines
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "HydrationActivity onCreate started")

            setContentView(R.layout.hydration_page)

            // Initialize components
            initializeComponents()
            initializeViews()
            setupToolbar()
            setupBottomNavigation()
            setupRecyclerView()

            // Load data asynchronously
            loadSavedDataAsync()

            setupClickListeners()

            Log.d(TAG, "HydrationActivity onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in HydrationActivity onCreate", e)
            showToast("Error initializing hydration tracker: ${e.message}")
        }
    }

    private fun initializeComponents() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        reminderManager = ReminderManager(this)
    }

    private fun initializeViews() {
        try {
            toolbar = findViewById(R.id.toolbar)
            progressCircular = findViewById(R.id.progress_circular)
            tvCurrentIntake = findViewById(R.id.tv_current_intake)
            tvDailyGoal = findViewById(R.id.tv_daily_goal)
            tvPercentage = findViewById(R.id.tv_percentage)
            btnAdd250 = findViewById(R.id.btn_add_250)
            btnAdd500 = findViewById(R.id.btn_add_500)
            btnAdd750 = findViewById(R.id.btn_add_750)
            etCustomAmount = findViewById(R.id.et_custom_amount)
            btnAddCustom = findViewById(R.id.btn_add_custom)
            rvIntakeHistory = findViewById(R.id.rv_intake_history)
            cardReminderSettings = findViewById(R.id.card_reminder_settings)
            tvReminderStatus = findViewById(R.id.tv_reminder_status)
            btnSetGoal = findViewById(R.id.btn_set_goal)
            bottomNavigation = findViewById(R.id.bottom_navigation)

            Log.d(TAG, "All views initialized successfully")
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
                title = "Hydration Tracker"
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
                        navigateToActivity(StepsActivity::class.java)
                        true
                    }
                    R.id.nav_hydration -> {
                        // Already in hydration activity
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

            // Set hydration as selected
            bottomNavigation.selectedItemId = R.id.nav_hydration

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

    private fun setupRecyclerView() {
        try {
            intakeAdapter = IntakeHistoryAdapter(intakeHistory) { position ->
                showRemoveConfirmationDialog(position)
            }

            rvIntakeHistory.apply {
                layoutManager = LinearLayoutManager(this@HydrationActivity)
                adapter = intakeAdapter
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                // Add item decoration for better spacing
                addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(
                    this@HydrationActivity,
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                ))
            }

            Log.d(TAG, "RecyclerView setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun loadSavedDataAsync() {
        activityScope.launch {
            try {
                // Load data in background
                val loadedData = withContext(Dispatchers.IO) {
                    loadSavedData()
                }

                // Update UI on main thread
                updateUI()
                intakeAdapter.notifyDataSetChanged()

                Log.d(TAG, "Data loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
                showToast("Error loading saved data")
            }
        }
    }

    private suspend fun loadSavedData(): Boolean {
        return try {
            val currentDate = getCurrentDateString()
            val lastDate = sharedPreferences.getString(LAST_DATE_KEY, "")

            if (currentDate != lastDate) {
                // New day - reset intake but keep goal
                currentIntake = 0
                intakeHistory.clear()
                saveCurrentIntake()
                saveLastDate(currentDate)
                Log.d(TAG, "New day detected - intake reset")
            } else {
                // Same day - load saved intake and history
                currentIntake = sharedPreferences.getInt(CURRENT_INTAKE_KEY, 0)
                loadIntakeHistory()
            }

            // Load daily goal
            dailyGoal = sharedPreferences.getInt(DAILY_GOAL_KEY, DEFAULT_GOAL)

            Log.d(TAG, "Loaded data - Intake: ${currentIntake}ml, Goal: ${dailyGoal}ml, History: ${intakeHistory.size} entries")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error in loadSavedData", e)
            false
        }
    }

    private fun loadIntakeHistory() {
        // For simplicity, we'll reconstruct from current intake
        // In a production app, you might want to serialize/deserialize the actual history
        intakeHistory.clear()
    }

    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun setupClickListeners() {
        try {
            // Water amount buttons
            btnAdd250.setOnClickListener { addWater(250) }
            btnAdd500.setOnClickListener { addWater(500) }
            btnAdd750.setOnClickListener { addWater(750) }

            // Custom amount setup
            btnAddCustom.isEnabled = false
            btnAddCustom.setOnClickListener { addCustomWater() }

            etCustomAmount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString()?.trim()
                    btnAddCustom.isEnabled = !text.isNullOrEmpty() && text.toIntOrNull()?.let { it > 0 } == true
                }
            })

            // Settings and goal buttons
            cardReminderSettings.setOnClickListener { showReminderSettingsDialog() }
            btnSetGoal.setOnClickListener { showSetDailyGoalDialog() }

            Log.d(TAG, "Click listeners setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun addWater(amount: Int) {
        if (amount <= 0 || amount > MAX_INTAKE_AMOUNT) {
            showToast("Invalid water amount")
            return
        }

        try {
            val oldIntake = currentIntake
            currentIntake += amount

            // Add to history
            val currentTime = System.currentTimeMillis()
            intakeHistory.add(0, IntakeEntry(amount, currentTime))

            // Save data asynchronously
            activityScope.launch {
                withContext(Dispatchers.IO) {
                    saveCurrentIntake()
                }
            }

            // Update UI
            updateUI()
            intakeAdapter.notifyItemInserted(0)
            rvIntakeHistory.scrollToPosition(0)

            // Success feedback
            showToast("Added ${amount}ml water!")
            Log.d(TAG, "Water added: ${amount}ml (${oldIntake}ml → ${currentIntake}ml)")

            // Check for milestones
            checkMilestones(oldIntake, currentIntake)

        } catch (e: Exception) {
            Log.e(TAG, "Error adding water", e)
            currentIntake -= amount // Rollback
            showToast("Error adding water")
        }
    }

    private fun checkMilestones(oldIntake: Int, newIntake: Int) {
        val percentage = ((newIntake.toDouble() / dailyGoal.toDouble()) * 100).toInt()

        when {
            percentage >= 100 && ((oldIntake.toDouble() / dailyGoal.toDouble()) * 100).toInt() < 100 -> {
                showToast("🎉 Daily goal achieved! Great job!")
            }
            percentage >= 75 && ((oldIntake.toDouble() / dailyGoal.toDouble()) * 100).toInt() < 75 -> {
                showToast("🌟 75% complete! Almost there!")
            }
            percentage >= 50 && ((oldIntake.toDouble() / dailyGoal.toDouble()) * 100).toInt() < 50 -> {
                showToast("💪 Halfway to your goal!")
            }
        }
    }

    private fun showRemoveConfirmationDialog(position: Int) {
        if (position < 0 || position >= intakeHistory.size) return

        val entry = intakeHistory[position]
        AlertDialog.Builder(this)
            .setTitle("Remove Water Entry")
            .setMessage("Remove ${entry.amount}ml from your intake?")
            .setPositiveButton("Remove") { _, _ ->
                removeWaterEntry(position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun removeWaterEntry(position: Int) {
        try {
            if (position >= 0 && position < intakeHistory.size) {
                val removedEntry = intakeHistory[position]
                currentIntake = (currentIntake - removedEntry.amount).coerceAtLeast(0)
                intakeHistory.removeAt(position)

                // Save data
                activityScope.launch {
                    withContext(Dispatchers.IO) {
                        saveCurrentIntake()
                    }
                }

                // Update UI
                updateUI()
                intakeAdapter.notifyItemRemoved(position)

                showToast("Removed ${removedEntry.amount}ml")
                Log.d(TAG, "Water entry removed: ${removedEntry.amount}ml, new total: ${currentIntake}ml")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing water entry", e)
            showToast("Error removing entry")
        }
    }

    private fun addCustomWater() {
        try {
            val customAmountText = etCustomAmount.text.toString().trim()
            if (customAmountText.isEmpty()) {
                showToast("Please enter an amount")
                return
            }

            val customAmount = customAmountText.toIntOrNull()
            when {
                customAmount == null || customAmount <= 0 -> {
                    showToast("Please enter a valid amount")
                    etCustomAmount.error = "Invalid amount"
                }
                customAmount > MAX_INTAKE_AMOUNT -> {
                    showToast("Amount too large (max ${MAX_INTAKE_AMOUNT}ml)")
                    etCustomAmount.error = "Too large"
                }
                else -> {
                    addWater(customAmount)
                    etCustomAmount.text.clear()
                    etCustomAmount.error = null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding custom water", e)
            showToast("Error adding custom water")
        }
    }

    private fun updateUI() {
        try {
            // Update displays
            tvCurrentIntake.text = "${currentIntake} ml"
            tvDailyGoal.text = "of ${dailyGoal} ml"

            // Calculate percentage
            val percentage = if (dailyGoal > 0) {
                ((currentIntake.toDouble() / dailyGoal.toDouble()) * 100).toInt()
            } else 0

            tvPercentage.text = "${percentage}%"
            progressCircular.progress = percentage.coerceAtMost(100)

            // Update reminder status
            updateReminderStatus()

        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI", e)
        }
    }

    private fun showSetDailyGoalDialog() {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_goal, null)
            val etGoal = dialogView.findViewById<EditText>(R.id.et_goal)

            etGoal.setText(dailyGoal.toString())
            etGoal.selectAll()

            AlertDialog.Builder(this)
                .setTitle("Set Daily Goal")
                .setMessage("Enter your daily hydration goal (1-${MAX_GOAL_AMOUNT}ml)")
                .setView(dialogView)
                .setPositiveButton("Set Goal") { _, _ ->
                    val goalText = etGoal.text.toString().trim()
                    val newGoal = goalText.toIntOrNull()

                    when {
                        goalText.isEmpty() -> showToast("Please enter a goal amount")
                        newGoal == null || newGoal <= 0 -> showToast("Please enter a valid goal")
                        newGoal > MAX_GOAL_AMOUNT -> showToast("Goal too large (max ${MAX_GOAL_AMOUNT}ml)")
                        else -> {
                            dailyGoal = newGoal
                            activityScope.launch {
                                withContext(Dispatchers.IO) {
                                    saveDailyGoal()
                                }
                                updateUI()
                            }
                            showToast("Daily goal set to ${dailyGoal}ml!")
                            Log.d(TAG, "Daily goal updated to: ${dailyGoal}ml")
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

    private fun showReminderSettingsDialog() {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reminder_settings, null)

            // Initialize dialog components
            val switchReminders = dialogView.findViewById<Switch>(R.id.switch_reminders)
            val rgInterval = dialogView.findViewById<RadioGroup>(R.id.rg_interval)
            val rb1Hour = dialogView.findViewById<RadioButton>(R.id.rb_1_hour)
            val rb2Hours = dialogView.findViewById<RadioButton>(R.id.rb_2_hours)
            val rb3Hours = dialogView.findViewById<RadioButton>(R.id.rb_3_hours)
            val rb4Hours = dialogView.findViewById<RadioButton>(R.id.rb_4_hours)
            val btnStartTime = dialogView.findViewById<Button>(R.id.btn_start_time)
            val btnEndTime = dialogView.findViewById<Button>(R.id.btn_end_time)

            // Days checkboxes
            val dayCheckboxes = mapOf(
                1 to dialogView.findViewById<CheckBox>(R.id.cb_sunday),
                2 to dialogView.findViewById<CheckBox>(R.id.cb_monday),
                3 to dialogView.findViewById<CheckBox>(R.id.cb_tuesday),
                4 to dialogView.findViewById<CheckBox>(R.id.cb_wednesday),
                5 to dialogView.findViewById<CheckBox>(R.id.cb_thursday),
                6 to dialogView.findViewById<CheckBox>(R.id.cb_friday),
                7 to dialogView.findViewById<CheckBox>(R.id.cb_saturday)
            )

            // Load current settings
            switchReminders.isChecked = reminderManager.isRemindersEnabled()

            when (reminderManager.getReminderInterval()) {
                1 -> rb1Hour.isChecked = true
                2 -> rb2Hours.isChecked = true
                3 -> rb3Hours.isChecked = true
                4 -> rb4Hours.isChecked = true
            }

            // Set time buttons
            var startHour = reminderManager.getStartHour()
            var startMinute = reminderManager.getStartMinute()
            var endHour = reminderManager.getEndHour()
            var endMinute = reminderManager.getEndMinute()

            btnStartTime.text = reminderManager.formatTime(startHour, startMinute)
            btnEndTime.text = reminderManager.formatTime(endHour, endMinute)

            // Set active days
            val activeDays = reminderManager.getActiveDays()
            dayCheckboxes.forEach { (day, checkbox) ->
                checkbox.isChecked = activeDays.contains(day)
            }

            // Time picker listeners
            btnStartTime.setOnClickListener {
                TimePickerDialog(this, { _, hour, minute ->
                    startHour = hour
                    startMinute = minute
                    btnStartTime.text = reminderManager.formatTime(hour, minute)
                }, startHour, startMinute, false).show()
            }

            btnEndTime.setOnClickListener {
                TimePickerDialog(this, { _, hour, minute ->
                    endHour = hour
                    endMinute = minute
                    btnEndTime.text = reminderManager.formatTime(hour, minute)
                }, endHour, endMinute, false).show()
            }

            // Show dialog
            AlertDialog.Builder(this)
                .setTitle("Reminder Settings")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    saveReminderSettings(
                        switchReminders, rgInterval, dayCheckboxes,
                        startHour, startMinute, endHour, endMinute
                    )
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing reminder settings dialog", e)
            showToast("Error opening reminder settings")
        }
    }

    private fun saveReminderSettings(
        switchReminders: Switch,
        rgInterval: RadioGroup,
        dayCheckboxes: Map<Int, CheckBox>,
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int
    ) {
        try {
            val interval = when (rgInterval.checkedRadioButtonId) {
                R.id.rb_1_hour -> 1
                R.id.rb_2_hours -> 2
                R.id.rb_3_hours -> 3
                R.id.rb_4_hours -> 4
                else -> 2
            }

            val selectedDays = dayCheckboxes.filter { it.value.isChecked }.keys.toSet()

            reminderManager.saveReminderSettings(
                switchReminders.isChecked, interval,
                startHour, startMinute, endHour, endMinute, selectedDays
            )

            updateReminderStatus()
            val status = if (switchReminders.isChecked) "enabled" else "disabled"
            showToast("Reminder settings saved! Reminders $status.")

        } catch (e: Exception) {
            Log.e(TAG, "Error saving reminder settings", e)
            showToast("Error saving reminder settings")
        }
    }

    private fun updateReminderStatus() {
        try {
            tvReminderStatus.text = reminderManager.getReminderStatusText()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating reminder status", e)
        }
    }

    private fun saveDailyGoal() {
        try {
            sharedPreferences.edit().putInt(DAILY_GOAL_KEY, dailyGoal).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving daily goal", e)
        }
    }

    private fun saveCurrentIntake() {
        try {
            sharedPreferences.edit().putInt(CURRENT_INTAKE_KEY, currentIntake).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving current intake", e)
        }
    }

    private fun saveLastDate(date: String) {
        try {
            sharedPreferences.edit().putString(LAST_DATE_KEY, date).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving last date", e)
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
        // Save data when going to background
        activityScope.launch {
            withContext(Dispatchers.IO) {
                saveCurrentIntake()
                saveLastDate(getCurrentDateString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel() // Clean up coroutines
        Log.d(TAG, "HydrationActivity destroyed")
    }

    // Data class for intake history
    data class IntakeEntry(
        val amount: Int,
        val timestamp: Long
    )

    // Optimized RecyclerView Adapter
    inner class IntakeHistoryAdapter(
        private val entries: MutableList<IntakeEntry>,
        private val onDeleteClick: (Int) -> Unit
    ) : RecyclerView.Adapter<IntakeHistoryAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvWaterAmount: TextView = itemView.findViewById(R.id.tv_water_amount)
            val tvWaterTime: TextView = itemView.findViewById(R.id.tv_water_time)
            val btnDeleteWater: ImageButton = itemView.findViewById(R.id.btn_delete_water)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_hydration, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                val entry = entries[position]

                // Fixed line - direct string formatting
                holder.tvWaterAmount.text = "${entry.amount} ml"

                val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                holder.tvWaterTime.text = dateFormat.format(Date(entry.timestamp))

                holder.btnDeleteWater.setOnClickListener {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onDeleteClick(pos)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error binding view holder at position $position", e)
            }
        }

        override fun getItemCount(): Int = entries.size
    }
}
