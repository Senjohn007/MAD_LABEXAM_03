package com.example.wellnesstracker

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        // Initialize animations
        setupAnimations()

        // Using a Handler with the main looper to delay the transition
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if activity is still active before navigating
            if (!isFinishing && !isDestroyed) {
                // Start the first onboarding activity
                val intent = Intent(this, FirstOnboard::class.java)
                startActivity(intent)
                finish() // Close the splash activity so user can't go back to it
            }
        }, SPLASH_DELAY)
    }

    private fun setupAnimations() {
        // Fade in animation for the entire view
        val rootView = findViewById<View>(android.R.id.content)
        rootView.alpha = 0f
        rootView.animate()
            .alpha(1f)
            .setDuration(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Pulse animation for the logo
        val logo = findViewById<View>(R.id.iv_splash_logo)
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.1f, 1.0f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.1f, 1.0f)
        val pulse = ObjectAnimator.ofPropertyValuesHolder(logo, scaleX, scaleY)
        pulse.duration = 1000
        pulse.repeatCount = ValueAnimator.INFINITE
        pulse.repeatMode = ValueAnimator.RESTART
        pulse.interpolator = AccelerateDecelerateInterpolator()
        pulse.start()

        // Subtle fade-in for the tagline
        val tagline = findViewById<View>(R.id.tv_tagline)
        tagline.alpha = 0f
        tagline.animate()
            .alpha(0.9f)
            .setDuration(1000)
            .setStartDelay(300)
            .start()

        // Subtle fade-in for the loading elements
        val loadingElements = listOf<View>(
            findViewById(R.id.pb_loading),
            findViewById(R.id.tv_loading)
        )

        loadingElements.forEachIndexed { index, view ->
            view.alpha = 0f
            view.animate()
                .alpha(0.8f)
                .setDuration(800)
                .setStartDelay(500L + (index * 150L))
                .start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up any pending animations or handlers
        // This helps prevent memory leaks
    }
}