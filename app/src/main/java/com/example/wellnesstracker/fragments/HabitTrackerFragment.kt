package com.example.wellnesstracker.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.example.wellnesstracker.R
import com.example.wellnesstracker.adapters.HabitAdapter
import com.example.wellnesstracker.models.Habit
import com.example.wellnesstracker.utils.SharedPreferences

class HabitTrackerFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var fabAddHabit: FloatingActionButton
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var preferencesManager: SharedPreferences
    private val habits = mutableListOf<Habit>()

    private val categories = arrayOf(
        "General", "Health", "Exercise", "Water", "Meditation",
        "Reading", "Sleep", "Nutrition", "Productivity", "Social"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habit_tracker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        loadHabits()
        updateEmptyState()

        fabAddHabit.setOnClickListener {
            showAddEditHabitDialog()
        }
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rv_habits)
        emptyStateText = view.findViewById(R.id.tv_empty_state)
        fabAddHabit = view.findViewById(R.id.fab_add_habit)
        preferencesManager = SharedPreferences(requireContext())
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            habits = habits,
            preferencesManager = preferencesManager,
            onEditClick = { habit -> showAddEditHabitDialog(habit) },
            onDeleteClick = { habit -> showDeleteConfirmationDialog(habit) }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = habitAdapter
        }
    }

    private fun loadHabits() {
        habits.clear()
        habits.addAll(preferencesManager.getHabits())
        habitAdapter.notifyDataSetChanged()
    }

    private fun updateEmptyState() {
        if (habits.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
        }
    }

    private fun showAddEditHabitDialog(habitToEdit: Habit? = null) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_edit_habit, null)

        val titleText = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.et_habit_name)
        val descriptionInput = dialogView.findViewById<TextInputEditText>(R.id.et_habit_description)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel)
        val saveButton = dialogView.findViewById<Button>(R.id.btn_save)

        // Setup spinner immediately
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        // Pre-fill for editing
        habitToEdit?.let { habit ->
            titleText.text = "Edit Habit"
            nameInput.setText(habit.name)
            descriptionInput.setText(habit.description)
            val categoryIndex = categories.indexOf(habit.category)
            if (categoryIndex >= 0) {
                categorySpinner.setSelection(categoryIndex)
            }
            saveButton.text = "UPDATE"
        }

        // Create dialog with custom view
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Show dialog first
        dialog.show()

        // Set proper dialog dimensions to ensure buttons are visible
        dialog.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.9).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }

        // Handle button clicks from the custom layout
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val category = if (categorySpinner.selectedItem != null) {
                categorySpinner.selectedItem.toString()
            } else {
                "General"
            }

            // Validation
            if (name.isEmpty()) {
                nameInput.error = "Please enter habit name"
                nameInput.requestFocus()
                return@setOnClickListener
            }

            // Save or update habit
            try {
                if (habitToEdit != null) {
                    val updatedHabit = habitToEdit.copy(
                        name = name,
                        description = description,
                        category = category
                    )
                    updateHabit(habitToEdit, updatedHabit)
                } else {
                    val newHabit = Habit(
                        name = name,
                        description = description,
                        category = category
                    )
                    addHabit(newHabit)
                }
                dialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving habit: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Set focus to name input
        nameInput.requestFocus()
    }

    private fun addHabit(habit: Habit) {
        try {
            habits.add(habit)
            preferencesManager.saveHabits(habits)
            habitAdapter.notifyItemInserted(habits.size - 1)
            updateEmptyState()
            Toast.makeText(context, "Habit added successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error adding habit: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHabit(oldHabit: Habit, newHabit: Habit) {
        try {
            val index = habits.indexOf(oldHabit)
            if (index >= 0) {
                habits[index] = newHabit
                preferencesManager.saveHabits(habits)
                habitAdapter.notifyItemChanged(index)
                Toast.makeText(context, "Habit updated successfully!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error updating habit: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteHabit(habit)
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(true)
            .show()
    }

    private fun deleteHabit(habit: Habit) {
        try {
            habits.remove(habit)
            preferencesManager.saveHabits(habits)

            // Also remove all completions for this habit
            val completions = preferencesManager.getHabitCompletions().toMutableList()
            completions.removeAll { it.habitId == habit.id }
            preferencesManager.saveHabitCompletions(completions)

            habitAdapter.removeHabit(habit)
            updateEmptyState()
            Toast.makeText(context, "Habit deleted successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error deleting habit: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Optional: Add method to refresh habits when fragment becomes visible
    override fun onResume() {
        super.onResume()
        loadHabits()
    }
}
