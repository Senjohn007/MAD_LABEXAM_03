package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wellnesstracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)

        // Set up bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already in home, do nothing or refresh
                    true
                }
                R.id.nav_hydration -> {
                    // Navigate to HydrationActivity
                    navigateToHydration()
                    true
                }
                R.id.nav_mood -> {
                    // Navigate to MoodActivity
                    navigateToMood()
                    true
                }
                R.id.nav_steps -> {
                    // Navigate to StepsActivity
                    navigateToSteps()
                    true
                }
                else -> false
            }
        }

        // Set up click listeners for the cards
        binding.cardHydration.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.nav_hydration
            navigateToHydration()
        }

        binding.cardMood.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.nav_mood
            navigateToMood()
        }

        binding.cardSteps.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.nav_steps
            navigateToSteps()
        }

        // Set up profile icon click listener
        binding.ivProfile.setOnClickListener {
            // Navigate to ProfileActivity or show profile dialog
            navigateToProfile()
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure home is selected when returning to MainActivity
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun navigateToHydration() {
        val intent = Intent(this, HydrationActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMood() {
        val intent = Intent(this, MoodActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSteps() {
        val intent = Intent(this, StepsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }
}