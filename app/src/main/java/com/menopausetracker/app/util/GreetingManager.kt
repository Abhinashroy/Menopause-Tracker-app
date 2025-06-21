package com.menopausetracker.app.util

import android.content.Context
import com.menopausetracker.app.R
import java.util.*

/**
 * Utility class to manage daily greeting messages
 */
class GreetingManager(private val context: Context) {

    /**
     * Returns a random greeting message from the predefined list
     * The message stays the same throughout the day and changes at midnight
     */
    fun getDailyGreeting(): String {
        val greetings = context.resources.getStringArray(R.array.daily_greetings)
        val calendar = Calendar.getInstance()

        // Use the day of year as seed for random selection
        // This ensures the same message appears all day, but changes each day
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % greetings.size

        return greetings[index]
    }
}
