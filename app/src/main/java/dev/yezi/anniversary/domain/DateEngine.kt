package dev.yezi.anniversary.domain

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

enum class CountingMode { COUNT_DOWN, COUNT_UP }
enum class RepeatType { NONE, YEARLY, INTERVAL_DAYS }

data class DayCount(val signedDays: Long, val displayDays: Long, val isPast: Boolean)

object DateEngine {
    fun dayCount(target: LocalDate, today: LocalDate, mode: CountingMode, includeDueDate: Boolean): DayCount {
        val raw = ChronoUnit.DAYS.between(today, target)
        val adjusted = if (mode == CountingMode.COUNT_UP && includeDueDate && raw <= 0) raw - 1 else raw
        val signed = when (mode) {
            CountingMode.COUNT_DOWN -> adjusted
            CountingMode.COUNT_UP -> -adjusted
        }
        return DayCount(signed, kotlin.math.abs(signed), target.isBefore(today))
    }

    fun nextOccurrence(anchor: LocalDate, today: LocalDate, repeatType: RepeatType, interval: Int = 1): LocalDate? {
        require(interval > 0)
        return when (repeatType) {
            RepeatType.NONE -> anchor.takeIf { !it.isBefore(today) }
            RepeatType.YEARLY -> {
                var candidate = annualDate(anchor, today.year)
                if (candidate.isBefore(today)) candidate = annualDate(anchor, today.year + 1)
                candidate
            }
            RepeatType.INTERVAL_DAYS -> {
                if (!anchor.isBefore(today)) anchor else {
                    val elapsed = ChronoUnit.DAYS.between(anchor, today)
                    anchor.plusDays(((elapsed + interval - 1) / interval) * interval)
                }
            }
        }
    }

    fun annualDate(anchor: LocalDate, year: Int): LocalDate = when {
        anchor.monthValue == 2 && anchor.dayOfMonth == 29 && !java.time.Year.isLeap(year.toLong()) -> LocalDate.of(year, 2, 28)
        else -> LocalDate.of(year, anchor.monthValue, anchor.dayOfMonth)
    }

    fun addDays(date: LocalDate, days: Long): LocalDate = date.plusDays(days)
    fun daysBetween(first: LocalDate, second: LocalDate): Long = ChronoUnit.DAYS.between(first, second)
    fun monthLength(year: Int, month: Int): Int = YearMonth.of(year, month).lengthOfMonth()
}
