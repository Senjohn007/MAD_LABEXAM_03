package com.example.wellnesstracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.wellnesstracker.databinding.ActivityMainBinding
import com.example.wellnesstracker.ui.HabitTrackerFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"
    private var currentDestination: String = "home"
    private var habitFragment: HabitTrackerFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "MainActivity onCreate started")

            enableEdgeToEdge()
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            setupToolbar()
            setupBottomNavigation()
            setupCardClickListeners()
            setupProfileClickListener()

            // Set default view (Home) - only if not restored from saved state
            if (savedInstanceState == null) {
                showHomeView()
                binding.bottomNavigation.selectedItemId = R.id.nav_home
            }

            Log.d(TAG, "MainActivity onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in MainActivity onCreate", e)
            showToast("Error initializing app: ${e.message}")
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(binding.toolbar)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    private fun setupBottomNavigation() {
        try {
            binding.bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        Log.d(TAG, "Home navigation selected")
                        if (currentDestination != "home") {
                            showHomeView()
                        }
                        true
                    }
                    R.id.nav_habits -> {
                        Log.d(TAG, "Habits navigation selected")
                        if (currentDestination != "habits") {
                            navigateToHabits()
                        }
                        true
                    }
                    R.id.nav_hydration -> {
                        Log.d(TAG, "Hydration navigation selected")
                        navigateToHydration()
                        true
                    }
                    R.id.nav_mood -> {
                        Log.d(TAG, "Mood navigation selected")
                        navigateToMood()
                        true
                    }
                    R.id.nav_steps -> {
                        Log.d(TAG, "Steps navigation selected")
                        navigateToSteps()
                        true
                    }
                    else -> {
                        Log.w(TAG, "Unknown navigation item selected: ${item.itemId}")
                        false
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up bottom navigation", e)
        }
    }

    private fun setupCardClickListeners() {
        try {
            binding.cardHabits.setOnClickListener {
                Log.d(TAG, "Habits card clicked")
                binding.bottomNavigation.selectedItemId = R.id.nav_habits
            }

            binding.cardHydration.setOnClickListener {
                Log.d(TAG, "Hydration card clicked")
                navigateToHydration()
            }

            binding.cardMood.setOnClickListener {
                Log.d(TAG, "Mood card clicked")
                navigateToMood()
            }

            binding.cardSteps.setOnClickListener {
                Log.d(TAG, "Steps card clicked")
                navigateToSteps()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up card click listeners", e)
        }
    }

    private fun setupProfileClickListener() {
        try {
            binding.ivProfile.setOnClickListener {
                Log.d(TAG, "Profile icon clicked")
                navigateToProfile()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up profile click listener", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Log.d(TAG, "MainActivity resumed")

            // Reset to home when returning from activities (not from habits fragment)
            if (currentDestination != "home" && currentDestination != "habits") {
                showHomeView()
                binding.bottomNavigation.selectedItemId = R.id.nav_home
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    private fun showHomeView() {
        try {
            binding.scrollViewHome.visibility = android.view.View.VISIBLE

            // Clear fragment container if there's a fragment
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            }

            // Clear any remaining fragment
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment != null) {
                supportFragmentManager.beginTransaction()
                    .remove(currentFragment)
                    .commit()
            }

            binding.tvToolbarTitle.text = getString(R.string.main_toolbar_title)
            currentDestination = "home"
            habitFragment = null

            Log.d(TAG, "Home view displayed")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing home view", e)
        }
    }

    private fun navigateToHabits() {
        try {
            Log.d(TAG, "Navigating to Habits Fragment")

            binding.scrollViewHome.visibility = android.view.View.GONE

            // Reuse existing fragment if available to preserve state
            if (habitFragment == null) {
                habitFragment = HabitTrackerFragment()
            }

            replaceFragment(habitFragment!!)
            binding.tvToolbarTitle.text = "Daily Habits"
            currentDestination = "habits"

            showToast("Habit Tracker loaded")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to Habits", e)
            showToast("Unable to open Habit Tracker")
        }
    }

    private fun navigateToHydration() {
        try {
            Log.d(TAG, "Navigating to HydrationActivity")
            currentDestination = "hydration"
            val intent = Intent(this, HydrationActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to HydrationActivity", e)
            showToast("Unable to open Hydration Tracker")
        }
    }

    private fun navigateToMood() {
        try {
            Log.d(TAG, "Navigating to MoodActivity")
            currentDestination = "mood"
            val intent = Intent(this, MoodActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to MoodActivity", e)
            showToast("Unable to open Mood Journal")
        }
    }

    private fun navigateToSteps() {
        try {
            Log.d(TAG, "Navigating to StepsActivity")
            currentDestination = "steps"
            val intent = Intent(this, StepsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to StepsActivity", e)
            showToast("Unable to open Step Counter")
        }
    }

    private fun navigateToProfile() {
        try {
            Log.d(TAG, "Attempting to navigate to ProfileActivity")
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "Successfully navigated to ProfileActivity")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to ProfileActivity", e)
            showToast("Unable to open Profile Page")
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("habit_fragment")
                .commit()
            Log.d(TAG, "Fragment replaced: ${fragment.javaClass.simpleName}")
        } catch (e: Exception) {
            Log.e(TAG, "Error replacing fragment", e)
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast", e)
        }
    }

    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        try {
            // Handle back navigation for fragments
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                showHomeView()
                binding.bottomNavigation.selectedItemId = R.id.nav_home
            } else {
                super.onBackPressed()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling back press", e)
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("current_destination", currentDestination)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentDestination = savedInstanceState.getString("current_destination", "home")
    }

    override fun onDestroy() {
        super.onDestroy()
        habitFragment = null
        Log.d(TAG, "MainActivity destroyed")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity paused")
    }
}
