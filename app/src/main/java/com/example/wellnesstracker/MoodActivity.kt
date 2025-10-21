package com.example.wellnesstracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstracker.models.MoodEntry
import com.example.wellnesstracker.services.MoodManager
import com.example.wellnesstracker.utils.DateUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class MoodActivity : AppCompatActivity() {

    private val TAG = "MoodActivity"

    // Companion object for constants
    private companion object {
        const val CHART_ANIMATION_DURATION = 1000
        const val MOOD_HISTORY_DAYS = 7
        const val TREND_THRESHOLD = 0.5
        const val INSIGHTS_MIN_ENTRIES = 3
    }

    // Backend
    private lateinit var moodManager: MoodManager

    // UI Components
    private lateinit var toolbar: Toolbar
    private lateinit var tvMoodVeryHappy: TextView
    private lateinit var tvMoodHappy: TextView
    private lateinit var tvMoodNeutral: TextView
    private lateinit var tvMoodSad: TextView
    private lateinit var tvMoodVerySad: TextView
    private lateinit var etMoodNote: EditText
    private lateinit var btnSaveMood: Button
    private lateinit var rvMoodHistory: RecyclerView
    private lateinit var lineChart: LineChart
    private lateinit var tvMoodHistory: TextView
    private lateinit var tvMostCommonMood: TextView
    private lateinit var tvMoodTrend: TextView
    private lateinit var bottomNavigation: BottomNavigationView

    // New UI Components from layout
    private lateinit var tvCurrentDate: TextView
    private lateinit var btnPreviousDay: ImageButton
    private lateinit var btnNextDay: ImageButton
    private lateinit var btnMorning: Button
    private lateinit var btnAfternoon: Button
    private lateinit var btnEvening: Button
    private lateinit var chipGroupFactors: ChipGroup
    private lateinit var btnPeriodWeek: Button
    private lateinit var btnPeriodMonth: Button
    private lateinit var btnPeriodYear: Button

    // Additional views from layout
    private lateinit var tvAverageMood: TextView
    private lateinit var tvTopFactor: TextView
    private lateinit var tvTodayEntries: TextView

    // Data and state
    private var selectedMood: MoodType? = null
    private var selectedTimeOfDay: String? = null
    private lateinit var adapter: MoodHistoryAdapter
    private val emojiViews = mutableListOf<TextView>()
    private var isDataLoading = false
    private var currentDate = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    private val timeOfDayFormat = SimpleDateFormat("HH", Locale.getDefault())

    // Coroutines
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    enum class MoodType(val emoji: String, val label: String, val level: Int, val color: String) {
        VERY_HAPPY("😄", "Great", 5, "#4CAF50"),
        HAPPY("😊", "Good", 4, "#8BC34A"),
        NEUTRAL("😐", "Okay", 3, "#FF9800"),
        SAD("😔", "Sad", 2, "#FF5722"),
        VERY_SAD("😢", "Awful", 1, "#F44336")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "MoodActivity onCreate started")
            setContentView(R.layout.mood_page)

            initializeComponents()
            initializeViews()
            setupToolbar()
            setupBottomNavigation()
            setupMoodSelectors()
            setupClickListeners()
            setupRecyclerView()
            setupChart()
            setupDateNavigation()
            setupTimeOfDaySelection()
            setupPeriodSelection()
            setupFactorsSelection()

            // Load data asynchronously
            loadDataAsync()

            Log.d(TAG, "MoodActivity onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            showToast("Error initializing mood tracker: ${e.message}")
        }
    }

    private fun initializeComponents() {
        moodManager = MoodManager(this)
    }

    private fun initializeViews() {
        try {
            toolbar = findViewById(R.id.toolbar)
            tvMoodVeryHappy = findViewById(R.id.tv_mood_very_happy)
            tvMoodHappy = findViewById(R.id.tv_mood_happy)
            tvMoodNeutral = findViewById(R.id.tv_mood_neutral)
            tvMoodSad = findViewById(R.id.tv_mood_sad)
            tvMoodVerySad = findViewById(R.id.tv_mood_very_sad)
            etMoodNote = findViewById(R.id.et_mood_note)
            btnSaveMood = findViewById(R.id.btn_save_mood)
            rvMoodHistory = findViewById(R.id.rv_mood_history)
            lineChart = findViewById(R.id.line_chart)
            tvMoodHistory = findViewById(R.id.tv_mood_history)
            tvMostCommonMood = findViewById(R.id.tv_most_common_mood)
            tvMoodTrend = findViewById(R.id.tv_mood_trend)
            bottomNavigation = findViewById(R.id.bottom_navigation)

            // Initialize new views
            tvCurrentDate = findViewById(R.id.tv_current_date)
            btnPreviousDay = findViewById(R.id.btn_previous_day)
            btnNextDay = findViewById(R.id.btn_next_day)
            btnMorning = findViewById(R.id.btn_morning)
            btnAfternoon = findViewById(R.id.btn_afternoon)
            btnEvening = findViewById(R.id.btn_evening)
            chipGroupFactors = findViewById(R.id.chip_group_factors)
            btnPeriodWeek = findViewById(R.id.btn_period_week)
            btnPeriodMonth = findViewById(R.id.btn_period_month)
            btnPeriodYear = findViewById(R.id.btn_period_year)

            // Initialize additional views
            tvAverageMood = findViewById(R.id.tv_average_mood)
            tvTopFactor = findViewById(R.id.tv_top_factor)
            tvTodayEntries = findViewById(R.id.tv_today_entries)

            // Populate emoji views list
            emojiViews.apply {
                clear()
                addAll(listOf(tvMoodVeryHappy, tvMoodHappy, tvMoodNeutral, tvMoodSad, tvMoodVerySad))
            }

            // Initially disable save button
            btnSaveMood.isEnabled = false

            // Set current date
            updateDateDisplay()

            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            throw e
        }
    }

    private fun setupDateNavigation() {
        btnPreviousDay.setOnClickListener {
            currentDate.add(Calendar.DAY_OF_MONTH, -1)
            updateDateDisplay()
            refreshUI()
        }

        btnNextDay.setOnClickListener {
            currentDate.add(Calendar.DAY_OF_MONTH, 1)
            updateDateDisplay()
            refreshUI()
        }
    }

    private fun updateDateDisplay() {
        val today = Calendar.getInstance()
        if (currentDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
            currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            tvCurrentDate.text = "Today, ${dateFormat.format(currentDate.time)}"
        } else {
            tvCurrentDate.text = dateFormat.format(currentDate.time)
        }
    }

    private fun setupTimeOfDaySelection() {
        btnMorning.setOnClickListener {
            selectTimeOfDay("Morning")
        }
        btnAfternoon.setOnClickListener {
            selectTimeOfDay("Afternoon")
        }
        btnEvening.setOnClickListener {
            selectTimeOfDay("Evening")
        }
    }

    private fun selectTimeOfDay(timeOfDay: String) {
        selectedTimeOfDay = timeOfDay

        // Reset button styles
        btnMorning.setBackgroundResource(R.drawable.time_selector_unselected)
        btnAfternoon.setBackgroundResource(R.drawable.time_selector_unselected)
        btnEvening.setBackgroundResource(R.drawable.time_selector_unselected)
        btnMorning.setTextColor(Color.parseColor("#757575"))
        btnAfternoon.setTextColor(Color.parseColor("#757575"))
        btnEvening.setTextColor(Color.parseColor("#757575"))

        // Highlight selected button
        when (timeOfDay) {
            "Morning" -> {
                btnMorning.setBackgroundResource(R.drawable.time_selector_selected)
                btnMorning.setTextColor(Color.WHITE)
            }
            "Afternoon" -> {
                btnAfternoon.setBackgroundResource(R.drawable.time_selector_selected)
                btnAfternoon.setTextColor(Color.WHITE)
            }
            "Evening" -> {
                btnEvening.setBackgroundResource(R.drawable.time_selector_selected)
                btnEvening.setTextColor(Color.WHITE)
            }
        }

        updateSaveButtonState()
    }

    private fun setupPeriodSelection() {
        btnPeriodWeek.setOnClickListener {
            selectPeriod(0)
        }
        btnPeriodMonth.setOnClickListener {
            selectPeriod(1)
        }
        btnPeriodYear.setOnClickListener {
            selectPeriod(2)
        }

        // Set month as default
        selectPeriod(1)
    }

    private fun selectPeriod(period: Int) {
        // Reset button styles
        btnPeriodWeek.setBackgroundResource(R.drawable.period_selector_unselected)
        btnPeriodMonth.setBackgroundResource(R.drawable.period_selector_unselected)
        btnPeriodYear.setBackgroundResource(R.drawable.period_selector_unselected)
        btnPeriodWeek.setTextColor(Color.parseColor("#757575"))
        btnPeriodMonth.setTextColor(Color.parseColor("#757575"))
        btnPeriodYear.setTextColor(Color.parseColor("#757575"))

        // Highlight selected button
        when (period) {
            0 -> {
                btnPeriodWeek.setBackgroundResource(R.drawable.period_selector_selected)
                btnPeriodWeek.setTextColor(Color.WHITE)
            }
            1 -> {
                btnPeriodMonth.setBackgroundResource(R.drawable.period_selector_selected)
                btnPeriodMonth.setTextColor(Color.WHITE)
            }
            2 -> {
                btnPeriodYear.setBackgroundResource(R.drawable.period_selector_selected)
                btnPeriodYear.setTextColor(Color.WHITE)
            }
        }

        updateChart(period)
    }

    private fun setupFactorsSelection() {
        // Factors are handled in saveMoodEntry by checking selected chips
    }

    private fun getSelectedFactors(): List<String> {
        val selectedFactors = mutableListOf<String>()
        for (i in 0 until chipGroupFactors.childCount) {
            val chip = chipGroupFactors.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedFactors.add(chip.text.toString())
            }
        }
        return selectedFactors
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
                title = "Mood Journal"
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
                        navigateToActivity(HydrationActivity::class.java)
                        true
                    }
                    R.id.nav_mood -> {
                        // Already in mood activity
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

            // Set mood as selected
            bottomNavigation.selectedItemId = R.id.nav_mood

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

    private fun setupMoodSelectors() {
        try {
            val moodClickListener = { mood: MoodType, view: TextView ->
                selectMood(mood, view)
            }

            tvMoodVeryHappy.setOnClickListener {
                moodClickListener(MoodType.VERY_HAPPY, tvMoodVeryHappy)
            }
            tvMoodHappy.setOnClickListener {
                moodClickListener(MoodType.HAPPY, tvMoodHappy)
            }
            tvMoodNeutral.setOnClickListener {
                moodClickListener(MoodType.NEUTRAL, tvMoodNeutral)
            }
            tvMoodSad.setOnClickListener {
                moodClickListener(MoodType.SAD, tvMoodSad)
            }
            tvMoodVerySad.setOnClickListener {
                moodClickListener(MoodType.VERY_SAD, tvMoodVerySad)
            }

            Log.d(TAG, "Mood selectors setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up mood selectors", e)
        }
    }

    private fun setupClickListeners() {
        try {
            btnSaveMood.setOnClickListener {
                if (!isDataLoading) {
                    saveMoodEntry()
                }
            }

            etMoodNote.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = updateSaveButtonState()
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            Log.d(TAG, "Click listeners setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            adapter = MoodHistoryAdapter()

            rvMoodHistory.apply {
                layoutManager = LinearLayoutManager(this@MoodActivity)
                adapter = this@MoodActivity.adapter
                // Optimize RecyclerView performance
                setHasFixedSize(true)
                setItemViewCacheSize(20)
                isNestedScrollingEnabled = false

                // Add divider
                addItemDecoration(
                    androidx.recyclerview.widget.DividerItemDecoration(
                        this@MoodActivity,
                        androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                    )
                )
            }

            Log.d(TAG, "RecyclerView setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun setupChart() {
        try {
            lineChart.apply {
                description = Description().apply { text = "Mood Tracking - 6 Hour Intervals" }
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setBackgroundColor(Color.WHITE)
                setGridBackgroundColor(Color.WHITE)

                // Configure axes
                axisRight.isEnabled = false
                axisLeft.apply {
                    axisMinimum = 1f
                    axisMaximum = 5f
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                    textColor = Color.DKGRAY
                    labelCount = 5
                }

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textColor = Color.DKGRAY
                    labelRotationAngle = -45f // Rotate labels for better readability
                    setLabelCount(8, false) // Show max 8 labels to avoid crowding
                }

                // Configure legend
                legend.apply {
                    isEnabled = true
                    textColor = Color.DKGRAY
                    textSize = 12f
                }

                // Set no data text
                setNoDataText("Start tracking your mood throughout the day!")
                setNoDataTextColor(Color.GRAY)
            }

            Log.d(TAG, "Chart setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up chart", e)
        }
    }

    private fun loadDataAsync() {
        activityScope.launch {
            try {
                isDataLoading = true

                withContext(Dispatchers.IO) {
                    // Simulate loading time for better UX
                    delay(100)
                }

                refreshUI()
                isDataLoading = false

            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
                isDataLoading = false
                showToast("Error loading mood data")
            }
        }
    }

    private fun selectMood(type: MoodType, view: TextView) {
        try {
            Log.d(TAG, "Selecting mood: ${type.label}")

            // Clear previous selections
            emojiViews.forEach { it.setBackgroundColor(Color.TRANSPARENT) }

            // Highlight selected mood
            view.setBackgroundColor(Color.parseColor("#E3F2FD"))
            selectedMood = type
            updateSaveButtonState()

            // Provide haptic feedback
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
            showToast("${type.emoji} ${type.label} mood selected")

            Log.d(TAG, "Mood selected: ${type.label}")
        } catch (e: Exception) {
            Log.e(TAG, "Error selecting mood", e)
        }
    }

    private fun updateSaveButtonState() {
        try {
            val shouldEnable = selectedMood != null && !isDataLoading
            btnSaveMood.isEnabled = shouldEnable

            // Update button text based on state
            btnSaveMood.text = when {
                isDataLoading -> "Saving..."
                selectedMood != null -> "Save Mood Entry"
                else -> "Select a mood first"
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error updating save button state", e)
        }
    }

    private fun saveMoodEntry() {
        val mood = selectedMood
        if (mood == null) {
            showToast("Please select a mood first")
            return
        }

        activityScope.launch {
            try {
                isDataLoading = true
                updateSaveButtonState()

                // Get current date in yyyy-MM-dd format
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate.time)
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                // Get selected factors
                val factors = getSelectedFactors()

                // Convert factors to comma-separated string for storage
                val factorsStr = if (factors.isNotEmpty()) factors.joinToString(",") else ""

                val entry = MoodEntry(
                    date = dateStr,
                    time = timeStr,
                    moodEmoji = mood.emoji,
                    moodLevel = mood.level,
                    notes = etMoodNote.text.toString().trim()
                )

                Log.d(TAG, "Saving mood entry: ${entry}")

                // Save in background
                withContext(Dispatchers.IO) {
                    moodManager.saveMoodEntry(entry)
                    Log.d(TAG, "Mood entry saved to storage: ${entry}")
                }

                // Ensure UI updates happen on main thread
                withContext(Dispatchers.Main) {
                    // Clear form first
                    clearMoodSelection()

                    // Add small delay to ensure data is persisted
                    delay(100)

                    // Refresh UI with new data
                    refreshUI()

                    showToast("Mood saved! ${mood.emoji}")
                    Log.d(TAG, "Mood entry saved successfully: ${mood.label}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error saving mood entry", e)
                showToast("Error saving mood: ${e.message}")
            } finally {
                isDataLoading = false
                updateSaveButtonState()
            }
        }
    }

    private fun clearMoodSelection() {
        selectedMood = null
        selectedTimeOfDay = null
        emojiViews.forEach { it.setBackgroundColor(Color.TRANSPARENT) }
        etMoodNote.text.clear()

        // Reset time of day buttons
        btnMorning.setBackgroundResource(R.drawable.time_selector_unselected)
        btnAfternoon.setBackgroundResource(R.drawable.time_selector_unselected)
        btnEvening.setBackgroundResource(R.drawable.time_selector_unselected)
        btnMorning.setTextColor(Color.parseColor("#757575"))
        btnAfternoon.setTextColor(Color.parseColor("#757575"))
        btnEvening.setTextColor(Color.parseColor("#757575"))

        // Clear factor chips
        chipGroupFactors.clearCheck()
    }

    private fun refreshUI() {
        try {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate.time)
            val entries = moodManager.getMoodEntriesForDate(dateStr)
            Log.d(TAG, "Refreshing UI with ${entries.size} entries for date $dateStr")

            // Debug logging to verify data retrieval
            entries.take(3).forEachIndexed { index, entry ->
                Log.d(TAG, "Entry $index: ${entry.moodEmoji} - ${entry.date} ${entry.time} - '${entry.notes}'")
            }

            // Update RecyclerView with explicit data refresh
            Log.d(TAG, "Updating adapter with ${entries.size} entries")
            adapter.updateEntries(entries)

            // Show/hide history section
            tvMoodHistory.isVisible = entries.isNotEmpty()
            rvMoodHistory.isVisible = entries.isNotEmpty()

            // Update insights and chart with time-based data
            updateInsights(entries)
            updateChart() // Now uses detailed time-based data
            updateTodayEntriesDisplay() // New method for today's entries

            Log.d(TAG, "UI refreshed successfully - RecyclerView has ${adapter.itemCount} items")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing UI", e)
        }
    }

    private fun updateInsights(entries: List<MoodEntry>) {
        try {
            if (entries.isEmpty()) {
                tvAverageMood.text = "No data yet"
                tvMostCommonMood.text = "No data yet"
                tvMoodTrend.text = "Start tracking!"
                tvTopFactor.text = "No data yet"
                return
            }

            // Average mood
            val avgMoodLevel = entries.map { it.moodLevel }.average()
            val avgMoodType = MoodType.values().find { it.level == avgMoodLevel.toInt() }
            tvAverageMood.text = "${avgMoodType?.label ?: "Unknown"} ${avgMoodType?.emoji ?: ""}"

            // Most common mood
            val moodCounts = entries.groupingBy { it.moodEmoji }.eachCount()
            val mostCommon = moodCounts.maxByOrNull { it.value }
            tvMostCommonMood.text = "${mostCommon?.key} (${mostCommon?.value} times)"

            // Enhanced trend calculation with time awareness
            val weeklyEntries = moodManager.getWeeklyMoodEntries()
            updateTrendDisplay(weeklyEntries)

            // Top factor
            val allFactors = entries.flatMap { entry ->
                entry.notes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
            val factorCounts = allFactors.groupingBy { it }.eachCount()
            val topFactor = factorCounts.maxByOrNull { it.value }
            tvTopFactor.text = topFactor?.key ?: "No factors"

            // Log today's interval statistics for debugging
            try {
                val todayStats = moodManager.getTodayIntervalStats()
                Log.d(TAG, "Today's Mood Intervals: $todayStats")
            } catch (e: Exception) {
                Log.d(TAG, "Today interval stats not available yet")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error updating insights", e)
        }
    }

    private fun updateTrendDisplay(weeklyEntries: List<MoodEntry>) {
        try {
            if (weeklyEntries.size < INSIGHTS_MIN_ENTRIES) {
                tvMoodTrend.text = "Need more data"
                tvMoodTrend.setTextColor(Color.GRAY)
                return
            }

            val recent = weeklyEntries.take(3).map { it.moodLevel }.average()
            val older = weeklyEntries.drop(weeklyEntries.size / 2).map { it.moodLevel }.average()

            when {
                recent > older + TREND_THRESHOLD -> {
                    tvMoodTrend.text = "Improving ↗"
                    tvMoodTrend.setTextColor(Color.parseColor("#4CAF50"))
                }
                recent < older - TREND_THRESHOLD -> {
                    tvMoodTrend.text = "Declining ↘"
                    tvMoodTrend.setTextColor(Color.parseColor("#F44336"))
                }
                else -> {
                    tvMoodTrend.text = "Stable →"
                    tvMoodTrend.setTextColor(Color.parseColor("#FF9800"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating trend display", e)
        }
    }

    private fun updateTodayEntriesDisplay() {
        try {
            // Get today's entries
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todayEntries = moodManager.getMoodEntriesForDate(today)

            // Count entries by time of day
            val morningCount = todayEntries.count {
                val hour = it.time.split(":")[0].toInt()
                hour in 6..11
            }
            val afternoonCount = todayEntries.count {
                val hour = it.time.split(":")[0].toInt()
                hour in 12..17
            }
            val eveningCount = todayEntries.count {
                val hour = it.time.split(":")[0].toInt()
                hour in 18..23 || hour in 0..5
            }

            // Update the today entries text view
            val totalEntries = todayEntries.size
            val timeOfDayEntries = when {
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY) in 6..11 -> morningCount
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY) in 12..17 -> afternoonCount
                else -> eveningCount
            }

            tvTodayEntries.text = "$totalEntries entries today"

            Log.d(TAG, "Today's entries: $totalEntries (Morning: $morningCount, Afternoon: $afternoonCount, Evening: $eveningCount)")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating today entries display", e)
        }
    }

    private fun updateChart(period: Int = 1) {
        try {
            // Use detailed trend data instead of daily averages
            val trendData = moodManager.getDetailedMoodTrendData(MOOD_HISTORY_DAYS)
            Log.d(TAG, "Updating chart with ${trendData.size} detailed data points")

            // Filter out empty data points for cleaner chart
            val filteredData = trendData.filter { it.second > 0f }

            if (filteredData.isEmpty()) {
                lineChart.clear()
                lineChart.setNoDataText("Start tracking your mood throughout the day!")
                lineChart.invalidate()
                return
            }

            val entries = filteredData.mapIndexed { index, (_, avgMood) ->
                Entry(index.toFloat(), avgMood)
            }

            val dataSet = LineDataSet(entries, "Mood Throughout Time").apply {
                color = Color.parseColor("#1976D2")
                setCircleColor(Color.parseColor("#1976D2"))
                circleRadius = 4f
                lineWidth = 2f
                valueTextSize = 9f
                valueTextColor = Color.DKGRAY
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillColor = Color.parseColor("#E3F2FD")
                fillAlpha = 80
                setDrawValues(true)

                // Add different colors based on mood level
                setCircleColors(entries.map { entry ->
                    when {
                        entry.y >= 4.5f -> Color.parseColor("#4CAF50") // Great mood - Green
                        entry.y >= 3.5f -> Color.parseColor("#8BC34A") // Good mood - Light Green
                        entry.y >= 2.5f -> Color.parseColor("#FF9800") // Okay mood - Orange
                        entry.y >= 1.5f -> Color.parseColor("#FF5722") // Sad mood - Red Orange
                        else -> Color.parseColor("#F44336")            // Awful mood - Red
                    }
                })
            }

            val lineData = LineData(dataSet)
            lineChart.data = lineData

            // Format X-axis labels with time intervals
            val timeLabels = filteredData.map { (timeLabel, _) -> timeLabel }
            lineChart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(timeLabels)
                labelRotationAngle = -45f // Rotate labels for better readability
                setLabelCount(min(timeLabels.size, 8), false) // Show max 8 labels to avoid crowding
            }

            // Update chart description
            lineChart.description.text = "Mood Tracking - 6 Hour Intervals"

            // Animate chart
            lineChart.animateX(CHART_ANIMATION_DURATION)
            lineChart.invalidate()

            Log.d(TAG, "Detailed chart updated successfully with ${filteredData.size} intervals")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating detailed chart", e)
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

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
        Log.d(TAG, "MoodActivity destroyed")
    }

    // Helper function to get time of day from hour
    private fun getTimeOfDayFromHour(hour: Int): String {
        return when {
            hour in 6..11 -> "Morning"
            hour in 12..17 -> "Afternoon"
            hour in 18..23 || hour in 0..5 -> "Evening"
            else -> "Unknown"
        }
    }

    // Helper function to convert date and time to milliseconds for proper sorting
    private fun getDateTimeMillis(entry: MoodEntry): Long {
        return try {
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateTimeString = "${entry.date} ${entry.time}"
            dateTimeFormat.parse(dateTimeString)?.time ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing datetime: ${entry.date} ${entry.time}", e)
            0L
        }
    }

    // FIXED: RecyclerView Adapter with PROPER CHRONOLOGICAL sorting
    private inner class MoodHistoryAdapter : RecyclerView.Adapter<MoodHistoryAdapter.ViewHolder>() {

        private var entries = listOf<MoodEntry>()

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMoodEmoji: TextView = view.findViewById(R.id.tv_mood_emoji)
            val tvMoodLabel: TextView = view.findViewById(R.id.tv_mood_label)
            val tvMoodTime: TextView = view.findViewById(R.id.tv_mood_time)
            val tvMoodNote: TextView = view.findViewById(R.id.tv_mood_note)
            val tvTimeBadge: TextView = view.findViewById(R.id.tv_time_badge)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mood_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                val entry = entries[position]

                holder.tvMoodEmoji.text = entry.moodEmoji

                // Get mood type for color
                val moodType = MoodType.values().find { it.level == entry.moodLevel }
                val labelColor = Color.parseColor(moodType?.color ?: "#546E7A")

                holder.tvMoodLabel.apply {
                    text = when (entry.moodLevel) {
                        5 -> "Great mood"
                        4 -> "Good mood"
                        3 -> "Okay mood"
                        2 -> "Sad mood"
                        1 -> "Awful mood"
                        else -> "Unknown mood"
                    }
                    setTextColor(labelColor)
                }

                // Display formatted date and time
                holder.tvMoodTime.text = "${DateUtils.formatDateForDisplay(entry.date)} at ${entry.time}"

                // Set time badge
                val hour = try { entry.time.split(":")[0].toInt() } catch (e: Exception) { 12 }
                val timeOfDay = getTimeOfDayFromHour(hour)
                holder.tvTimeBadge.text = timeOfDay

                // Handle notes visibility
                if (entry.notes.isBlank()) {
                    holder.tvMoodNote.visibility = View.GONE
                } else {
                    holder.tvMoodNote.visibility = View.VISIBLE
                    holder.tvMoodNote.text = "\"${entry.notes}\""
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error binding view holder at position $position", e)
            }
        }

        override fun getItemCount() = entries.size

        // CRITICAL FIX: Proper sorting by datetime
        fun updateEntries(newEntries: List<MoodEntry>) {
            val oldSize = entries.size

            // Sort entries properly:
            // 1. For different dates: newer dates first
            // 2. For same date: chronological time order (morning -> afternoon -> evening)
            entries = newEntries.sortedWith { entry1, entry2 ->
                try {
                    // First compare dates (newer dates first)
                    val dateComparison = entry2.date.compareTo(entry1.date)
                    if (dateComparison != 0) {
                        return@sortedWith dateComparison
                    }

                    // For same date, sort chronologically by time
                    val dateTimeMillis1 = getDateTimeMillis(entry1)
                    val dateTimeMillis2 = getDateTimeMillis(entry2)

                    // Within same date: earlier times first (chronological order)
                    dateTimeMillis1.compareTo(dateTimeMillis2)
                } catch (e: Exception) {
                    Log.e(TAG, "Error sorting entries", e)
                    entry1.timestamp.compareTo(entry2.timestamp)
                }
            }

            // Use reliable notification
            notifyDataSetChanged()

            Log.d(TAG, "Adapter updated: ${entries.size} entries (was $oldSize)")

            // Debug: Log sorted entries to verify correct order
            entries.forEachIndexed { index, entry ->
                val millis = getDateTimeMillis(entry)
                val hour = try { entry.time.split(":")[0].toInt() } catch (e: Exception) { -1 }
                val timeOfDay = getTimeOfDayFromHour(hour)
                Log.d(TAG, "Final Entry $index: ${entry.moodEmoji} - ${entry.date} ${entry.time} ($timeOfDay) [${millis}ms]")
            }
        }
    }
}
