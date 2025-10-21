package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignupActivity : AppCompatActivity() {

    private lateinit var fullnameInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var fullnameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var checkboxTerms: CheckBox
    private lateinit var btnSignup: Button
    private lateinit var btnGoogleSignup: Button
    private lateinit var tvLoginRedirect: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        fullnameInputLayout = findViewById(R.id.fullname_input_layout)
        emailInputLayout = findViewById(R.id.email_input_layout)
        passwordInputLayout = findViewById(R.id.password_input_layout)
        confirmPasswordInputLayout = findViewById(R.id.confirm_password_input_layout)
        fullnameEditText = findViewById(R.id.fullname_edit_text)
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text)
        checkboxTerms = findViewById(R.id.checkbox_terms)
        btnSignup = findViewById(R.id.btn_signup)
        btnGoogleSignup = findViewById(R.id.btn_google_signup)
        tvLoginRedirect = findViewById(R.id.tv_login_redirect)
    }

    private fun setupClickListeners() {
        // Signup button click
        btnSignup.setOnClickListener {
            validateAndSignup()
        }

        // Google signup button click
        btnGoogleSignup.setOnClickListener {
            // Implement Google Sign-Up later
            Toast.makeText(this, "Google Sign-Up coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Login redirect click
        tvLoginRedirect.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun validateAndSignup() {
        val fullName = fullnameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        // Clear previous errors
        fullnameInputLayout.error = null
        emailInputLayout.error = null
        passwordInputLayout.error = null
        confirmPasswordInputLayout.error = null

        var isValid = true

        // Validate full name
        if (fullName.isEmpty()) {
            fullnameInputLayout.error = "Full name is required"
            isValid = false
        } else if (fullName.length < 2) {
            fullnameInputLayout.error = "Full name must be at least 2 characters"
            isValid = false
        }

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
        } else if (password.length < 8) {
            passwordInputLayout.error = "Password must be at least 8 characters"
            isValid = false
        } else if (!isValidPassword(password)) {
            passwordInputLayout.error = "Password must contain at least one letter and one number"
            isValid = false
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordInputLayout.error = "Passwords do not match"
            isValid = false
        }

        // Validate terms checkbox
        if (!checkboxTerms.isChecked) {
            Toast.makeText(this, "Please accept the Terms and Conditions", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (isValid) {
            performSignup(fullName, email, password)
        }
    }

    private fun performSignup(fullName: String, email: String, password: String) {
        // Show loading state
        btnSignup.isEnabled = false
        btnSignup.text = "Creating Account..."

        // Simulate signup process (replace with actual registration)
        android.os.Handler(mainLooper).postDelayed({
            // Simple demo - assume signup is always successful
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

            // Save user data locally (you can implement SharedPreferences or database)
            saveUserData(fullName, email)

            navigateToMainActivity()
        }, 2000) // 2 second delay to simulate network request
    }

    private fun saveUserData(fullName: String, email: String) {
        // Save to SharedPreferences for demo purposes
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_name", fullName)
            putString("user_email", email)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        // Check if password contains at least one letter and one number
        return password.any { it.isLetter() } && password.any { it.isDigit() }
    }

    private fun navigateToLogin() {
        finish() // This will go back to LoginActivity
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity() // Clear all previous activities from stack
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // This will naturally go back to LoginActivity
    }
}
