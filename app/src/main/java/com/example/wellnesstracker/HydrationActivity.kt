package com.example.wellnesstracker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HydrationActivity : AppCompatActivity() {

    private val TAG = "HydrationActivity"

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
    private var currentIntake = 1000 // ml
    private var dailyGoal = 2500 // ml
    private val intakeHistory = mutableListOf<IntakeEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "HydrationActivity onCreate started")

            // Set the content view to use hydration_page.xml
            setContentView(R.layout.hydration_page)

            initializeViews()
            setupToolbar()
            setupClickListeners()
            setupRecyclerView()
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

            // Custom amount button
            btnAddCustom.setOnClickListener {
                addCustomWater()
            }

            // Custom amount EditText listener
            etCustomAmount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    btnAddCustom.isEnabled = !s.isNullOrEmpty()
                }
            })

            // Reminder settings card
            cardReminderSettings.setOnClickListener {
                showToast("Reminder settings - Coming Soon!")
            }

            // Set goal button
            btnSetGoal.setOnClickListener {
                showToast("Set daily goal - Coming Soon!")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            rvIntakeHistory.layoutManager = LinearLayoutManager(this)
            // Note: You'll need to create IntakeHistoryAdapter later
            // rvIntakeHistory.adapter = IntakeHistoryAdapter(intakeHistory)

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun addWater(amount: Int) {
        try {
            currentIntake += amount

            // Add to history
            val currentTime = System.currentTimeMillis()
            intakeHistory.add(0, IntakeEntry(amount, currentTime))

            // Update UI
            updateUI()

            showToast("Added ${amount}ml water!")
            Log.d(TAG, "Added ${amount}ml water. Current intake: ${currentIntake}ml")

        } catch (e: Exception) {
            Log.e(TAG, "Error adding water", e)
            showToast("Error adding water")
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
            val percentage = ((currentIntake.toDouble() / dailyGoal.toDouble()) * 100).toInt()
            tvPercentage.text = "${percentage}%"

            // Update progress bar
            progressCircular.progress = percentage.coerceAtMost(100)

            Log.d(TAG, "UI updated - Intake: ${currentIntake}ml, Goal: ${dailyGoal}ml, Percentage: ${percentage}%")

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

    // Data class for intake history
    data class IntakeEntry(
        val amount: Int,
        val timestamp: Long
    )
}
