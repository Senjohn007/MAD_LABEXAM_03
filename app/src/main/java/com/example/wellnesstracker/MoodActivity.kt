package com.example.wellnesstracker

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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

class MoodActivity : AppCompatActivity() {

    private val TAG = "MoodActivity"

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
    private lateinit var btnShowTrend: Button
    private lateinit var rvMoodHistory: RecyclerView
    private lateinit var lineChart: LineChart
    private lateinit var tvMoodHistory: TextView
    private lateinit var tvMostCommonMood: TextView
    private lateinit var tvMoodTrend: TextView

    // Data
    private var selectedMood: MoodType? = null
    private lateinit var adapter: MoodHistoryAdapter
    private val emojiViews = mutableListOf<TextView>()

    enum class MoodType(val emoji: String, val label: String, val level: Int) {
        VERY_HAPPY("😄", "Great", 5),
        HAPPY("😊", "Good", 4),
        NEUTRAL("😐", "Okay", 3),
        SAD("😔", "Sad", 2),
        VERY_SAD("😢", "Awful", 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mood_page)

        try {
            Log.d(TAG, "MoodActivity onCreate started")

            // Initialize backend
            moodManager = MoodManager(this)

            initializeViews()
            setupToolbar()
            setupMoodSelectors()
            setupClickListeners()
            setupRecyclerView()
            setupChart()
            refreshUI()

            Log.d(TAG, "MoodActivity onCreate completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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

            // Add emoji views to list for easy manipulation
            emojiViews.clear()
            emojiViews.addAll(listOf(
                tvMoodVeryHappy, tvMoodHappy, tvMoodNeutral,
                tvMoodSad, tvMoodVerySad
            ))

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
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener {
                Log.d(TAG, "Back button clicked")
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    private fun setupMoodSelectors() {
        try {
            val moods = MoodType.values()

            // Set up each emoji with its corresponding mood
            tvMoodVeryHappy.setOnClickListener {
                Log.d(TAG, "Very Happy clicked")
                selectMood(MoodType.VERY_HAPPY, tvMoodVeryHappy)
            }
            tvMoodHappy.setOnClickListener {
                Log.d(TAG, "Happy clicked")
                selectMood(MoodType.HAPPY, tvMoodHappy)
            }
            tvMoodNeutral.setOnClickListener {
                Log.d(TAG, "Neutral clicked")
                selectMood(MoodType.NEUTRAL, tvMoodNeutral)
            }
            tvMoodSad.setOnClickListener {
                Log.d(TAG, "Sad clicked")
                selectMood(MoodType.SAD, tvMoodSad)
            }
            tvMoodVerySad.setOnClickListener {
                Log.d(TAG, "Very Sad clicked")
                selectMood(MoodType.VERY_SAD, tvMoodVerySad)
            }

            Log.d(TAG, "Mood selectors set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up mood selectors", e)
        }
    }

    private fun setupClickListeners() {
        try {
            btnSaveMood.setOnClickListener {
                Log.d(TAG, "Save button clicked")
                saveMoodEntry()
            }

            btnShowTrend.setOnClickListener {
                Log.d(TAG, "Show trend button clicked")
                updateChart()
            }

            etMoodNote.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = updateSaveButtonState()
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            Log.d(TAG, "Click listeners set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            adapter = MoodHistoryAdapter()
            rvMoodHistory.layoutManager = LinearLayoutManager(this)
            rvMoodHistory.adapter = adapter
            Log.d(TAG, "RecyclerView set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun setupChart() {
        try {
            lineChart.apply {
                description = Description().apply { text = "" }
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setBackgroundColor(Color.WHITE)

                // Configure axes
                axisRight.isEnabled = false
                axisLeft.apply {
                    axisMinimum = 1f
                    axisMaximum = 5f
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                }
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                }

                // Configure legend
                legend.isEnabled = true
                legend.textColor = Color.DKGRAY
            }
            Log.d(TAG, "Chart set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up chart", e)
        }
    }

    private fun selectMood(type: MoodType, view: TextView) {
        try {
            Log.d(TAG, "Selecting mood: ${type.label}")

            // Clear previous selections (simple background color change)
            emojiViews.forEach {
                it.setBackgroundColor(Color.TRANSPARENT)
            }

            // Set new selection with a colored background
            view.setBackgroundColor(Color.parseColor("#E3F2FD"))
            selectedMood = type
            updateSaveButtonState()

            Toast.makeText(this, "${type.label} mood selected", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Mood selected successfully: ${type.label}")
        } catch (e: Exception) {
            Log.e(TAG, "Error selecting mood", e)
        }
    }

    private fun updateSaveButtonState() {
        try {
            val shouldEnable = selectedMood != null
            btnSaveMood.isEnabled = shouldEnable
            Log.d(TAG, "Save button enabled: $shouldEnable")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating save button state", e)
        }
    }

    private fun saveMoodEntry() {
        try {
            val mood = selectedMood
            if (mood == null) {
                Toast.makeText(this, "Please select a mood first", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d(TAG, "Saving mood entry: ${mood.label}")

            val entry = MoodEntry(
                date = DateUtils.getCurrentDate(),
                time = DateUtils.getCurrentTime(),
                moodEmoji = mood.emoji,
                moodLevel = mood.level,
                notes = etMoodNote.text.toString().trim()
            )

            moodManager.saveMoodEntry(entry)

            // Clear form
            selectedMood = null
            emojiViews.forEach { it.setBackgroundColor(Color.TRANSPARENT) }
            etMoodNote.text.clear()
            updateSaveButtonState()

            // Refresh UI
            refreshUI()

            Toast.makeText(this, "Mood saved! ${mood.emoji}", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Mood entry saved successfully: ${mood.label}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving mood entry", e)
            Toast.makeText(this, "Error saving mood: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshUI() {
        try {
            val entries = moodManager.getAllMoodEntries()
            Log.d(TAG, "Refreshing UI with ${entries.size} entries")

            adapter.updateEntries(entries)
            tvMoodHistory.isVisible = entries.isNotEmpty()

            updateInsights(entries)
            updateChart()

            Log.d(TAG, "UI refreshed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing UI", e)
        }
    }

    private fun updateInsights(entries: List<MoodEntry>) {
        try {
            if (entries.isEmpty()) {
                tvMostCommonMood.text = "No data yet"
                tvMoodTrend.text = "Start tracking!"
                return
            }

            // Most common mood
            val moodCounts = entries.groupingBy { it.moodEmoji }.eachCount()
            val mostCommon = moodCounts.maxByOrNull { it.value }
            tvMostCommonMood.text = "${mostCommon?.key} (${mostCommon?.value} times)"

            // Simple trend calculation
            val weeklyEntries = moodManager.getWeeklyMoodEntries()
            if (weeklyEntries.size >= 3) {
                val recent = weeklyEntries.take(3).map { it.moodLevel }.average()
                val older = weeklyEntries.drop(weeklyEntries.size / 2).map { it.moodLevel }.average()

                when {
                    recent > older + 0.5 -> {
                        tvMoodTrend.text = "Improving ↗"
                        tvMoodTrend.setTextColor(Color.parseColor("#4CAF50"))
                    }
                    recent < older - 0.5 -> {
                        tvMoodTrend.text = "Declining ↘"
                        tvMoodTrend.setTextColor(Color.parseColor("#F44336"))
                    }
                    else -> {
                        tvMoodTrend.text = "Stable →"
                        tvMoodTrend.setTextColor(Color.parseColor("#FF9800"))
                    }
                }
            } else {
                tvMoodTrend.text = "Need more data"
                tvMoodTrend.setTextColor(Color.GRAY)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating insights", e)
        }
    }

    private fun updateChart() {
        try {
            val trendData = moodManager.getMoodTrendData(7)
            Log.d(TAG, "Updating chart with ${trendData.size} data points")

            if (trendData.all { it.second == 0f }) {
                lineChart.clear()
                lineChart.invalidate()
                return
            }

            val entries = trendData.mapIndexed { index, (_, avgMood) ->
                Entry(index.toFloat(), avgMood)
            }

            val dataSet = LineDataSet(entries, "Daily Average Mood").apply {
                color = Color.parseColor("#1976D2")
                setCircleColor(Color.parseColor("#1976D2"))
                circleRadius = 6f
                lineWidth = 3f
                valueTextSize = 12f
                valueTextColor = Color.DKGRAY
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillColor = Color.parseColor("#E3F2FD")
                fillAlpha = 100
            }

            val lineData = LineData(dataSet)
            lineChart.data = lineData

            // Format X-axis labels (dates)
            val dateLabels = trendData.map { (date, _) ->
                if (date.length >= 10) date.substring(5) else date // Show MM-dd
            }
            lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)

            lineChart.animateX(1000)
            lineChart.invalidate()

            Log.d(TAG, "Chart updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating chart", e)
        }
    }

    // RecyclerView Adapter
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
                holder.tvMoodLabel.text = when (entry.moodLevel) {
                    5 -> "Great mood"
                    4 -> "Good mood"
                    3 -> "Okay mood"
                    2 -> "Sad mood"
                    1 -> "Awful mood"
                    else -> "Unknown mood"
                }
                holder.tvMoodTime.text = "${DateUtils.formatDateForDisplay(entry.date)} at ${entry.time}"

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

        fun updateEntries(newEntries: List<MoodEntry>) {
            entries = newEntries.sortedByDescending { it.timestamp }
            notifyDataSetChanged()
            Log.d(TAG, "Adapter updated with ${entries.size} entries")
        }
    }
}
