package com.example.wellnesstracker

import android.app.Application
import com.example.wellnesstracker.utils.Prefs

class WellnessTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
    }
}
