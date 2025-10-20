package com.example.wellnesstracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstracker.R
import com.example.wellnesstracker.models.Habit
import com.example.wellnesstracker.utils.SharedPreferences
import java.util.Calendar
import java.util.Date

class HabitAdapter(
    private var habits: MutableList<Habit>,
    private val preferencesManager: SharedPreferences,
    private val onEditClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitName: TextView = itemView.findViewById(R.id.tv_habit_name)
        val habitDescription: TextView = itemView.findViewById(R.id.tv_habit_description)
        val habitCategory: TextView = itemView.findViewById(R.id.tv_habit_category)
        val completionCheckbox: CheckBox = itemView.findViewById(R.id.cb_habit_completion)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_habit)
        val progressText: TextView = itemView.findViewById(R.id.tv_progress_text)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_habit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_habit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.habitName.text = habit.name
        holder.habitDescription.text = habit.description
        holder.habitCategory.text = habit.category

        // Check if habit is completed today
        val isCompletedToday = preferencesManager.isHabitCompletedToday(habit.id)
        holder.completionCheckbox.isChecked = isCompletedToday

        // Calculate weekly progress
        val weeklyProgress = calculateWeeklyProgress(habit.id)
        holder.progressBar.progress = weeklyProgress
        holder.progressText.text = "$weeklyProgress% this week"

        // Set completion checkbox listener
        holder.completionCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != isCompletedToday) {
                preferencesManager.toggleHabitCompletion(habit.id)
                notifyItemChanged(position) // Update progress
            }
        }

        // Set edit button listener
        holder.btnEdit.setOnClickListener {
            onEditClick(habit)
        }

        // Set delete button listener
        holder.btnDelete.setOnClickListener {
            onDeleteClick(habit)
        }
    }

    override fun getItemCount(): Int = habits.size

    private fun calculateWeeklyProgress(habitId: String): Int {
        val completions = preferencesManager.getHabitCompletions()
        val calendar = Calendar.getInstance()
        // Get start of week (Monday)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val weekStart = calendar.time
        val weekEnd = Date(weekStart.time + 7 * 24 * 60 * 60 * 1000)

        val weeklyCompletions = completions.filter { completion ->
            completion.habitId == habitId && completion.isCompleted &&
                    completion.completedAt >= weekStart.time && completion.completedAt < weekEnd.time // Fixed property name
        }

        return ((weeklyCompletions.size.toFloat() / 7) * 100).toInt()
    }

    fun updateHabits(newHabits: List<Habit>) {
        habits.clear()
        habits.addAll(newHabits)
        notifyDataSetChanged()
    }

    fun removeHabit(habit: Habit) {
        val position = habits.indexOf(habit)
        if (position != -1) {
            habits.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
