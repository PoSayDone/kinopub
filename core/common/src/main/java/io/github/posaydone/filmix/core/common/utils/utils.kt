package io.github.posaydone.filmix.core.common.utils

import android.content.Context
import io.github.posaydone.filmix.core.common.R
import java.text.DecimalFormat

/**
 * Formats a large number into a compact string with 'k' for thousands or 'm' for millions.
 * Handles numbers less than 1000 by returning them as is.
 *
 * Examples:
 * 999 -> "999"
 * 1000 -> "1.0k"
 * 1550 -> "1.6k"
 * 1100000 -> "1.1m"
 */
fun formatVoteCount(count: Int): String {
    if (count < 1000) return count.toString()

    // Use DecimalFormat to handle rounding to one decimal place correctly
    val decimalFormat = DecimalFormat("#.#")

    return when {
        count < 1_000_000 -> {
            val thousands = count / 1000.0
            "${decimalFormat.format(thousands)}k"
        }

        else -> {
            val millions = count / 1_000_000.0
            "${decimalFormat.format(millions)}m"
        }
    }
}


/**
 * Formats a duration in seconds into a localized string like "2 hours 5 minutes".
 * Uses Android's plural string resources to handle localization correctly.
 */
fun formatDuration(context: Context, totalSeconds: Int): String {
    if (totalSeconds <= 0) return ""

    val displayMinutes = (totalSeconds / 60).coerceAtLeast(1)

    val hours = displayMinutes / 60
    val minutes = displayMinutes % 60

    val parts = mutableListOf<String>()

    if (hours > 0) {
        val hoursString = context.resources.getQuantityString(R.plurals.hours_plural, hours, hours)
        parts.add(hoursString)
    }

    if (minutes > 0) {
        val minutesString =
            context.resources.getQuantityString(R.plurals.minutes_plural, minutes, minutes)
        parts.add(minutesString)
    }

    return parts.joinToString(" ")
}
