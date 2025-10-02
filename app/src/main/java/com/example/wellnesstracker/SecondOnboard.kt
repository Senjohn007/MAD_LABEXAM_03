package com.example.wellnesstracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class SecondOnboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_onboard)

        // Find buttons
        val btnContinue = findViewById<Button>(R.id.btn_continue_step2)
        val tvSkip = findViewById<TextView>(R.id.tv_skip_step2)

        // Continue button - Navigate to ThirdOnboard
        btnContinue.setOnClickListener {
            val intent = Intent(this, ThirdOnboard::class.java)
            startActivity(intent)
            // Don't call finish() here if you want to maintain back stack
        }

        // Skip button - Navigate directly to MainActivity
        tvSkip.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
