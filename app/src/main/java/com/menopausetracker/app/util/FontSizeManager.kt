package com.menopausetracker.app.util

import android.content.Context
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * Utility class to manage font size settings across the app
 */
class FontSizeManager {
    companion object {
        const val FONT_SIZE_SMALL = "small"
        const val FONT_SIZE_NORMAL = "normal"
        const val FONT_SIZE_LARGE = "large"

        // Base font sizes in SP
        const val SIZE_SMALL = 14f
        const val SIZE_NORMAL = 16f
        const val SIZE_LARGE = 20f

        /**
         * Apply the saved font size preference to a TextView
         */
        fun applyFontSize(context: Context, textView: TextView, isHeading: Boolean = false) {
            val sharedPrefs = context.getSharedPreferences(
                "app_preferences", Context.MODE_PRIVATE
            )
            val fontSize = sharedPrefs.getString("font_size", FONT_SIZE_NORMAL)

            val textSize = when (fontSize) {
                FONT_SIZE_SMALL -> SIZE_SMALL
                FONT_SIZE_NORMAL -> SIZE_NORMAL
                FONT_SIZE_LARGE -> SIZE_LARGE
                else -> SIZE_NORMAL
            }

            // If it's a heading, make it slightly larger
            textView.textSize = if (isHeading) textSize * 1.25f else textSize
        }

        /**
         * Get the current font size value from preferences
         */
        fun getCurrentFontSize(context: Context): String {
            val sharedPrefs = context.getSharedPreferences(
                "app_preferences", Context.MODE_PRIVATE
            )
            return sharedPrefs.getString("font_size", FONT_SIZE_NORMAL) ?: FONT_SIZE_NORMAL
        }
    }
}
