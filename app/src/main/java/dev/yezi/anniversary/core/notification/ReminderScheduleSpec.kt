package dev.yezi.anniversary.core.notification

import dev.yezi.anniversary.core.database.ReminderEntity

/**
 * A feature-resolved request for one alarm. Core receives an absolute instant and
 * generic source identity; it does not interpret feature dates or recurrence rules.
 */
data class ReminderScheduleSpec(
    val reminder: ReminderEntity,
    val triggerAtEpochMillis: Long?
)
