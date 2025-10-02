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
                        showToast("Home")
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
                    else -> false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up bottom navigation", e)
        }
    }

    private fun setupCardClickListeners() {
        try {
            binding.cardHydration.setOnClickListener {
                binding.bottomNavigation.selectedItemId = R.id.nav_hydration
            }

            binding.cardMood.setOnClickListener {
                binding.bottomNavigation.selectedItemId = R.id.nav_mood
            }

            binding.cardSteps.setOnClickListener {
                binding.bottomNavigation.selectedItemId = R.id.nav_steps
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up card click listeners", e)
        }
    }

    private fun setupProfileClickListener() {
        try {
            binding.ivProfile.setOnClickListener {
                navigateToProfile()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up profile click listener", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    private fun navigateToHydration() {
        try {
            val intent = Intent(this, HydrationActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Hydration Tracker - Coming Soon!")
        }
    }

    private fun navigateToMood() {
        try {
            val intent = Intent(this, MoodActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Mood Journal - Coming Soon!")
        }
    }

    private fun navigateToSteps() {
        try {
            val intent = Intent(this, StepsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Step Counter - Coming Soon!")
        }
    }

    private fun navigateToProfile() {
        try {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Profile Settings - Coming Soon!")
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast", e)
        }
    }
}
