package dev.yezi.anniversary.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DateEngineTest {
    @Test fun sameDayIsZeroUnlessCountingUpIncludesTheEventDay() {
        val day = LocalDate.of(2026, 10, 18)
        assertEquals(0, DateEngine.dayCount(day, day, CountingMode.COUNT_DOWN, false).displayDays)
        assertEquals(1, DateEngine.dayCount(day, day, CountingMode.COUNT_UP, true).displayDays)
    }

    @Test fun nextAndPreviousDaysRemainOneDayApartAcrossMidnight() {
        val day = LocalDate.of(2026, 1, 1)
        assertEquals(1, DateEngine.dayCount(day.plusDays(1), day, CountingMode.COUNT_DOWN, false).displayDays)
        assertEquals(1, DateEngine.dayCount(day.minusDays(1), day, CountingMode.COUNT_UP, false).displayDays)
    }

    @Test fun calendarMonthAndYearBoundariesUseCalendarDays() {
        assertEquals(1, DateEngine.daysBetween(LocalDate.of(2025, 1, 31), LocalDate.of(2025, 2, 1)))
        assertEquals(1, DateEngine.daysBetween(LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 1)))
        assertEquals(2, DateEngine.daysBetween(LocalDate.of(2024, 2, 28), LocalDate.of(2024, 3, 1)))
    }

    @Test fun includeDueDateAddsTheEventDayToElapsedCount() {
        val today = LocalDate.of(2026, 6, 10)
        val target = LocalDate.of(2026, 6, 7)
        assertEquals(3, DateEngine.dayCount(target, today, CountingMode.COUNT_UP, false).displayDays)
        assertEquals(4, DateEngine.dayCount(target, today, CountingMode.COUNT_UP, true).displayDays)
    }

    @Test fun annualLeapDayFallsBackToFebruaryTwentyEighth() {
        val leapDay = LocalDate.of(2024, 2, 29)
        assertEquals(LocalDate.of(2025, 2, 28), DateEngine.annualDate(leapDay, 2025))
        assertEquals(LocalDate.of(2028, 2, 29), DateEngine.annualDate(leapDay, 2028))
    }

    @Test fun yearlyOccurrenceSelectsNextValidDate() {
        val anchor = LocalDate.of(2020, 10, 18)
        assertEquals(LocalDate.of(2026, 10, 18), DateEngine.nextOccurrence(anchor, LocalDate.of(2026, 10, 18), RepeatType.YEARLY))
        assertEquals(LocalDate.of(2027, 10, 18), DateEngine.nextOccurrence(anchor, LocalDate.of(2026, 10, 19), RepeatType.YEARLY))
    }

    @Test fun intervalRuleRoundsForwardToNextOccurrence() {
        assertEquals(LocalDate.of(2026, 10, 20), DateEngine.nextOccurrence(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 18), RepeatType.INTERVAL_DAYS, 5))
    }

    @Test fun dateOnlyMathIsUnaffectedByDstOffsetChanges() {
        val beforeDst = Instant.parse("2026-03-07T12:00:00Z").atZone(ZoneId.of("America/Los_Angeles")).toLocalDate()
        val afterDst = Instant.parse("2026-03-09T12:00:00Z").atZone(ZoneId.of("America/Los_Angeles")).toLocalDate()
        assertEquals(2, DateEngine.daysBetween(beforeDst, afterDst))
        assertFalse(beforeDst.isAfter(afterDst))
        assertTrue(DateEngine.monthLength(2024, 2) == 29)
    }
}
