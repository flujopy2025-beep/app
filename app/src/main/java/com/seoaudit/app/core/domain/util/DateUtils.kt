package com.seoaudit.app.core.domain.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    fun toFormattedStrings(): Pair<String, String> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        return startDate.format(formatter) to endDate.format(formatter)
    }

    val dayCount: Int get() = (endDate.toEpochDay() - startDate.toEpochDay()).toInt() + 1
}

object DateUtils {
    /**
     * Calculates the default date range for GSC queries.
     * The range starts 28 days before yesterday and ends yesterday (inclusive).
     * This produces a range of exactly 28 days.
     *
     * @param today The current date (defaults to LocalDate.now())
     * @return DateRange from (today - 29 days) to (today - 1 day)
     */
    fun defaultDateRange(today: LocalDate = LocalDate.now()): DateRange {
        val endDate = today.minusDays(1) // yesterday
        val startDate = endDate.minusDays(27) // 28 days total including end
        return DateRange(startDate = startDate, endDate = endDate)
    }
}
