package com.example.wellnesstracker

import android.content.Intent
import android.graphics.Color
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
import kotlinx.coroutines.*
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

    // Data and state
    private var selectedMood: MoodType? = null
    private lateinit var adapter: MoodHistoryAdapter
    private val emojiViews = mutableListOf<TextView>()
    private var isDataLoading = false

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

            // Populate emoji views list
            emojiViews.apply {
                clear()
                addAll(listOf(tvMoodVeryHappy, tvMoodHappy, tvMoodNeutral, tvMoodSad, tvMoodVerySad))
            }

            // Initially disable save button
            btnSaveMood.isEnabled = false

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

    // FIXED: Updated saveMoodEntry method with proper threading and UI updates
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

                val entry = MoodEntry(
                    date = DateUtils.getCurrentDate(),
                    time = DateUtils.getCurrentTime(),
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
        emojiViews.forEach { it.setBackgroundColor(Color.TRANSPARENT) }
        etMoodNote.text.clear()
    }

    // UPDATED: Enhanced refreshUI with time-based interval display
    private fun refreshUI() {
        try {
            val entries = moodManager.getAllMoodEntries()
            Log.d(TAG, "Refreshing UI with ${entries.size} entries")

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
            updateTodayIntervalDisplay() // New method for interval insights

            Log.d(TAG, "UI refreshed successfully - RecyclerView has ${adapter.itemCount} items")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing UI", e)
        }
    }

    // UPDATED: Enhanced insights with interval-based information
    private fun updateInsights(entries: List<MoodEntry>) {
        try {
            if (entries.isEmpty()) {
                tvMostCommonMood.text = "No data yet"
                tvMoodTrend.text = "Start tracking!"
                return
            }

            // Most common mood (existing logic)
            val moodCounts = entries.groupingBy { it.moodEmoji }.eachCount()
            val mostCommon = moodCounts.maxByOrNull { it.value }
            tvMostCommonMood.text = "${mostCommon?.key} (${mostCommon?.value} times)"

            // Enhanced trend calculation with time awareness
            val weeklyEntries = moodManager.getWeeklyMoodEntries()
            updateTrendDisplay(weeklyEntries)

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

    // NEW: Display today's interval information
    private fun updateTodayIntervalDisplay() {
        try {
            val intervalStats = moodManager.getTodayIntervalStats()
            Log.d(TAG, intervalStats)

            // Log interval breakdown for debugging
            val intervalData = moodManager.getTodayMoodsByInterval()
            intervalData.forEach { (interval, entries) ->
                if (entries.isNotEmpty()) {
                    val avgMood = entries.map { it.moodLevel }.average()
                    Log.d(TAG, "$interval: ${entries.size} entries, avg mood: ${"%.1f".format(avgMood)}")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error updating today interval display", e)
        }
    }

    // UPDATED: Enhanced chart with time-based intervals
    private fun updateChart() {
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

    // FIXED: Optimized RecyclerView Adapter with corrected notification logic
    private inner class MoodHistoryAdapter : RecyclerView.Adapter<MoodHistoryAdapter.ViewHolder>() {

        private var entries = listOf<MoodEntry>()

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMoodEmoji: TextView = view.findViewById(R.id.tv_mood_emoji)
            val tvMoodLabel: TextView = view.findViewById(R.id.tv_mood_label)
            val tvMoodTime: TextView = view.findViewById(R.id.tv_mood_time)
            val tvMoodNote: TextView = view.findViewById(R.id.tv_mood_note)
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

                holder.tvMoodTime.text = "${DateUtils.formatDateForDisplay(entry.date)} at ${entry.time}"

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

        // FIXED: Simplified and reliable updateEntries method
        fun updateEntries(newEntries: List<MoodEntry>) {
            val oldSize = entries.size
            entries = newEntries.sortedByDescending { it.timestamp }

            // Use reliable notification - this is the key fix
            notifyDataSetChanged()

            Log.d(TAG, "Adapter updated: ${entries.size} entries (was $oldSize)")

            // Debug: Log first few entries to verify data
            entries.take(3).forEachIndexed { index, entry ->
                Log.d(TAG, "Adapter Entry $index: ${entry.moodEmoji} - ${entry.date} ${entry.time}")
            }
        }
    }
}
