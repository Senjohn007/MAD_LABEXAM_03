package com.example.wellnesstracker

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    private val TAG = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "SettingsActivity onCreate started")

            enableEdgeToEdge()

            // Set the settings layout
            setContentView(R.layout.settings_page)

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            setupToolbar()

            // Show that settings is loaded
            showToast("Settings loaded successfully!")

            Log.d(TAG, "SettingsActivity onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in SettingsActivity onCreate", e)
            showToast("Settings - Coming Soon!")
        }
    }

    private fun setupToolbar() {
        try {
            // Find toolbar by ID and set up back navigation
            val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)

            // Handle back button click - go back to ProfileActivity
            toolbar.setNavigationOnClickListener {
                finish() // Go back to ProfileActivity
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
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
        Log.d(TAG, "SettingsActivity destroyed")
    }

    // Handle back button in action bar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
