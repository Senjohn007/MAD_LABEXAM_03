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

        // Skip button - Navigate to SecondOnboard
        tvSkip.setOnClickListener {
            val intent = Intent(this, SecondOnboard::class.java)
            startActivity(intent)
        }
    }
}
