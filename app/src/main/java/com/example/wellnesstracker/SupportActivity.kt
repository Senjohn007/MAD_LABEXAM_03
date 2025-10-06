package com.example.wellnesstracker

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SupportActivity : AppCompatActivity() {

    private val TAG = "SupportActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "SupportActivity onCreate started")

            enableEdgeToEdge()

            // Set the help & support layout
            setContentView(R.layout.help_support_page)

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            setupToolbar()
            setupClickListeners()

            // Show that support page is loaded
            showToast("Help & Support loaded successfully!")

            Log.d(TAG, "SupportActivity onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in SupportActivity onCreate", e)
            showToast("Help & Support - Coming Soon!")
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

    private fun setupClickListeners() {
        try {
            // Email Support Card
            val emailSupportCard = findViewById<CardView>(R.id.card_email_support)
            emailSupportCard.setOnClickListener {
                Log.d(TAG, "Email Support clicked")
                showToast("Email Support - Coming Soon!")
            }

            // Live Chat Card
            val liveChatCard = findViewById<CardView>(R.id.card_live_chat)
            liveChatCard.setOnClickListener {
                Log.d(TAG, "Live Chat clicked")
                showToast("Live Chat - Coming Soon!")
            }

            // Rate App Button
            val rateAppButton = findViewById<android.widget.Button>(R.id.btn_rate_app)
            rateAppButton.setOnClickListener {
                Log.d(TAG, "Rate App clicked")
                showToast("Rate App - Coming Soon!")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
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
        Log.d(TAG, "SupportActivity destroyed")
    }

    // Handle back button in action bar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
