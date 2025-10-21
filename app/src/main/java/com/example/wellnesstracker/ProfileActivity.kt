package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileActivity : AppCompatActivity() {

    private val TAG = "ProfileActivity"
    private lateinit var sessionManager: SessionManager

    // UI Components
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var fabChangePicture: FloatingActionButton
    private lateinit var llGoalsContainer: LinearLayout
    private lateinit var btnEditGoals: Button
    private lateinit var cardSettings: CardView
    private lateinit var cardHelp: CardView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "ProfileActivity onCreate started")

            enableEdgeToEdge()
            setContentView(R.layout.profile_page)

            // Initialize SessionManager
            sessionManager = SessionManager(this)

            // Check if user is logged in
            if (!sessionManager.isLoggedIn()) {
                navigateToLogin()
                return
            }

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            initializeViews()
            setupToolbar()
            loadUserData()
            setupClickListeners()
            setupWellnessGoals()

            Log.d(TAG, "ProfileActivity onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in ProfileActivity onCreate", e)
            showToast("Error loading profile: ${e.message}")
        }
    }

    private fun initializeViews() {
        try {
            tvUserName = findViewById(R.id.tv_user_name)
            tvUserEmail = findViewById(R.id.tv_user_email)
            btnEditProfile = findViewById(R.id.btn_edit_profile)
            fabChangePicture = findViewById(R.id.fab_change_picture)
            llGoalsContainer = findViewById(R.id.ll_goals_container)
            btnEditGoals = findViewById(R.id.btn_edit_goals)
            cardSettings = findViewById(R.id.card_settings)
            cardHelp = findViewById(R.id.card_help)
            btnLogout = findViewById(R.id.btn_logout)

            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
        }
    }

    private fun setupToolbar() {
        try {
            val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
                title = "Profile"
            }

            toolbar.setNavigationOnClickListener {
                finish()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    private fun loadUserData() {
        try {
            // Load user data from session
            val userName = sessionManager.getUserName() ?: "User"
            val userEmail = sessionManager.getUserEmail() ?: "No email"

            tvUserName.text = userName
            tvUserEmail.text = userEmail

            Log.d(TAG, "User data loaded: $userName, $userEmail")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading user data", e)
            showToast("Error loading user data")
        }
    }

    private fun setupWellnessGoals() {
        try {
            // Clear existing goals
            llGoalsContainer.removeAllViews()

            // Sample wellness goals - you can make this dynamic
            val goals = listOf(
                "🏃 Daily Exercise" to "Stay active with 30 minutes of exercise",
                "💧 Hydration" to "Drink 8 glasses of water daily",
                "😊 Mood Tracking" to "Monitor and improve emotional wellness",
                "💤 Sleep Quality" to "Get 7-8 hours of quality sleep"
            )

            goals.forEach { (title, description) ->
                addGoalItem(title, description)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up wellness goals", e)
        }
    }

    private fun addGoalItem(title: String, description: String) {
        try {
            val goalView = layoutInflater.inflate(R.layout.item_wellness_goal, llGoalsContainer, false)

            val tvGoalTitle = goalView.findViewById<TextView>(R.id.tv_goal_title)

            // Combine title and description into one text
            tvGoalTitle.text = "$title\n$description"

            llGoalsContainer.addView(goalView)

        } catch (e: Exception) {
            Log.e(TAG, "Error adding goal item", e)
            // Fallback: add simple text view
            val textView = TextView(this).apply {
                text = title
                textSize = 16f
                setPadding(0, 8, 0, 8)
                setTextColor(getColor(R.color.black))
            }
            llGoalsContainer.addView(textView)
        }
    }


    private fun setupClickListeners() {
        try {
            // Edit Profile Button
            btnEditProfile.setOnClickListener {
                Log.d(TAG, "Edit profile button clicked")
                showToast("Edit Profile feature coming soon!")
            }

            // Change Picture FAB
            fabChangePicture.setOnClickListener {
                Log.d(TAG, "Change picture button clicked")
                showToast("Change picture feature coming soon!")
            }

            // Edit Goals Button
            btnEditGoals.setOnClickListener {
                Log.d(TAG, "Edit goals button clicked")
                showToast("Edit goals feature coming soon!")
            }

            // Settings Card
            cardSettings.setOnClickListener {
                Log.d(TAG, "Settings card clicked")
                navigateToSettings()
            }

            // Help & Support Card
            cardHelp.setOnClickListener {
                Log.d(TAG, "Help & Support card clicked")
                navigateToSupport()
            }

            // Logout Button
            btnLogout.setOnClickListener {
                Log.d(TAG, "Logout button clicked")
                showLogoutDialog()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun showLogoutDialog() {
        try {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout from AURA?")
                .setPositiveButton("Logout") { _, _ ->
                    performLogout()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing logout dialog", e)
            performLogout() // Fallback to direct logout
        }
    }

    private fun performLogout() {
        try {
            Log.d(TAG, "Performing logout")

            // Clear user session
            sessionManager.clearSession()

            showToast("Logged out successfully")

            // Navigate to login screen and clear task stack
            navigateToLogin()

        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            showToast("Error during logout")
            navigateToLogin() // Still navigate to login
        }
    }

    private fun navigateToLogin() {
        try {
            Log.d(TAG, "Navigating to LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to login", e)
            finish()
        }
    }

    private fun navigateToSettings() {
        try {
            Log.d(TAG, "Navigating to SettingsActivity")

            // Check if SettingsActivity exists, otherwise show placeholder
            try {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                showToast("Settings feature coming soon!")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to SettingsActivity", e)
            showToast("Settings feature coming soon!")
        }
    }

    private fun navigateToSupport() {
        try {
            Log.d(TAG, "Navigating to SupportActivity")

            // Check if SupportActivity exists, otherwise show placeholder
            try {
                val intent = Intent(this, SupportActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                showToast("Help & Support feature coming soon!")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to SupportActivity", e)
            showToast("Help & Support feature coming soon!")
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast", e)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ProfileActivity destroyed")
    }
}
