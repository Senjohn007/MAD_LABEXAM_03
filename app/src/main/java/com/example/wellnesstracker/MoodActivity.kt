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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MoodActivity : AppCompatActivity() {

    private val TAG = "MoodActivity"

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
    private lateinit var btnListView: Button
    private lateinit var btnCalendarView: Button

    // Data variables
    private var selectedMood: MoodType? = null
    private val moodHistory = mutableListOf<MoodEntry>()
    private lateinit var moodHistoryAdapter: MoodHistoryAdapter
    private var isListView = true

    // Mood types enum
    enum class MoodType(val emoji: String, val label: String, val score: Int) {
        VERY_HAPPY("😄", "Great", 5),
        HAPPY("😊", "Good", 4),
        NEUTRAL("😐", "Okay", 3),
        SAD("😔", "Sad", 2),
        VERY_SAD("😢", "Awful", 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "MoodActivity onCreate started")

            // Set the content view to use mood_activity.xml
            setContentView(R.layout.mood_page)

            initializeViews()
            setupToolbar()
            setupMoodSelectors()
            setupClickListeners()
            setupRecyclerView()
            loadSampleData()
            updateUI()

            Log.d(TAG, "MoodActivity onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in MoodActivity onCreate", e)
            showToast("Error initializing mood journal: ${e.message}")
        }
    }

    private fun initializeViews() {
        try {
            // Initialize all views
            toolbar = findViewById(R.id.toolbar)
            tvMoodVeryHappy = findViewById(R.id.tv_mood_very_happy)
            tvMoodHappy = findViewById(R.id.tv_mood_happy)
            tvMoodNeutral = findViewById(R.id.tv_mood_neutral)
            tvMoodSad = findViewById(R.id.tv_mood_sad)
            tvMoodVerySad = findViewById(R.id.tv_mood_very_sad)
            etMoodNote = findViewById(R.id.et_mood_note)
            btnSaveMood = findViewById(R.id.btn_save_mood)
            rvMoodHistory = findViewById(R.id.rv_mood_history)
            btnListView = findViewById(R.id.btn_list_view)
            btnCalendarView = findViewById(R.id.btn_calendar_view)

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

    private fun setupMoodSelectors() {
        try {
            // Set up mood emoji click listeners
            tvMoodVeryHappy.setOnClickListener { selectMood(MoodType.VERY_HAPPY, tvMoodVeryHappy) }
            tvMoodHappy.setOnClickListener { selectMood(MoodType.HAPPY, tvMoodHappy) }
            tvMoodNeutral.setOnClickListener { selectMood(MoodType.NEUTRAL, tvMoodNeutral) }
            tvMoodSad.setOnClickListener { selectMood(MoodType.SAD, tvMoodSad) }
            tvMoodVerySad.setOnClickListener { selectMood(MoodType.VERY_SAD, tvMoodVerySad) }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up mood selectors", e)
        }
    }

    private fun setupClickListeners() {
        try {
            // Save mood button
            btnSaveMood.setOnClickListener {
                saveMoodEntry()
            }

            // View toggle buttons
            btnListView.setOnClickListener {
                toggleView(true)
            }

            btnCalendarView.setOnClickListener {
                toggleView(false)
            }

            // Note input listener
            etMoodNote.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    updateSaveButtonState()
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            moodHistoryAdapter = MoodHistoryAdapter(moodHistory)
            rvMoodHistory.layoutManager = LinearLayoutManager(this)
            rvMoodHistory.adapter = moodHistoryAdapter

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun selectMood(moodType: MoodType, selectedView: TextView) {
        try {
            // Clear previous selection
            clearMoodSelection()

            // Set new selection
            selectedMood = moodType
            selectedView.setBackgroundResource(R.drawable.mood_selected_background)

            // Update save button state
            updateSaveButtonState()

            Log.d(TAG, "Selected mood: ${moodType.label}")

        } catch (e: Exception) {
            Log.e(TAG, "Error selecting mood", e)
        }
    }

    private fun clearMoodSelection() {
        try {
            // Clear all mood selections
            tvMoodVeryHappy.background = null
            tvMoodHappy.background = null
            tvMoodNeutral.background = null
            tvMoodSad.background = null
            tvMoodVerySad.background = null

        } catch (e: Exception) {
            Log.e(TAG, "Error clearing mood selection", e)
        }
    }

    private fun updateSaveButtonState() {
        try {
            // Enable save button only if mood is selected
            btnSaveMood.isEnabled = selectedMood != null

        } catch (e: Exception) {
            Log.e(TAG, "Error updating save button state", e)
        }
    }

    private fun saveMoodEntry() {
        try {
            if (selectedMood == null) {
                showToast("Please select a mood first")
                return
            }

            val note = etMoodNote.text.toString().trim()
            val currentTime = System.currentTimeMillis()

            val moodEntry = MoodEntry(
                mood = selectedMood!!,
                note = note,
                timestamp = currentTime
            )

            // Add to history (at the beginning for recent entries first)
            moodHistory.add(0, moodEntry)

            // Update UI
            updateUI()

            // Clear form
            clearForm()

            showToast("Mood saved successfully!")
            Log.d(TAG, "Saved mood entry: ${selectedMood!!.label}")

        } catch (e: Exception) {
            Log.e(TAG, "Error saving mood entry", e)
            showToast("Error saving mood entry")
        }
    }

    private fun clearForm() {
        try {
            selectedMood = null
            clearMoodSelection()
            etMoodNote.text.clear()
            updateSaveButtonState()

        } catch (e: Exception) {
            Log.e(TAG, "Error clearing form", e)
        }
    }

    private fun toggleView(isListViewSelected: Boolean) {
        try {
            isListView = isListViewSelected

            if (isListView) {
                // List view selected
                btnListView.setBackgroundResource(R.drawable.button_toggle_selected)
                btnCalendarView.setBackgroundResource(R.drawable.button_toggle_unselected)
                showToast("List view selected")
            } else {
                // Calendar view selected
                btnListView.setBackgroundResource(R.drawable.button_toggle_unselected)
                btnCalendarView.setBackgroundResource(R.drawable.button_toggle_selected)
                showToast("Calendar view - Coming Soon!")
            }

            Log.d(TAG, "Toggled view to: ${if (isListView) "List" else "Calendar"}")

        } catch (e: Exception) {
            Log.e(TAG, "Error toggling view", e)
        }
    }

    private fun loadSampleData() {
        try {
            // Add some sample mood entries for demonstration
            val calendar = Calendar.getInstance()

            // Today
            moodHistory.add(MoodEntry(MoodType.HAPPY, "Had a great morning!", calendar.timeInMillis))

            // Yesterday
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            moodHistory.add(MoodEntry(MoodType.NEUTRAL, "Work was okay", calendar.timeInMillis))

            // 2 days ago
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            moodHistory.add(MoodEntry(MoodType.VERY_HAPPY, "Weekend was amazing!", calendar.timeInMillis))

        } catch (e: Exception) {
            Log.e(TAG, "Error loading sample data", e)
        }
    }

    private fun updateUI() {
        try {
            // Update RecyclerView
            moodHistoryAdapter.notifyDataSetChanged()

            Log.d(TAG, "UI updated - Mood history size: ${moodHistory.size}")

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

    // Data class for mood entries
    data class MoodEntry(
        val mood: MoodType,
        val note: String,
        val timestamp: Long
    ) {
        fun getFormattedDate(): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    // RecyclerView Adapter for mood history
    private class MoodHistoryAdapter(private val moodList: List<MoodEntry>) :
        RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {

        class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvMoodEmoji: TextView = itemView.findViewById(R.id.tv_mood_emoji)
            val tvMoodLabel: TextView = itemView.findViewById(R.id.tv_mood_label)
            val tvMoodNote: TextView = itemView.findViewById(R.id.tv_mood_note)
            val tvMoodTime: TextView = itemView.findViewById(R.id.tv_mood_time)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mood_history, parent, false)
            return MoodViewHolder(view)
        }

        override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
            val moodEntry = moodList[position]

            holder.tvMoodEmoji.text = moodEntry.mood.emoji
            holder.tvMoodLabel.text = "${moodEntry.mood.label} mood"
            holder.tvMoodTime.text = moodEntry.getFormattedDate()

            // Show note if it exists
            if (moodEntry.note.isNotEmpty()) {
                holder.tvMoodNote.text = moodEntry.note
                holder.tvMoodNote.visibility = View.VISIBLE
            } else {
                holder.tvMoodNote.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int = moodList.size
    }
}
