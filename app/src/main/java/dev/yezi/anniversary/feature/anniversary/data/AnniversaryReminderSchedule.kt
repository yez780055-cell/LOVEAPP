package dev.yezi.anniversary.feature.anniversary.data

import dev.yezi.anniversary.core.database.ReminderEntity
import dev.yezi.anniversary.core.date.DateEngine
import dev.yezi.anniversary.core.date.RepeatType
import dev.yezi.anniversary.core.notification.ReminderScheduleSpec
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object AnniversaryReminderSchedule {
    const val SOURCE_TYPE = "anniversary"
    const val DELIVERY_ACTION = "dev.yezi.anniversary.action.ANNIVERSARY_REMINDER"

    fun resolve(event: EventEntity, reminder: ReminderEntity, nowMillis: Long = System.currentTimeMillis()): ReminderScheduleSpec {
        if (!reminder.enabled) return ReminderScheduleSpec(reminder.copy(nextTriggerAt = null), null)
        val zone = runCatching { ZoneId.of(event.timezone) }.getOrDefault(ZoneId.systemDefault())
        val anchor = runCatching { LocalDate.parse(event.targetDate) }.getOrNull()
            ?: return ReminderScheduleSpec(reminder.copy(nextTriggerAt = null), null)
        val today = LocalDate.now(zone)
        val repeatType = if (event.repeatType == "yearly") RepeatType.YEARLY else RepeatType.NONE
        val occurrence = DateEngine.nextOccurrence(anchor, today, repeatType)
            ?: return ReminderScheduleSpec(reminder.copy(nextTriggerAt = null), null)
        val time = runCatching { LocalTime.parse(reminder.localTime) }.getOrDefault(LocalTime.of(9, 0))
        var trigger = occurrence.minusDays(reminder.offsetDays.toLong()).atTime(time).atZone(zone).toInstant().toEpochMilli()

        if (trigger <= nowMillis) {
            if (event.repeatType != "yearly") return ReminderScheduleSpec(reminder.copy(nextTriggerAt = null), null)
            val nextOccurrence = DateEngine.nextOccurrence(anchor, today.plusDays(1), RepeatType.YEARLY)
                ?: return ReminderScheduleSpec(reminder.copy(nextTriggerAt = null), null)
            trigger = nextOccurrence.minusDays(reminder.offsetDays.toLong()).atTime(time).atZone(zone).toInstant().toEpochMilli()
        }
        return ReminderScheduleSpec(reminder.copy(nextTriggerAt = trigger), trigger)
    }
}
