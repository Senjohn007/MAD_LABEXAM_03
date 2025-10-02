package com.example.wellnesstracker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "Profile Settings - Coming Soon!"
            textSize = 18f
            setPadding(32, 32, 32, 32)
        }

        setContentView(textView)
    }
}
