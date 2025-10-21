package com.example.wellnesstracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class FirstOnboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_onboard)

        // Find the continue button
        val btnContinue = findViewById<Button>(R.id.btn_continue)
        val tvSkip = findViewById<TextView>(R.id.tv_skip)

        // Continue button - Navigate to SecondOnboard
        btnContinue.setOnClickListener {
            val intent = Intent(this, SecondOnboard::class.java)
            startActivity(intent)
        }

        // Skip button - Clear session and navigate to LoginActivity
        tvSkip.setOnClickListener {
            // Clear any existing session first
            val sessionManager = SessionManager(this)
            sessionManager.clearSession()

            // Then navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
