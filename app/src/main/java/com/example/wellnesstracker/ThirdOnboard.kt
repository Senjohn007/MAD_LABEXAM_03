package com.example.wellnesstracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class ThirdOnboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.third_onboard)

        // Find buttons
        val btnGetStarted = findViewById<Button>(R.id.btn_get_started)
        val tvSkip = findViewById<TextView>(R.id.tv_skip_step3)

        // Get Started button - Navigate to MainActivity (simplified)
        btnGetStarted.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Skip/Customize later button - Navigate to MainActivity (simplified)
        tvSkip.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
