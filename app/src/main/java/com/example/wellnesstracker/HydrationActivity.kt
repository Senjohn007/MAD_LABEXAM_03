package com.example.wellnesstracker

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstracker.utils.ReminderManager
import java.text.SimpleDateFormat
import java.util.*

class HydrationActivity : AppCompatActivity() {

    private val TAG = "HydrationActivity"

    // SharedPreferences constants
    private val PREFS_NAME = "HydrationPrefs"
    private val DAILY_GOAL_KEY = "daily_goal"
    private val CURRENT_INTAKE_KEY = "current_intake"
    private val LAST_DATE_KEY = "last_date"

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

    // Data variables
    private var currentIntake = 0
    private var dailyGoal = 2500 // Default goal
    private val intakeHistory = mutableListOf<IntakeEntry>()
    private lateinit var intakeAdapter: IntakeHistoryAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var reminderManager: ReminderManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "HydrationActivity onCreate started")

            // Initialize SharedPreferences and ReminderManager
            sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            reminderManager = ReminderManager(this)

            // Set the content view to use hydration_page.xml
            setContentView(R.layout.hydration_page)

            initializeViews()
            setupToolbar()
            setupRecyclerView()
            loadSavedData() // Load saved data before setting up UI
            setupClickListeners()
            updateUI()

            Log.d(TAG, "HydrationActivity onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in HydrationActivity onCreate", e)
            showToast("Error initializing hydration tracker: ${e.message}")
        }
    }

    private fun initializeViews() {
        try {
            // Initialize all views
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

            Log.d(TAG, "All views initialized successfully")

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

    private fun setupRecyclerView() {
        try {
            // Initialize the adapter with empty list
            intakeAdapter = IntakeHistoryAdapter(intakeHistory) { position ->
                removeWaterEntry(position)
            }

            // Set layout manager and adapter
            rvIntakeHistory.layoutManager = LinearLayoutManager(this)
            rvIntakeHistory.adapter = intakeAdapter

            Log.d(TAG, "RecyclerView setup completed")

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun loadSavedData() {
        try {
            // Check if it's a new day - if so, reset intake
            val currentDate = getCurrentDateString()
            val lastDate = sharedPreferences.getString(LAST_DATE_KEY, "")

            if (currentDate != lastDate) {
                // New day - reset intake but keep goal
                currentIntake = 0
                intakeHistory.clear()
                saveCurrentIntake()
                saveLastDate(currentDate)
            } else {
                // Same day - load saved intake
                currentIntake = sharedPreferences.getInt(CURRENT_INTAKE_KEY, 0)
            }

            // Load daily goal
            dailyGoal = sharedPreferences.getInt(DAILY_GOAL_KEY, 2500)

            Log.d(TAG, "Loaded data - Intake: ${currentIntake}ml, Goal: ${dailyGoal}ml")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved data", e)
        }
    }

    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun setupClickListeners() {
        try {
            // Add water buttons
            btnAdd250.setOnClickListener {
                addWater(250)
            }

            btnAdd500.setOnClickListener {
                addWater(500)
            }

            btnAdd750.setOnClickListener {
                addWater(750)
            }

            // Custom amount button - initially disabled
            btnAddCustom.isEnabled = false
            btnAddCustom.setOnClickListener {
                addCustomWater()
            }

            // Custom amount EditText listener
            etCustomAmount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    btnAddCustom.isEnabled = !s.isNullOrEmpty() && s.toString().trim().isNotEmpty()
                }
            })

            // Reminder settings card - NOW FUNCTIONAL!
            cardReminderSettings.setOnClickListener {
                showReminderSettingsDialog()
            }

            // Set goal button - FUNCTIONAL!
            btnSetGoal.setOnClickListener {
                showSetDailyGoalDialog()
            }

            Log.d(TAG, "Click listeners setup completed")

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun showReminderSettingsDialog() {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reminder_settings, null)

            // Get all UI components from dialog
            val switchReminders = dialogView.findViewById<Switch>(R.id.switch_reminders)
            val rgInterval = dialogView.findViewById<RadioGroup>(R.id.rg_interval)
            val rb1Hour = dialogView.findViewById<RadioButton>(R.id.rb_1_hour)
            val rb2Hours = dialogView.findViewById<RadioButton>(R.id.rb_2_hours)
            val rb3Hours = dialogView.findViewById<RadioButton>(R.id.rb_3_hours)
            val rb4Hours = dialogView.findViewById<RadioButton>(R.id.rb_4_hours)
            val btnStartTime = dialogView.findViewById<Button>(R.id.btn_start_time)
            val btnEndTime = dialogView.findViewById<Button>(R.id.btn_end_time)

            // Days checkboxes
            val cbSunday = dialogView.findViewById<CheckBox>(R.id.cb_sunday)
            val cbMonday = dialogView.findViewById<CheckBox>(R.id.cb_monday)
            val cbTuesday = dialogView.findViewById<CheckBox>(R.id.cb_tuesday)
            val cbWednesday = dialogView.findViewById<CheckBox>(R.id.cb_wednesday)
            val cbThursday = dialogView.findViewById<CheckBox>(R.id.cb_thursday)
            val cbFriday = dialogView.findViewById<CheckBox>(R.id.cb_friday)
            val cbSaturday = dialogView.findViewById<CheckBox>(R.id.cb_saturday)

            // Load current settings
            switchReminders.isChecked = reminderManager.isRemindersEnabled()

            when (reminderManager.getReminderInterval()) {
                1 -> rb1Hour.isChecked = true
                2 -> rb2Hours.isChecked = true
                3 -> rb3Hours.isChecked = true
                4 -> rb4Hours.isChecked = true
            }

            // Set time buttons text
            btnStartTime.text = reminderManager.formatTime(reminderManager.getStartHour(), reminderManager.getStartMinute())
            btnEndTime.text = reminderManager.formatTime(reminderManager.getEndHour(), reminderManager.getEndMinute())

            // Set active days
            val activeDays = reminderManager.getActiveDays()
            cbSunday.isChecked = activeDays.contains(1)
            cbMonday.isChecked = activeDays.contains(2)
            cbTuesday.isChecked = activeDays.contains(3)
            cbWednesday.isChecked = activeDays.contains(4)
            cbThursday.isChecked = activeDays.contains(5)
            cbFriday.isChecked = activeDays.contains(6)
            cbSaturday.isChecked = activeDays.contains(7)

            // Time picker variables
            var startHour = reminderManager.getStartHour()
            var startMinute = reminderManager.getStartMinute()
            var endHour = reminderManager.getEndHour()
            var endMinute = reminderManager.getEndMinute()

            // Start time picker
            btnStartTime.setOnClickListener {
                TimePickerDialog(this, { _, hour, minute ->
                    startHour = hour
                    startMinute = minute
                    btnStartTime.text = reminderManager.formatTime(hour, minute)
                }, startHour, startMinute, false).show()
            }

            // End time picker
            btnEndTime.setOnClickListener {
                TimePickerDialog(this, { _, hour, minute ->
                    endHour = hour
                    endMinute = minute
                    btnEndTime.text = reminderManager.formatTime(hour, minute)
                }, endHour, endMinute, false).show()
            }

            // Create and show dialog
            AlertDialog.Builder(this)
                .setTitle("Reminder Settings")
                .setView(dialogView)
                .setPositiveButton("Save") { dialog, _ ->
                    // Get selected interval
                    val interval = when (rgInterval.checkedRadioButtonId) {
                        R.id.rb_1_hour -> 1
                        R.id.rb_2_hours -> 2
                        R.id.rb_3_hours -> 3
                        R.id.rb_4_hours -> 4
                        else -> 2
                    }

                    // Get selected days
                    val selectedDays = mutableSetOf<Int>()
                    if (cbSunday.isChecked) selectedDays.add(1)
                    if (cbMonday.isChecked) selectedDays.add(2)
                    if (cbTuesday.isChecked) selectedDays.add(3)
                    if (cbWednesday.isChecked) selectedDays.add(4)
                    if (cbThursday.isChecked) selectedDays.add(5)
                    if (cbFriday.isChecked) selectedDays.add(6)
                    if (cbSaturday.isChecked) selectedDays.add(7)

                    // Save settings
                    reminderManager.saveReminderSettings(
                        switchReminders.isChecked,
                        interval,
                        startHour,
                        startMinute,
                        endHour,
                        endMinute,
                        selectedDays
                    )

                    // Update UI
                    updateReminderStatus()

                    val status = if (switchReminders.isChecked) "enabled" else "disabled"
                    showToast("Reminder settings saved! Reminders $status.")

                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing reminder settings dialog", e)
            showToast("Error opening reminder settings")
        }
    }

    private fun updateReminderStatus() {
        tvReminderStatus.text = reminderManager.getReminderStatusText()
    }

    private fun showSetDailyGoalDialog() {
        try {
            // Create custom dialog layout
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_goal, null)
            val etGoal = dialogView.findViewById<EditText>(R.id.et_goal)

            // Set current goal as hint/default
            etGoal.setText(dailyGoal.toString())
            etGoal.selectAll()

            AlertDialog.Builder(this)
                .setTitle("Set Daily Goal")
                .setMessage("Enter your daily hydration goal in milliliters (ml)")
                .setView(dialogView)
                .setPositiveButton("Set Goal") { dialog, _ ->
                    val goalText = etGoal.text.toString().trim()
                    if (goalText.isNotEmpty()) {
                        val newGoal = goalText.toIntOrNull()
                        if (newGoal != null && newGoal > 0 && newGoal <= 10000) {
                            dailyGoal = newGoal
                            saveDailyGoal()
                            updateUI()
                            showToast("Daily goal set to ${dailyGoal}ml!")
                            Log.d(TAG, "Daily goal updated to: ${dailyGoal}ml")
                        } else {
                            showToast("Please enter a valid goal between 1-10000 ml")
                        }
                    } else {
                        showToast("Please enter a goal amount")
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing set goal dialog", e)
            showToast("Error opening goal setting dialog")
        }
    }

    private fun addWater(amount: Int) {
        try {
            currentIntake += amount

            // Add to history (add at the beginning of the list)
            val currentTime = System.currentTimeMillis()
            intakeHistory.add(0, IntakeEntry(amount, currentTime))

            // Save current intake
            saveCurrentIntake()

            // Update UI
            updateUI()

            // Notify adapter of the new item insertion
            intakeAdapter.notifyItemInserted(0)

            // Scroll to top to show the new item
            rvIntakeHistory.scrollToPosition(0)

            showToast("Added ${amount}ml water!")
            Log.d(TAG, "Added ${amount}ml water. Current intake: ${currentIntake}ml, History size: ${intakeHistory.size}")

        } catch (e: Exception) {
            Log.e(TAG, "Error adding water", e)
            showToast("Error adding water")
        }
    }

    private fun removeWaterEntry(position: Int) {
        try {
            if (position >= 0 && position < intakeHistory.size) {
                val removedEntry = intakeHistory[position]
                currentIntake -= removedEntry.amount
                intakeHistory.removeAt(position)

                // Save current intake
                saveCurrentIntake()

                // Update UI
                updateUI()
                intakeAdapter.notifyItemRemoved(position)

                showToast("Removed ${removedEntry.amount}ml water")
                Log.d(TAG, "Removed ${removedEntry.amount}ml water. Current intake: ${currentIntake}ml")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing water entry", e)
            showToast("Error removing water entry")
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
            if (customAmount == null || customAmount <= 0) {
                showToast("Please enter a valid amount")
                return
            }

            if (customAmount > 5000) {
                showToast("Amount too large. Please enter a reasonable amount.")
                return
            }

            addWater(customAmount)
            etCustomAmount.text.clear()

        } catch (e: Exception) {
            Log.e(TAG, "Error adding custom water", e)
            showToast("Error adding custom water")
        }
    }

    private fun updateUI() {
        try {
            // Update current intake display
            tvCurrentIntake.text = "${currentIntake} ml"
            tvDailyGoal.text = "of ${dailyGoal} ml"

            // Calculate and update percentage
            val percentage = if (dailyGoal > 0) {
                ((currentIntake.toDouble() / dailyGoal.toDouble()) * 100).toInt()
            } else {
                0
            }
            tvPercentage.text = "${percentage}%"

            // Update progress bar
            progressCircular.progress = percentage.coerceAtMost(100)

            // Update reminder status
            updateReminderStatus()

            Log.d(TAG, "UI updated - Intake: ${currentIntake}ml, Goal: ${dailyGoal}ml, Percentage: ${percentage}%")

        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI", e)
        }
    }

    private fun saveDailyGoal() {
        try {
            val editor = sharedPreferences.edit()
            editor.putInt(DAILY_GOAL_KEY, dailyGoal)
            editor.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving daily goal", e)
        }
    }

    private fun saveCurrentIntake() {
        try {
            val editor = sharedPreferences.edit()
            editor.putInt(CURRENT_INTAKE_KEY, currentIntake)
            editor.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving current intake", e)
        }
    }

    private fun saveLastDate(date: String) {
        try {
            val editor = sharedPreferences.edit()
            editor.putString(LAST_DATE_KEY, date)
            editor.apply()
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

    override fun onPause() {
        super.onPause()
        // Save current intake when app goes to background
        saveCurrentIntake()
        saveLastDate(getCurrentDateString())
    }

    // Data class for intake history
    data class IntakeEntry(
        val amount: Int,
        val timestamp: Long
    )

    // RecyclerView Adapter for intake history
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
            Log.d(TAG, "ViewHolder created")
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = entries[position]

            // Set water amount
            holder.tvWaterAmount.text = "${entry.amount} ml"

            // Format and set time
            val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            holder.tvWaterTime.text = dateFormat.format(Date(entry.timestamp))

            // Set delete button click listener
            holder.btnDeleteWater.setOnClickListener {
                onDeleteClick(holder.adapterPosition)
            }

            Log.d(TAG, "ViewHolder bound at position $position with amount ${entry.amount}ml")
        }

        override fun getItemCount(): Int {
            Log.d(TAG, "getItemCount: ${entries.size}")
            return entries.size
        }
    }
}
