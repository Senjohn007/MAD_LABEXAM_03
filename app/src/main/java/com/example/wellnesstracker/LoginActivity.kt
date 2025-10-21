package com.example.wellnesstracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: Button
    private lateinit var forgotPassword: TextView
    private lateinit var tvSignupRedirect: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMainActivity()
            return
        }

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        emailInputLayout = findViewById(R.id.email_input_layout)
        passwordInputLayout = findViewById(R.id.password_input_layout)
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        btnLogin = findViewById(R.id.btn_login)
        btnGoogleLogin = findViewById(R.id.btn_google_login)
        forgotPassword = findViewById(R.id.forgot_password)
        tvSignupRedirect = findViewById(R.id.tv_signup_redirect)
    }

    private fun setupClickListeners() {
        // Login button click
        btnLogin.setOnClickListener {
            validateAndLogin()
        }

        // Google login button click
        btnGoogleLogin.setOnClickListener {
            // Implement Google Sign-In later
            Toast.makeText(this, "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Forgot password click
        forgotPassword.setOnClickListener {
            // Implement forgot password functionality
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Sign up redirect click
        tvSignupRedirect.setOnClickListener {
            navigateToSignup()
        }
    }

    private fun validateAndLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Clear previous errors
        emailInputLayout.error = null
        passwordInputLayout.error = null

        var isValid = true

        // Validate email
        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailInputLayout.error = "Please enter a valid email"
            isValid = false
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (isValid) {
            performLogin(email, password)
        }
    }

    private fun performLogin(email: String, password: String) {
        // Show loading state
        btnLogin.isEnabled = false
        btnLogin.text = "Signing In..."

        // Simulate login process (replace with actual authentication)
        android.os.Handler(mainLooper).postDelayed({
            // Simple validation for demo (replace with actual authentication logic)
            if (isValidCredentials(email, password)) {

                // Extract user name from email or use demo data
                val userName = getUserNameFromEmail(email)

                // Save user session using SessionManager
                sessionManager.createSession(userName, email)

                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                btnLogin.isEnabled = true
                btnLogin.text = "Sign In"
            }
        }, 1500) // 1.5 second delay to simulate network request
    }

    private fun getUserNameFromEmail(email: String): String {
        return when (email.lowercase()) {
            "senuja@example.com" -> "Senuja"
            "user@example.com" -> "John Doe"
            "test@example.com" -> "Test User"
            "admin@example.com" -> "Admin User"
            else -> {
                // Extract name from email username part and capitalize
                val emailUsername = email.substringBefore("@")
                if (emailUsername.contains(".")) {
                    // Handle names like "john.doe@example.com"
                    emailUsername.split(".").joinToString(" ") {
                        it.replaceFirstChar { char -> char.uppercase() }
                    }
                } else {
                    // Handle single names like "senuja@example.com"
                    emailUsername.replaceFirstChar { it.uppercase() }
                }
            }
        }
    }

    private fun isValidCredentials(email: String, password: String): Boolean {
        // Demo credentials for testing - replace with actual authentication logic
        val validCredentials = mapOf(
            "senuja@example.com" to "password123",
            "user@example.com" to "password123",
            "test@example.com" to "test123",
            "admin@example.com" to "admin123"
        )

        // Check if credentials match demo data
        if (validCredentials[email.lowercase()] == password) {
            return true
        }

        // Fallback: any email with password length >= 6 (for testing)
        return email.contains("@") && password.length >= 6
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun navigateToSignup() {
        startActivity(Intent(this, SignupActivity::class.java))
        // Don't finish() here so user can go back to login
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity() // Exit app when back is pressed from login
    }

    override fun onResume() {
        super.onResume()
        // Check if user is logged in when returning to this activity
        if (sessionManager.isLoggedIn()) {
            navigateToMainActivity()
        }
    }
}
