package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wellnesstracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

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
                        // Already on home, show confirmation
                        showToast("You're on the Home screen")
                        true
                    }
                    R.id.nav_hydration -> {
                        navigateToHydration()
                        true
                    }
                    R.id.nav_mood -> {
                        navigateToMood()
                        true
                    }
                    R.id.nav_steps -> {
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
            binding.cardHydration.setOnClickListener {
                Log.d(TAG, "Hydration card clicked")
                binding.bottomNavigation.selectedItemId = R.id.nav_hydration
            }

            binding.cardMood.setOnClickListener {
                Log.d(TAG, "Mood card clicked")
                binding.bottomNavigation.selectedItemId = R.id.nav_mood
            }

            binding.cardSteps.setOnClickListener {
                Log.d(TAG, "Steps card clicked")
                binding.bottomNavigation.selectedItemId = R.id.nav_steps
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
            // Ensure home is selected when returning to MainActivity
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            Log.d(TAG, "MainActivity resumed, home navigation selected")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    private fun navigateToHydration() {
        try {
            Log.d(TAG, "Navigating to HydrationActivity")
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

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity destroyed")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity paused")
    }
}
