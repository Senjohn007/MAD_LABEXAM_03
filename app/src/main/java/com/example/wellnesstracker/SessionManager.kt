package com.example.wellnesstracker

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    companion object {
        private const val PREF_NAME = "UserPrefs"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_LOGIN_TIME = "login_time"
    }

    init {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    // Save user session
    fun createSession(name: String, email: String) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
        editor.apply()
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Get user name
    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    // Get user email
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    // Get first name only
    fun getFirstName(): String {
        val fullName = getUserName()
        return if (fullName != null) {
            fullName.split(" ").firstOrNull() ?: fullName
        } else {
            "User"
        }
    }

    // Clear session (logout)
    fun clearSession() {
        editor.clear()
        editor.apply()
    }

    // Get time-based greeting
    fun getTimeBasedGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }

    // Get full personalized greeting
    fun getPersonalizedGreeting(): String {
        val timeGreeting = getTimeBasedGreeting()
        val firstName = getFirstName()
        return "$timeGreeting, $firstName!"
    }
}
