package com.example.wellnesstracker.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }

    fun getCurrentTime(): String {
        return timeFormat.format(Date())
    }

    fun formatDateForDisplay(date: String): String {
        return try {
            val parsedDate = dateFormat.parse(date)
            if (parsedDate != null) {
                displayDateFormat.format(parsedDate)
            } else {
                date
            }
        } catch (e: Exception) {
            date
        }
    }

    fun formatTimeForDisplay(hhMm: String): String = hhMm

    fun getDateDaysAgo(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormat.format(calendar.time)
    }

    fun isToday(date: String): Boolean {
        return date == getCurrentDate()
    }

    fun getShortDayName(date: String): String {
        return try {
            val parsedDate = dateFormat.parse(date)
            if (parsedDate != null) {
                SimpleDateFormat("E", Locale.getDefault()).format(parsedDate)
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
