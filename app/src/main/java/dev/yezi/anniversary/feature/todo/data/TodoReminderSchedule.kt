package dev.yezi.anniversary.feature.todo.data

import dev.yezi.anniversary.core.database.ReminderEntity
import dev.yezi.anniversary.core.notification.ReminderScheduleSpec
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

object TodoReminderSchedule {
    const val SOURCE_TYPE = "todo"
    const val DELIVERY_ACTION = "dev.yezi.anniversary.action.TODO_REMINDER"
    fun resolve(task: TodoTaskEntity): ReminderScheduleSpec {
        val reminder = ReminderEntity(
            id = "todo-${task.id}", sourceType = SOURCE_TYPE, sourceId = task.id,
            enabled = task.reminderEnabled && !task.isComplete,
            triggerType = "minutes_before", offsetDays = task.reminderMinutesBefore,
            localTime = task.scheduledTime ?: "09:00", exact = true,
            requestCode = task.id.hashCode(), notificationChannel = "todo_reminders",
            deliveryAction = DELIVERY_ACTION
        )
        val date = task.dueDate?.let { runCatching { java.time.LocalDate.parse(it) }.getOrNull() }
        val time = runCatching { LocalTime.parse(task.scheduledTime) }.getOrNull()
        if (!reminder.enabled || date == null || time == null) return ReminderScheduleSpec(reminder, null)
        val trigger = LocalDateTime.of(date, time).minusMinutes(task.reminderMinutesBefore.toLong()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return ReminderScheduleSpec(reminder.copy(nextTriggerAt = trigger), trigger)
    }
}
