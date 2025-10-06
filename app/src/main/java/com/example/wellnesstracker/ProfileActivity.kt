package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileActivity : AppCompatActivity() {

    private val TAG = "ProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "ProfileActivity onCreate started")

            enableEdgeToEdge()

            // Simply set the layout - no binding needed
            setContentView(R.layout.profile_page)

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            setupToolbar()
            setupClickListeners() // Add click listeners

            // Show that profile is loaded
            showToast("Profile loaded successfully!")

            Log.d(TAG, "ProfileActivity onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in ProfileActivity onCreate", e)
            showToast("Profile Settings - Coming Soon!")
        }
    }

    private fun setupToolbar() {
        try {
            // Find toolbar by ID and set up back navigation
            val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)

            // Handle back button click
            toolbar.setNavigationOnClickListener {
                finish() // Go back to MainActivity
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    // Setup click listeners for navigation
    private fun setupClickListeners() {
        try {
            // Settings Card Click Listener
            val settingsCard = findViewById<CardView>(R.id.card_settings)
            settingsCard.setOnClickListener {
                Log.d(TAG, "Settings card clicked")
                navigateToSettings()
            }

            // NEW: Help & Support Card Click Listener
            val helpCard = findViewById<CardView>(R.id.card_help)
            helpCard.setOnClickListener {
                Log.d(TAG, "Help & Support card clicked")
                navigateToSupport()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    // Navigate to Settings Activity
    private fun navigateToSettings() {
        try {
            Log.d(TAG, "Navigating to SettingsActivity")
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to SettingsActivity", e)
            showToast("Unable to open Settings")
        }
    }

    // NEW: Navigate to Support Activity
    private fun navigateToSupport() {
        try {
            Log.d(TAG, "Navigating to SupportActivity")
            val intent = Intent(this, SupportActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to SupportActivity", e)
            showToast("Unable to open Help & Support")
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
        Log.d(TAG, "ProfileActivity destroyed")
    }

    // Handle back button in action bar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
